package remedium;

import java.io.File;
import system.core.global;
import java.util.Properties;
import system.log.logHandler;
import system.mq.MessageQueue;
import system.mq.msg;
import utils.TimeTracker;
import system.net.Network;
import system.process.ProcessManager;
import system.process.Status;

/**
 *
 * @author Nuno Brito, 28th of May 2011 in Darmstadt, Germany.
 */
public class Remedium implements msg {

 // components of our system
    private MessageQueue mq = new MessageQueue(this);
    private ProcessManager processManager;
    private Network net = new Network(this);
    private logHandler log = new logHandler(this);


    protected TimeTracker timeTracker = new TimeTracker();

    // the backbone of our structure
    private SystemRem sys;

 // application initializer
    private system.core.global apps;
    private Properties properties = new Properties();

    private String storageLocation = "storage";
    private File storage = new File (storageLocation);

 // settings
    private String
            IDnameDefault = "remedium", // idenfication of this instance
            IDname = IDnameDefault,

            who = "system", // used for displaying log messages from this class
            myAddress = "http://localhost",
            defaultApp = "manager"; // the app that we start as default/root

    private int
            status = STOPPED; // intial instance status

    private boolean
            debug = true; // should we output error messages or not?

    public boolean isDebug() {
        return debug;
    }

    /**
     * Get the public ID of this instance
     */
    public String getIDname() {
        return IDname;
    }

   public String getIDnameDefault() {
        return IDnameDefault;
    }

   public String getIDserial() {
        String result = sys.ini.read(FIELD_ID_SERIAL);
        if(result == null)
            result = "";
        return result;
    }
    /** Get the system backbone of this instance */
    public SystemRem getSys(){
        return sys;
    }

    public TimeTracker getTimeTracker() {
        return timeTracker;
    }
    
    /** Provides back our system time */
    public long getTime(){
        return this.timeTracker.getTime();
    }

    public File getStorage() {
        return storage;
    }

    public Remedium() {
        this(new Properties());
    }

    public Remedium(final Properties data) {
        // assign our properties to the "data" object
        this.properties = data;
    }

    // start using only default parameters
    public boolean start() {
        return start(this.properties);
    }

    /**
     * This method will start the remedium system.
     * Optionally, you can add parameters to modify the default settings.
     *
     * Accepted parameters on the data container:
     *  - PORT - sets the port for the Network components
     *  - DIR - sets the default directory where work files are stored
     *  - ID - sets the identifier tag for this instance
     *  - DELETE - deletes the database or other resources when closing down
     *  - LOCK - Overwrite the randomly generated lock key
     */
    public Boolean start(Properties data) {
        boolean result = false;


        // pre-check
        if (isRunning()) {
            log(ERROR,"We are already running, before calling start you will "
                    + "need to stop this instance.");
            return false;
        }
        
        // import the startup values to our main properties object
        properties = data;



        if(data.containsKey(FIELD_PORT)==false)
            data.setProperty(FIELD_PORT, "" + net.getPort());


        // on case of multiple instances, add the port number to the ID.
        if (data.containsKey(FIELD_PORT)) {
            IDname = "rem-" + data.getProperty(FIELD_PORT);
        }

        // set our ID
        if (data.containsKey(FIELD_ID)) {
            IDname = IDname + " " + data.getProperty(FIELD_ID);
        }

        // should we delete the database when closing down this instance?
        if (data.containsKey(DELETE)) {
            properties.setProperty(DELETE, "");
        }

        
///////////////// kick start components and applications of our system

        // debut the Process Manager
        processManager = new ProcessManager(this);
        // check that we are available
        if(processManager == null)
            log(ERROR,"Process Manager failed to start");


        

        // set our working folder
        storage = new File(this.storage, getPort(this.properties));

        // MQ - is the msg queue available?
        if (!this.mq.hasStarted()) {
            this.mq.setRemedium(this);
            this.mq.start();
        }

        if (!this.mq.hasStarted()) {
            log(ERROR, "Failed to start the Message Queue system");
            return result;
        }

     
        // NET - is the Network service available?
        if (!this.net.hasStarted()) {
            this.net.setRemedium(this);
            this.net.start(data);
        }

        if (!this.net.hasStarted()) {
            log(ERROR, "Failed to start the Network service");
            return result;
        }

        // This has to be done before starting, otherwise the Applications will
        // stop in run() when they find  ' while (remedium.isRunning()) '
        setStatus(RUNNING);


     // the system component that provides specific services to this instance
        sys = new SystemRem(this);

     // we need to wait for the system component to finish its warm up
        while( sys.getProcess().getStatus()!= RUNNING){
            utils.time.wait(1);
        }

       
     // start our apps. Property "apps" allows to define which
     // apps are authorized to start. If empty, we assume that all apps start
        String authorizedApps = data.getProperty("apps","");
        apps = new system.core.global(this, authorizedApps);
        
        return true;
    }

    /**
     * Stop the instance using default values
     */
    public boolean stop() {
        return stop(new Properties());
    }

    /**
     * Stop the instance using customized values
     * Supported parameters:
     *  - DELETE - delete DB's created during startup
     *  - FORCE_FINISH - Force the system to exit even if not all threads have finished
     */
    public synchronized boolean stop(Properties parameters) {

         // set the overall system status as stopped
        setStatus(STOPPED);

        // if we have asked to delete traces on startup, enforce them here
        if (properties.containsKey(DELETE)) {
            parameters.setProperty(DELETE, "");
        }
        if (properties.containsKey(FORCE_FINISH)) {
            parameters.setProperty(FORCE_FINISH, "");
        }


        // let's close down our system
        net.stop();
        mq.stop();
        log.stop(properties);

        // reset some settings just in case someone starts this instance again
        IDname = IDnameDefault;

        // Force to exit the system if needed
            if (parameters.containsKey(FORCE_FINISH))
               System.exit(0);
        return true;
    }

    public global getApps() {
        return apps;
    }

    public MessageQueue getMQ() {
        return mq;
    }

    public Network getNet() {
        return net;
    }

    public ProcessManager getManager() {
        return processManager;
    }

    public logHandler getLog() {
        return log;
    }




    // Status handling routines
    private synchronized void setStatus(int newStatus) {
        status = newStatus;
    }
    /** what is our status */
    public synchronized int getStatus() {
        return status;
    }
    /** Are we running or not? */
    public synchronized boolean isRunning() {
        return (status == RUNNING);
    }
    /** log for this class alone */
    private void log(int gender, String message) {
        log.out(who,gender, message);
    }

    /** version available to the outside world */
    public void log(String who, int gender, String message) {
        // support for multiple instances running
            log.out(who, gender, message);
    }

    /** check if there is a log entry in particular (for debugging purposes) */
    public boolean logContains(String who, String what){
        return log.containsEntry(who, what);
    }

    /** filter messages from a given role */
    public void addLogFilter(String who){
        log.filterExcludeComponent(who);
    }

    /** filter messages from a given type */
    public void addLogGenderFilter(int gender){
        log.filterExcludeGender(gender);
    }


   public ProcessManager getProcess(){
            return processManager;
   }

    /**
     * Register a given role inside the process manager
     * @param role The role object that will be added
     * @param data The data that will be represent this object
     */
    public synchronized Boolean register(Status process){

        // preflight check
        if(process == null){
            log(ERROR, "Failed to register process, provided object is null");
            return false;
        }
        // all said and done, add this to the list
        return processManager.add(process);
    }


    /** get the Web address of this instance */
    public String getMyAddress() {
        // get the current address registered on the instance
        String result = myAddress;
        // if we are on port 80, no need to add port number
        if(getNet().getPort()!=80)
            result += ":" +getNet().getPort();
        // return our result
        return result;
    }

    /** get the default app name and location */
    public String getDefaultApp() {
        return defaultApp;
    }

    private String getPort(Properties settings){
    // if there is a port definition, use it if this is a valid number
            if(settings.containsKey(FIELD_PORT)){
                //change the current folder
                String temp = // do a filter to prevent malicious inputs
                    utils.text.findRegEx(
                        settings.getProperty(FIELD_PORT)
                        ,"[0-9]+$", 0); // only accept 0-9 chars
                if((temp != null)
                 && (temp.length()>0)){ // if the result is bigger than zero, use it
                    log(INFO,"Using "+temp+" as database storage folder ("
                            +settings.getProperty(FIELD_PORT)+")");
                    // do the change
                    return temp;
                }
            }
            return "";
    }

}
