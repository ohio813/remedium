/*
 * Component is a template class for any other classes that are
 * used as system components. Examples of classes that fit this
 * category are: Database, Network, Log, msg Queue and so forth.
 *
 * The purpose is to ensure that these components of the system
 * follow a standard class that provides services such as error
 * recovery and status information without need of implementing
 * these features by themselves.
 *
 * Services provided:
 *      - log recording
 *      - registration on process manager
 *      - make available a properties object that is shared across methods
 *      - automatic update of status on process manager
 *      - display status to outside (public methods) 
 *      - update status on the inside (protected methods)
 *      - time provider without need to use System.currentTimeMillis()
 *
 */

package system.core;

//import system.container.ContainerHSQL;
import app.user.User;
import java.io.File;
import system.container.INIcontainer;
import system.container.Box;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.container.Container;
import system.log.LogMessage;
import system.mqueue.msg;
//import system.database;
import system.net.protocols;
import system.process.Status;
import system.html.HtmlGenerator;
import system.html.Meta;

/**
 *
 * @author Nuno Brito 3rd of April 2011 in Darmstadt, Germany.
 */
public abstract class Component extends Thread
        {

    // definitions
    private boolean
            debug = false; // should this class output debug messages or not?

    // objects
    private Remedium
            instance; // the support instance

    private Status
            process; // the status for process manager

    protected Children
            children = new Children(this);

    public Box // our box of containers
            box = new Box(this);

    private Component // define the father of this component
            father = null; // at the beginning, his father is null at his life
   
    // where we keep our persistent data
    public INIcontainer
            ini;

    // protected objects
    protected String
            myAddress;

    protected Properties 
            settings;  // allow extensions to set this value
            
    protected int
            WAIT_TIME = 4; // how long between time loops here?
    
    public HtmlGenerator // HTML page creation
                 html;

    private String
            ID_serial = ""; // number that identifies this remedium instance

    /** public constructor with recover option*/
    public Component(Remedium assignedInstance,
       long assignedRemLock, Boolean recover){
       doConstructor(assignedInstance, recover, null);
    }
    /** public constructor *without* recover option*/
    public Component(Remedium assignedInstance){
       doConstructor(assignedInstance,false, null);
    }
    /** public constructor *without* recover option*/
    public Component(Remedium assignedInstance,
       Component assignedFather){
       doConstructor(assignedInstance,  false, assignedFather);
    }


    /**
     * Initialize this instance. The constructors call this method by default.
     */
    private void doConstructor(Remedium assignedInstance,
            Boolean recover, Component assignedFather){
        // preflight checks
        if(assignedInstance == null){
            System.out.println("ERROR, Can't start component using a null "
                    + "instance");
            return;
        }
        if(utils.text.isEmpty(getTitle())){
            log(msg.ERROR,"Can't start component using an empty "
                    + "title");
            return;
        }
        // assign this instance as our work instance
        setInstance(assignedInstance);
        // set the father of this component
        if(assignedFather!=null)
            setFather(assignedFather);
        // create our status object to place on the process manager
        setProcess(new Status(getCanonicalName()));
         // set the operational status (running, stopped, etc)
            getProcess().setStatus(msg.STOPPED);
         // set the process parameters (settings, definitions, progress, etc)
            settings = new Properties();
            getProcess().setParameters(settings);
        // register ourselves on the process manager
         getInstance().getManager().addComponent(this);

        // grab our full web address
         myAddress = "http://localhost:"+this.getInstance().getNet().getPort();

         // register our HTML service
         html = new HtmlGenerator(this.getCanonicalName());
         // customize it to suit our needs
         setHTML();
        // if there was an abnormal termination, proceed with cleanup here
        if(recover){
            onRecover();
        }
       
        // start our thread
        start();

    }


    /** Abstract methods to be overriden */
    public abstract void onStart(); // when starting normally
    public abstract void onRecover();// starting after an error forced to close
    public abstract void onLoop();// when looping
    public abstract void onStop(); // when closing things down
    public abstract String getTitle(); // get title assigned to this instance

    // this allows to handle web requests made to this component
    public abstract String doWebResponse(Request request, Response response);


 /////// Protected methods (available for extended methods)


    /** log messages to standard output */
    public final void log(int gender, String message) {
        instance.log(getCanonicalName(), gender, message);
    }
    public final void log(String client, int gender, String message) {
        instance.log(getCanonicalName()+"/"+client, gender, message);
    }

    
    /** Add a given component as our father, this can only be done once */
    private Boolean setFather(Component who){
        // preflight checks
        if(who == null){
            log(msg.ERROR, "Add father operation failed. This father is null");
            return false;
        }
        if(father != null){
            log(msg.ERROR, "Add father operation failed. We already have a father");
            return false;
        }
        // assign this component as our father
        father = who;
        // all done, output as sucess
        return true;
    }


 /////// Private methods (cannot be modified)

    /** Update elements on the HTML page such as navigation.
     This method is called inside a thread, updated at each second*/
    private void updateHTML(){

        // Add our root node of the breadcrumb
        if(this.getCanonicalName()//ignore if default app is equal to called app
                .equalsIgnoreCase(this.getInstance().getDefaultApp())==false)
        html.nav.addLink(
                     "index", // index name
                     "index",  // title visible to user
                     this.getInstance().getMyAddress()
                     , // web page to call
                    false); // are we selected or not?

        // if this is a child component, add the father
        if(hasFather()){
            html.nav.addLink(
                     father.getCanonicalName(), // index name
                     father.getTitle(),  // title visible to user
                     myAddress+"/"+
                     father.getCanonicalName()+
                     "?show="+"home", // web page to call
                    false); // are we selected or not?
        }


        // add the home title
        html.nav.addLink(
                getCanonicalName(), // index name
                getTitle(),  // title visible to user
                getCanonicalName()+"?show="+"home", // web page to call
                false); // are we selected or not?
    }


    /** Update the status of this instance on the process manager. */
     private void updateStatus(){
         // set the process parameters (settings, definitions, progress, etc)
            process.setParameters(settings);
         // write them onto the Process Manager
            instance.getProcess().setStatus(process);
         // update our list of childs along with other HTML details
            updateHTML();

     }

     /** Check indicators and changes in the outside system */
     private void monitor(){
         // if the support instance is not running, stop here as well
        if(instance.isRunning()==false){
            log(msg.INFO,"Stopping component");
            process.setStatus(msg.STOPPED);
        }
     }


     /** Ensure that our page runs automatically when doing some operation */
    public void addAutoHTMLrefresh(int seconds, String URL) {
        // set this page to update automatically
        Meta loopMeta = new Meta();
        loopMeta.add("http-equiv", "refresh");
        loopMeta.add("content", seconds + ";url=" + URL );
        html.setMeta(loopMeta.getText());
    }


     /** close our reply and dispatch the resulting text */
    protected void finishWebResponse(Request request, Response response,
            String text){

        PrintStream responseBody = null;

        try {
            responseBody = response.getPrintStream();
        } catch (IOException ex) {
           log(msg.ERROR,"finishResponse operation failed, invalid getPrintStream");
           return;
        }
        // we can't afford a null reply here
        if(responseBody==null){
           log(msg.ERROR,"finishResponse operation failed, invalid getPrintStream");
           return;
        }

            responseBody.println(text);
            responseBody.close();
//            log(msg.ROUTINE,"Web page request at "
//                    + utils.time.getDateTime()
//                    + " from "
//                    + request.getClientAddress().getHostName()
//                    );
    }

    /** Add all our custom settings to the HTML container */
    private void setHTML(){

        // add the breadcrumb index
         html.nav.addLink(
                     "index", // index name
                     "index",  // title visible to user
                     this.getInstance().getMyAddress(), // web page to call
                    false); // are we selected or not?

    // if this is a child component, add the father
        if(hasFather()){
            html.nav.addLink(
                     father.getCanonicalName(), // index name
                     father.getTitle(),  // title visible to user
                     myAddress+"/"+
                     father.getCanonicalName()+
                     "?show="+"home", // web page to call
                    false); // are we selected or not?
        }

    // Add default tab:
            html.nav.addLink(
                    getCanonicalName(), // index name
                    this.getTitle(),  // title visible to user
                    getCanonicalName()+"?show="+"home", // web page to call
                    true); // are we selected or not?

            html.setCopyright("powered by remedium");
            //, "+utils.time.getCurrentYear());
    }

/////// Public methods (mostly with password)



    /** get canonical name (including the father's name) */
    public final String getCanonicalName(){
        String result = "";
        if(hasFather()) // add the father name if one exists
            result = getFatherName()+"/";
        // add our own name and output the results
        return result + getTitle();
    }

    /** Get the serial identification number of this instance */
    public final String getIDserial(){
        return ID_serial;
    }

    /** Get web address of this component */
    public final String getWebAddress(){
        return getInstance().getMyAddress()+"/"+this.getCanonicalName();
    }



    /**
     * This is the version used for debugging purposes, if the debug flag
     * is not enabled then it will simply exit
     */
    public final Boolean sendDebug(Properties message) {
        if (!debug) {
            return false;
        }
        // send a msg using the default way
        send(message);
        return true;
    }



    /** we have received a web request for us, let's take care of it */
    public final String getWebResponse(Request request, Response response){

        // if this is a BOX request, handle it immediately and exit
        String actionBox = utils.internet.getHTMLparameter(request, "box");
        if(utils.text.isEmpty(actionBox) == false){
            // get the intended result from the box
            String result = box.batchSynchronize(request, response);
            this.finishWebResponse(request, response, result);
            return result;
        }

        // if this is a DB request, handle it immediately and exit
        String actionDB = utils.internet.getHTMLparameter(request, "db");
        if(utils.text.isEmpty(actionDB) == false){

            // verify if our database exists, before calling it over
            if(box.exists(actionDB) == false){
                String result = "Web: Requested DB does not exist: '"
                        + actionDB + "'";
                log(msg.DEBUG, result);
                this.finishWebResponse(request, response, result);
                return result;
            }

            // get the intended result from the requested database
            String result = box.get(actionDB).webRequest(request, response);
            // print the result
            this.finishWebResponse(request, response, result);
            // return the result from this request
            return result;
        }


        // list of content types at http://goo.gl/ZTJos
        response.set("Content-Type", "text/html");

        // Initial step: we can police the request before delivery to component
            String 
                result = "",
                selectedSection = "";

            // clean up the meta tags
            html.setMeta("");

            // process this request by the component
            String processRequest = doWebResponse(request, response);

            // Special switch, if process says ignore then it means that he
            // has already done everything on its own. No need to continue
            if(processRequest.equalsIgnoreCase("ignore"))
                return ""; // no need to any thing else beyond this point

            selectedSection = utils.internet.getHTMLparameter(request, "show");

            
            // if nothing is selected, choose the home section page
            if( utils.text.isEmpty(selectedSection))
                selectedSection = html.SectionHome;

                // select it on the sub navigation bar
                html.sub.setSelected(selectedSection);
                // show the requested section if found
                result = html.getPage(selectedSection);

            // print our reply to the browser
            this.finishWebResponse(request, response, result);
         // provide our results for anyone interested
        return result;
    }



 /////// GETTERS

    /** returns the status as available on the process manager */
    public final Status getProcess() {
        return process;
    }

     /** Provides back our system time */
    public final long getTime(){
        return instance.getTime();
    }

    /** get the synchronized version of the instance */
    public synchronized final Remedium getInstance() {
        return instance;
    }


    /**
     * Get the father of this component if any.
     * @return null if father is not specified
     */
    protected final Component getFather(){
       if(father != null)
           return father;
       return null;
    }

    protected final String getFatherName(){
       if(father != null)
           return father.getTitle();
       return "";
    }

    /** get the running state of this instance (running, stopped, etc) */
    protected final int getOperationalStatus() {
       return this.process.getStatus();
    }

 /////// Question makers

    /** Are we currently logged to a given system or not? */
    protected boolean isLogged(Request request){
        // get the origin of this request
        String from = utils.internet.getAddress(request);
        // return true if it is listed
        return this.getInstance().getLoggedAddress().containsKey(from);
    }

    /** Get the details from the currently logged user */
    public User getLoggedUser(Request request){
        // get the origin of this request
        String from = utils.internet.getAddress(request);
        if(this.getInstance().getLoggedAddress().containsKey(from)==false)
            return new User(); // return an empty user if we don't know him
        // got this far, return User details
        return this.getInstance().getLoggedAddress().get(from);
    }

 /////// SETTERS

    /** Set the running state of this instance (running, stopped, etc) */
    public final void setOperationalStatus(int operationalStatus) {
        this.process.setStatus(operationalStatus);
    }

    /** Set the time between loops */
    protected final void setTime(int Time) {
        this.WAIT_TIME = Time;
    }

    /** set the current active instance */
    private void setInstance(Remedium instance) {
        this.instance = instance;
    }

    /** synchronized way of setting this process */
    private void setProcess(Status process) {
        this.process = process;
    }



////////// Questions

    /** If this component has a father, say "yes" */
    protected final Boolean hasFather(){
        return father != null;
    }

////////// Do actions

    /** instructs this component to stop, we also stop our children */
    public void doStop(){
        // Call the generic onStop that is overwritten by extensions
        onStop();

        //Close depending services:
//        db.stop(settings);


        process.setStatus(msg.STOPPED);

        children.stop();

    }

   /** Default incomingmsg that appears when no injected method is available */
    public void doDefaultMessage(Properties incomingmsg) {
        log(msg.DEBUG,"doDefaultMessage: Got message from "
                + incomingmsg.getProperty(msg.FIELD_FROM));
    }




   
////////// Looped threads

    @Override
    public final void run(){

        // set ourselves as running
        setOperationalStatus(msg.STARTING);

        // start our internal services:
//        startDB();

        // kick start our INI container using the DB
         ini = new INIcontainer(this);

        // Call the onStart
        onStart();
        
        // retrieve our unique ID number
        ID_serial = this.getInstance().getIDserial();

         // set ourselves as running
        setOperationalStatus(msg.RUNNING);

        // do the loop until it stops
        while(
                getInstance().isRunning()
                //process.getStatus() != STOPPED
                ){

            // put some business logic here
            if(process.getStatus() == msg.RUNNING)
                //  only run the loop if we are in the "running" status
                onLoop();
           // do some waiting..
             //try {
                 //log(DEBUG, "Waiting " + WAIT_TIME + " seconds");
                 //sleep(WAIT_TIME * 1000);
                 utils.time.wait(WAIT_TIME);
                 //} catch (InterruptedException e) {}
           // update our status on the process manager
            updateStatus();
           // monitor changes in the outside world
            monitor();
           // grab our messages and react to them
            getNewMessages();
        } // while
        
        // call the onStop
        onStop();
    }// start


    ///// Container related procedures

//    /** Create a storage container with no checks*/
//    public final ContainerHSQL createDB(String title, String[] fields){
//        return new ContainerHSQL(this, title, fields);
//    }
    /** Create a storage container with no checks*/
    public final Container createDB(String title, String[] fields){
        LogMessage result = new LogMessage();
        return new Container(title, fields, this.getStorage(), result);
    }

    /** Get the storage folder for this component */
    public final File getStorage(){
        // create the folder as a subfolder inside our dedicated storage area
        File result = new File(this.instance.getStorage(),
                this.getCanonicalName());
    return result;
    }

    /** write on the storage container */
    public final void writeDB(Container container, String[] fields){
        container.write(fields);
    }


    ///////////////////// msg queue related fields

    /* Send a message to a specific task in a specific component*/
    public final void send(String toComponent,
            String task, String message) {
        Properties data = new Properties();
        data.setProperty(msg.FIELD_TO, toComponent);
        data.setProperty(msg.FIELD_TASK, task);
        data.setProperty(msg.FIELD_MESSAGE, message);
        this.send(data);
    }



     /**
     * This is the send msg to Queue command that is sent from a given
     * application, it is intended to be used only by roles so that we can
     * control the informations that are passed onto the queue.
     */
    public final void send(Properties message) {
        // preflight check
        if(this.instance.isRunning()==false)
            return;

        if(message == null){
            log(msg.ERROR,"Send message operation failed, tried to send a null"
                    + " message");
            return;
        }
        // we ensure that the ID tag is not faked, force the FROM field to be
        // from the component itself and not some other component.
        message.setProperty(msg.FIELD_FROM, getCanonicalName() );
        // send the msg
        instance.getMQ().send(message);
    }

     /** Password protected Send */
//    public final void send(long unLock, Properties message) {
//        send(message);
//    }


    /////////////////////////////
    //
    // Code injection portion, this is where we handle injected methods
    //
    /////////////////////////////

    /** if we received a message for us in the queue, call respective method */
    public final synchronized void digest(Properties digestmsg)
            throws NoSuchMethodException {
        try {
            @SuppressWarnings("rawtypes")
            Class partypes[] = new Class[1];
            partypes[0] = Properties.class;

            Class<? extends Component> cls = this.getClass();
            Method meth = cls.getMethod(this.getMethodNameFromMessage(digestmsg),
                    partypes);

            // sets the digestmsg (Properties) as parameter
            Object arglist[] = new Object[1];
            arglist[0] = digestmsg;

            meth.invoke(this, arglist);

        } catch (Throwable e) {

            log(msg.ERROR,"Digest message failed: '"+e.getMessage()+"'");

            /**
             * this means that we didn't found the expected method, just
             * call the method msgDefault(digestmsg) that can be overriden by each
             * role if they desire to.
             */
            log(msg.INFO,"Method '" + this.getMethodNameFromMessage(digestmsg) + "()' "
                    + "was not found, calling 'msgDefault()' instead");
            doDefaultMessage(digestmsg);
        }
    }



    /**
     * Extract the identification name of the role that is being contacted
     * If FIELD_TASK exists on the targetmsg, it will return that name instead.
     */
    private String getMethodNameFromMessage(Properties targetmsg) {
        String ret = "digest";
        if (targetmsg.containsKey(msg.FIELD_TASK)) {
            // we need to split the Application from the role, typically
            // the FIELD_FROM will appear like "AppA/RoleB"
            String call = targetmsg.getProperty(msg.FIELD_FROM);

            // modification to suit component classes
            if(call.indexOf("/") > 0)
                call = call.substring(call.indexOf("/") + 1);

            // now we append, _<task name>
            ret += "_" + targetmsg.getProperty(msg.FIELD_TASK);
        } else {
            // we need to split the Application from the role, typically
            // the FIELD_FROM will appear like "AppA/RoleB"
            String call = targetmsg.getProperty(msg.FIELD_FROM);
            // modification to suit components without father
            if(call.indexOf("/") > 0)
                call = call.substring(call.indexOf("/") + 1);
            ret += call;
        }
        debug("DigestMessage: Calling method '"+ret+"'");
        return ret;
    }


    /** get the messages on the queue that are targeted to us */
    private void getNewMessages(){

        // grab all messages from the queue
        ArrayList<Properties> messages
                = instance.getMQ().get(this.getCanonicalName());

        //debug("No messages for " + this.getCanonicalName());

        if (
             (messages == null)   // can't be null
          || (messages.isEmpty()) // can't be empty
           )
            return; // no point in proceeding if there are no messages

            //TODO this only handles one msg per round
            // perhaps we should limit the search to the first 100
            // messages? This would smooth peak request times
            Properties message = messages.get(0);
            try {

                // if the messages comes from another machine, display it
//                String ticket = "";
//                if(message.containsKey(FIELD_TICKET)){
//                    ticket = "with ticket #" + message.getProperty(FIELD_TICKET);
//                }
//
//                log(ROUTINE,"Received a message "
//                           + ticket
//                        );

                // if this was a msg from the outside, convert
                // the object
                String params =
                        message.getProperty(msg.FIELD_PARAMETERS, "");
                // do we have something to convert or not?
                if(!utils.text.isEmpty(params)){
                // we need to copy the ID
                    String id = message.getProperty(msg.FIELD_ID);
                // then convert the parameters onto a full object
                   // System.out.println("---------"+msg.toString());
                  message = protocols.stringToProperties(params);
                  // we need to restore these two values to make valid as before
                  message.setProperty(msg.FIELD_PARAMETERS, params);
                  message.setProperty(msg.FIELD_ID, id);
                }

                // calls the method mentioned in the msg
                digest(message);

                // Delete the message after digestion
                instance.getMQ().delete(message.getProperty(msg.FIELD_ID));

            } catch (NoSuchMethodException e) {
                log(msg.ERROR, e.toString());
            }
        
    }


    //////////////// Handling of messages


    /** dispatch a message to another component, request a status change */
    protected final void requestChangeStatus(String who, int newStatus){

        log(msg.INFO, "Requesting '"+who+"' to change his status to "
            +utils.text.translateStatus(newStatus));
    
            Properties message = new Properties();
            // the fields that we need to place here
            message.setProperty(msg.FIELD_FROM, this.getCanonicalName());
            message.setProperty(msg.FIELD_TO, who);
            message.setProperty(msg.FIELD_TASK, msg.CHANGE_STATUS);
            message.setProperty(msg.CHANGE_STATUS, ""+ newStatus);
            // dispatch the message out the queue
            send(message);
    }

    /** Change the status of this component */
    public void digest_change_status(Properties message){
        
        if(message.containsKey(msg.CHANGE_STATUS) == false)
            return; // no change request, then we might exit

  //      System.err.println(message.toString());
        // get our change request
        int newStatus =
                Integer.parseInt(
                    message.getProperty(msg.CHANGE_STATUS, "0")
                        );

        // if the status is valid then apply it to our component
        if(       (newStatus == msg.STOPPED)
                ||(newStatus == msg.RUNNING)
                ||(newStatus == msg.PAUSED)
                ||(newStatus == msg.RESUME)
                ){
            this.getProcess().setStatus(newStatus);
        }
        
        log(msg.INFO, "Changed status to "
                + utils.text.translateStatus(newStatus)
                );


        if(newStatus == msg.STOPPED){
            onStop();
        }
    }

  /** Display log messages only if authorized */
 private void debug(String message){
    if(debug)
        log(msg.DEBUG,message);
 }


}
