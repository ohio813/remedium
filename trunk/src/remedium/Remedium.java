package remedium;

import java.io.File;
import system.core.global;
import java.util.Properties;
import system.database;
import system.log.log_handler;
import system.msg;
import system.TimeTracker;
import system.message_queue;
import system.net.Locker;
import system.net.network;
import system.process.ProcessManager;
import system.process.Status;

/**
 *
 * @author Nuno Brito, 28th of May 2011 in Darmstadt, Germany.
 */
public class Remedium implements msg {

 // components of our system
    private database db = new database();
    private message_queue mq = new message_queue();
    private ProcessManager processManager;
    private network net = new network();
    private log_handler log = new log_handler(this);


    protected TimeTracker timeTracker = new TimeTracker();

    private SystemRem sys;

 // application initializer
    //private apps.global apps = new apps.global();
    private system.core.global apps;
//    private ArrayList<Application> applications = null;
    private Properties properties = new Properties();

 // ID's
   protected Locker
           lock; // provides protection for public methods

   private long 
           remLock = utils.math.RandomInteger(1, 9999999);

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

    

    public boolean isDebug() {
        return debug;
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

    public Remedium(Properties data) {
        this.properties = data;

        db.setRemedium(this);
        mq.setRemedium(this);
        //manager.setRemedium(this);
        net.setRemedium(this, remLock);
        //apps.setRemedium(this);

//        applications = new ArrayList<Application>();
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
     *  - PORT - sets the port for the network components
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

        // if we have a provided lock key, then use this value
        if (data.containsKey(LOCK)) {
            // use a key specified at start time
            long assignedLock = Long.parseLong(data.getProperty(LOCK));
            lock = new Locker(assignedLock,IDname);
            remLock = assignedLock;
        }else
            lock = new Locker(remLock, IDname); // generate use a random key


///////////////// kick start components and applications of our system

        // debut the Process Manager
        processManager = new ProcessManager(this, remLock);
        // check that we are available
        if(processManager == null)
            log(ERROR,"Process Manager failed to start");


        // DB - do we need to start up the database system by ourselves?
        if (!this.db.hasStarted()) {
            this.db.start(data);
        }

        if (!this.db.hasStarted()) {
            log(ERROR, "Failed to start database system");
            return result;
        }

        // MQ - is the msg queue available?
        if (!this.mq.hasStarted()) {
            this.mq.setRemedium(this);
            this.mq.start();
        }

        if (!this.mq.hasStarted()) {
            log(ERROR, "Failed to start the Message Queue system");
            return result;
        }

     
        // NET - is the network service available?
        if (!this.net.hasStarted()) {
            this.net.setRemedium(this, remLock);
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
        db.stop(parameters);
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

    /** No security checks here, we should really solve this.....*/
    public database getDB() {
        return db;
    }

    public message_queue getMQ() {
        return mq;
    }

    public network getNet() {
        return net;
    }

    public ProcessManager getManager() {
        return processManager;
    }

    public log_handler getLog() {
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


    /**
     *
     * @param unlock The key necessary to unlock this method
     * @return
     */
   public ProcessManager getProcess(){
       
       //if(lock.check(unlock))
            return processManager;
//       else
//            return null;
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


//    /** @return the applications that we have registered in our system */
//    public ArrayList<Application> getApplications() {
//        synchronized (this.applications) {
//            return applications;
//        }
//    }

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
        return //getMyAddress()+"/"+
                defaultApp;
    }




    /**
     * Add a new application to the list of applications that
     * are associated with this instance.
     *
     * Once the application is added, we will also try to launch
     * it, this means that all Roles associated with the application
     * will also be launched as well.
     */
//    public synchronized Boolean addApplication(Application application) {
//        log(INFO,"Adding application '" + application.getTitle() + "'");
//        //TODO process manager registration is not working, please fix
//        //application.registerRoles();
//        Boolean result = this.applications.add(application);
//        application.start();
//        return result;
//    }

    /** Ask a true/false question to our local properties. The answer defaults
     * to false if it was not found. If it was found, returns a true/false
     * result.
     */
    public Boolean is(String question){
       return properties.getProperty(question,"false").equalsIgnoreCase("true");
    }
}
