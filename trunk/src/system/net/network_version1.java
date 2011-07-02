/*
 * This is the implementation of the network interface using the simple web
 * framework and HSQL
 */
package system.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import system.msg;

/**
 *
 * @author Nuno Brito, 18th of May 2011 in Pittsburgh, USA
 */
public class network_version1 implements network_interface, msg {

    // turn this variable as true if you want debugging messages visible
    Boolean debug = false;
    // network port used by our system (use the default value unless specified otherwise
    String network_port = PORT_DEFAULT;
    // status of our system (use STOPPED as default)
    int status = STOPPED;
    // holder of the settings for this system
    Properties settings = new Properties();
    /**
     * Valid settings:
     *     - PASSWORD - sets a password to receive new messages
     *     -
     */
    // the thread that listens to a given port
    private RunnerThreadProcessExternalRequests networkThreadProcessExternal
            = new RunnerThreadProcessExternalRequests();
    private RunnerThreadProcessInternalRequests networkThreadProcessInternal
            = new RunnerThreadProcessInternalRequests();
    private HashMap<String, ArrayList<Properties>> networkMappings = null;
    public Connection connection = null;//new SocketConnection(container);
    private Remedium remedium;
    private long remLock = 0;

    /**
     * Generic start of the network system using default values, for more
     * ellaborated start, do set the properties as documented.
     * @return
     */
    @Override
    public boolean start() {
        Properties parameters = new Properties();
        return start(parameters);
    }

    /** start our network interface
     *  Supported parameters:
     *         - PORT - sets the port to use, by default is PORT_DEFAULT
     *         - PASSWORD - sets a password to validate incoming requests
     *          - LISTEN - starts listening to requests
     */
    @Override
    public boolean start(Properties parameters) {
        /**
         * On this method we will initialize the network system. Contrary to
         * popular belief, we won't start listening to any given port right away
         * but rather prepare only the basic settings.
         * 
         * We don't need to start listening to a port in order to dispatch new
         * messages.
         */
        if (parameters.containsKey(FIELD_PORT)) {
            String temp =
                    utils.text.findRegEx( // do filter to only accept numbers
                    parameters.getProperty(FIELD_PORT), "[0-9]+$", 0);
            int i = Integer.parseInt(temp);
            // this number can never be set below port 80
            if (i >= 80) {
                setPort(i);
                log(ROUTINE,"Changing from the default port number to port " + i);
            }
        }else
        {
            parameters.setProperty(FIELD_PORT, PORT_DEFAULT);
        }

        // if we should start listening to requests, start the listener thread
        if (parameters.containsKey(FIELD_LISTEN)) {
            //setStatus(LISTENING);
            networkThreadProcessExternal.start();

        }

        // update our status
        status = RUNNING;
        log(ROUTINE,"Network system has started at port "
                + parameters.getProperty(msg.FIELD_PORT));
        networkMappings = new HashMap();

        networkThreadProcessInternal.setRemedium(this.getRemedium());
        networkThreadProcessInternal.setNetworkMappings(this.networkMappings);
        networkThreadProcessInternal.start();

        return true;
    }


    /**
     * If our system has already been initialized then we'll output a flag result
     * as RUNNING, otherwise return false
     */
    @Override
    public boolean isRunning() {
        return (status >= RUNNING);
    }

    @Override
    public boolean hasStarted() {
        return (status != STOPPED);
    }

    /**
     * At our network system, we don't require to be listening at a given port if
     * our client is only intended to dispatch messages. If on the other hand
     * we are expected to receive them, this method must return a true value.
     */
    @Override
    public boolean isListening() {
        return (status == LISTENING);
    }

    /**
     * This method returns the port that is currently used as default at
     * our system.
     */
    @Override
    public int getPort() {
        return Integer.parseInt(network_port);
    }

    @Override
    public boolean setPort(int newPort) {
        // check constraints
        if ((newPort > 64535) || (newPort < 80)) {
            return false;
        }
        // set the new port
        network_port = Integer.toString(newPort);
        // verify that we have really setup  the new port
        return getPort() == newPort;
    }

    /**
     * Adds a new status on our system
     */
    void setStatus(int newStatus) {
        status = newStatus;

        switch (status) {
            case LISTENING:
                debug("Changed status to LISTENING");
                break;
            case RUNNING:
                debug("Changed status to RUNNING");
                break;
            case SUSPENDED:
                debug("Changed status to SUSPENDED");
                break;
            case INACTIVE:
                debug("Changed status to INACTIVE");
                break;
            case ERROR:
                debug("Changed status to ERROR");
                break;
            default:
                debug("Changed status to " + Integer.toString(status));
        }
    }

    public String getStatus() {

        String s = "";

        switch (status) {
            case LISTENING:
                debug("Network system is LISTENING");
                s = "LISTENING";
                break;
            case RUNNING:
                debug("Network system is RUNNING");
                s = "RUNNING";
                break;
            case SUSPENDED:
                debug("Network system is SUSPENDED");
                s = "SUSPENDED";
                break;
            case INACTIVE:
                debug("Network system is INACTIVE");
                s = "INACTIVE";
                break;
            case ERROR:
                debug("Network system is in ERROR");
                s = "ERROR";
                break;
            default:
                debug("Network system return status code #" + Integer.toString(status));
                s = Integer.toString(status);
                break;
        }
        return s;
    }

    /**
     * Generic way of stopping the network system using default values
     */
    @Override
    public boolean stop() {
        Properties parameters = new Properties();
        return stop(parameters);
    }

    /**
     * Stop our networking system
     */
    @Override
    public boolean stop(Properties parameters) {
        debug("Stopping the network system");
        try {
            // if we are listning to a port, cut the network connection
            if (isListening()) {
                getRemedium().getNet().connection.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(network_version1.class.getName()).log(Level.SEVERE, null, ex);
        }
        // update our status
        setStatus(STOPPED);
        return true;
    }

    @Override
    public ArrayList<Properties> get(Properties parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<Properties> get(String clientName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method will change the functioning of our server. Available
     * parameters:
     *      - LISTEN - starts listening on the specified port
     *      - PORT - sets the port where we should listen
     */
    @Override
    public boolean update(Properties parameters) {

        if (parameters.containsKey(FIELD_PORT)) {
            String temp =
                    utils.text.findRegEx( // do filter to only accept numbers
                    parameters.getProperty(FIELD_PORT), "[0-9]+$", 0);
            int i = Integer.parseInt(temp);
            // this number can never be set below port 80
            if (i >= 80) {
                setPort(i);
                debug("Using port " + i);
            }
        }

        if ( // should we start our server?
                (parameters.containsKey(FIELD_LISTEN))
                && (!isListening())) {
            // launch the thread to liste on a given port
            networkThreadProcessExternal.start();
        }

        return true;
    }


    /** get a given page from a given URL address */
    public String webget(String address) {
        // perhaps in the future we can use something like http://goo.gl/03WQp
        // provide a holder for the reply
        String result = "";

        try {
            URL webpage = new URL(address);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(webpage.openStream()));
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Process each line.
                result = result.concat(inputLine);
                //result = result + inputLine;
            }
            in.close();
        } catch (MalformedURLException me) {
            log(ERROR, "webGet operation has failed, MalFormedException: "
                    + me.toString());
            return null;
        } catch (IOException ioe) {
            log(ERROR, "webGet operation has failed, IOException: "
                    + ioe.toString());
            return null;
        }

        return result;
    }

    /**
     * This version of the send() method will use version 1 of the network
     * protocol when no specific version is specified.
     */
    @Override
    public Properties send(Properties message) {
        // pre-flight checks
        if (message == null) {
            log(ERROR, "Can't send a message as an empty container");
            return null;
        }

        // if a server is provided, use that value or quit here.
        if ( (!message.containsKey(FIELD_ADDRESS))
           ||( message.getProperty(FIELD_ADDRESS).isEmpty())
                ) {
            log(ERROR, "Can't send this message to an empty address");
            return null;
        }


        // all checked and ready to send the msgbox through the wire
        debug("SENDING: " + message.toString());

        // the composed msgbox with URL included
        String www =
                // we are using the HTTP format
                "http://"
                // the URL where the msgbox will be dispatched onto
                +
                message.getProperty(FIELD_ADDRESS)
                + "/"
                // the ?result parameter contains the content of the msgbox
                + "?msg="
                // translate the Properties object onto a safe string
                + protocols.propertiesToString(message);

        debug("Dispatching message as " + www);

        // provide a holder for the reply
        String msg = "";

        // if we are no longer running then quit here
        if(this.getRemedium().isRunning()==false)
            return null;

        try {
            URL webpage = new URL(www);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(webpage.openStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                // Process each line.
                //msgbox = msgbox + inputLine;
                msg = msg.concat(inputLine);
                debug(inputLine);
            }
            in.close();
        } catch (MalformedURLException me) {
            // if we are no longer running then quit here
            if(this.getRemedium().isRunning())
            log(ERROR, "Send operation to "+message.getProperty(FIELD_ADDRESS)
                    +" has failed: "
                    + me.toString());
            return null;
        } catch (IOException ioe) {
            // if we are no longer running then quit here
            if(this.getRemedium().isRunning())
            log(ERROR, "Send message to "+message.getProperty(FIELD_ADDRESS)
                    +" has failed: "
                    + ioe.toString());
            // in either case, return null since we failed to deliver a message
            return null;
        }

        // convert the result onto a properties object
        Properties result = protocols.stringToProperties(msg);

        // we must ensure that we can transform the received msgbox onto a
        // properties field
        
        if (result == null) {
            log(ERROR,"Send message to "+message.getProperty(FIELD_ADDRESS)
                    +" has failed: Message result is not valid");
            return null;
        }


        //we need to have received a ticket, otherwise the msgbox is invalid
        if (!result.containsKey(FIELD_TICKET)) {
            debug("Invalid message since it didn't contained a ticket value in "
                    + result.toString());
            return null;
        }

        // output our results for debugging purposes
        debug("We've received ticket #" + result.getProperty(FIELD_TICKET));

        return result;
    }

    @Override
     public String getAddress(){
        try {
            return InetAddress.getLocalHost().getHostAddress()
                    + ":" + this.network_port;
        } catch (UnknownHostException ex) {
            Logger.getLogger(network_version1.class.getName()).log
                    (Level.SEVERE, null, ex);
        }
        return null;
    }

    //** Ask where a given ticket has came from */
    public String getTicketOrigin(String ticket){
        return networkThreadProcessInternal.tracker.getTicketOrigin(ticket);
    }


    private void log(int gender, String message) {
        remedium.log(
                "network",
                gender,
                message);
    }

    private void debug(String message) {
        if (debug) {
            log(DEBUG, message);
        }
    }

    @Override
    public void setRemedium(Remedium remedium, long assignedRemLock) {
        this.remedium = remedium;
        this.remLock = assignedRemLock;
        networkThreadProcessExternal.setRemedium(remedium, assignedRemLock);
    }

    @Override
    public Remedium getRemedium() {
        return remedium;
    }
}

class RunnerThreadProcessInternalRequests extends Thread {

    // turn this variable as true if you want debugging messages visible
    Boolean debug = false;
    private Remedium remedium = null;
    private HashMap<String, ArrayList<Properties>> networkMappings;
    // each ticket number holds the location where it should be checked
    public message_tracker tracker;

    public RunnerThreadProcessInternalRequests(Remedium remedium) {
        super();
        this.remedium = remedium;
        tracker = new message_tracker(remedium);
    }

    public RunnerThreadProcessInternalRequests() {
        super();
    }

    public void setRemedium(Remedium remedium) {
        this.remedium = remedium;
    }

    public Remedium getRemedium() {
        return this.remedium;
    }

    public HashMap<String, ArrayList<Properties>> getNetworkMappings() {
        return this.networkMappings;
    }

    public void setNetworkMappings(HashMap<String, ArrayList<Properties>> networkMappings) {
        this.networkMappings = networkMappings;
    }

    @Override
    public void run() {

        // each ticket number holds the location where it should be checked
        tracker = new message_tracker(remedium);

        int // how long should it take before starting the thread?
                waitDelay = 3,
                // how long should it take between each loop to check for new messages?
                loopInterval = 2;

//        try {
            // sleep this thread as specified before
            utils.time.wait(waitDelay);
            //this.sleep(waitDelay);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(RunnerThreadProcessInternalRequests.class.getName()).log(Level.SEVERE, null, ex);
//        }


        // repeat a long loop while our network component is running
        while (remedium.getNet().isRunning()) {
            /**
             * There are two distinct actions that take place during this loop:
             *  - Get messages marked to go outside and ship them out
             *  - Check the status of previously sent messages
             *
             * So, during the first part of this loop we will dispatch all
             * messages marked to go outside and then we will iterate through
             * all the entries of the msgbox tracker to see what has happened
             * to them in the meanwhile.
             */
            // Do the first part:
            // query the msgbox queue and get the messages marked to go outside
            ArrayList<Properties> ext = new ArrayList<Properties>();
            ext = remedium.getMQ().getExternal();

            debug("Looking for messages to be dispatched");

            if ( // only proceed if the list is not empty
                    (ext != null)
                    && (ext.size() > 0)) {
                // begin processing messages intended to go outside
                if(debug)
                    remedium.log("network",msg.INFO, "Dispatching "
                        + ext.size() + " message(s)");

                // loop all messages and send them away
                for (Properties msgbox : ext) {
                    // showcase what we will be sending on this trip
                    debug("Dispatching " + msgbox.toString());
                    // the holder of the dispatch reply
                    Properties reply = new Properties();
                    // send msgbox through thw wire
                    reply = remedium.getNet().send(msgbox);
                    // if the answer is null, something went wrong
                    if ((reply != null)
                            && (reply.containsKey(msg.FIELD_TICKET))) {
                        // start the real action
                        // add the entry to our tracker
                        tracker.addEntry(reply);
                        // delete this msgbox from our queue
                        remedium.getMQ().delete(msgbox.getProperty(msg.FIELD_ID));
                    }
                }
            }


            utils.time.wait(loopInterval);
//            try {
//                // do some sleep before the next loop
//                Thread.sleep(loopInterval);
//            } catch (InterruptedException ex) {
//                // for some reason an exception has occured
//                remedium.log("network", msgbox.ERROR, "Something went wrong, please check");
//                Logger.getLogger(RunnerThreadProcessInternalRequests.class.getName()).log(Level.SEVERE, null, ex);
//            }

            // Do the second part:
            // go through our tracked msgbox list and see what happened to them
            // at the other side of the wire

            // get all entries using the Iterator format
            Iterator<ticketType> ticketIterator = tracker.getEntries().values().iterator();

            // iterate all available entries
            while (ticketIterator.hasNext()) {
                // assign the next entry
                ticketType entry = ticketIterator.next();

                log(network.ROUTINE,
                        "Checking if ticket " + entry.ticket
                        + " is valid to dispatch");

               if( !entry.isValid()) // has the entry expired?
                       continue;

//TODO canAsk is not functional yet
//               if (!entry.canAsk()) // is it time to ask again for updates?
//                        continue;

               debug("Ticket "+entry.ticket+" is valid, asking for updates.");


                Properties reply = new Properties();
                // get our question ready to ship as a msgbox
                Properties question = entry.prepareTicket();
                // send msgbox through the wire
                reply = remedium.getNet().send(question);
                // if the answer is null then something went wrong
                if ((reply != null)
                        && (reply.containsKey(msg.FIELD_TICKET))) {

                    // get the STATUS reply from the other side of the wire
                    // TODO Should we use numbers or text descriptions on status?
                    int statusResult = Integer.parseInt(reply.getProperty(
                            msg.FIELD_STATUS));

                    debug("Current state of ticket "
                            + reply.getProperty(msg.FIELD_TICKET)
                            + ": "
                            + utils.text.translateStatus(statusResult));

                    // if the STATUS is equal to COMPLETED, put the msgbox
                    // back at our own queue for processing by other apps
                    if (statusResult == msg.COMPLETED)
                        // to delete the ticket from our list, we must really
                        // ensure that the msgbox is placed on the queue first
                        if (remedium.getMQ().send(reply)){
                        // all done with success, set this ticket as completed
                        tracker.setTicketStatus(entry.ticket,
                                msg.COMPLETED);
                        debug("---------- Ticket " + entry.ticket + " was marked as COMPLETED");
                        // remove the ticket from our tracking list
                        tracker.deleteTicket(entry.ticket);
                    }// status = completed
                }// reply contains ticker
            } // while ticketIterator


            //TODO Missing to add the clean up tool to the msgbox tracker

        }//while network is running
    }


    private void log(int gender, String message) {
        remedium.log(
                "network",
                gender,
                message);
    }

    private void debug(String message) {
        if (debug) {
            log(network.DEBUG, message);
        }
    }
}

class RunnerThreadProcessExternalRequests extends Thread {

    private Remedium remedium = null;
    private long remLock = 0;

    public RunnerThreadProcessExternalRequests(Remedium remedium,
            long assignedRemLock ) {
        super();
        this.remedium = remedium;
        this.remLock = assignedRemLock;
    }

    RunnerThreadProcessExternalRequests() {
        super();
    }

    public void setRemedium(Remedium remedium,
            long assignedRemLock ) {
        this.remedium = remedium;
        this.remLock = assignedRemLock;
    }

    public Remedium getRemedium() {
        return remedium;
    }

    @Override
    public void run() {
        try {
            // the location where our handling of replies is provided
            Container container = new network_simple_server(remedium);

            // connect to the specified port
            getRemedium().getNet().connection = new SocketConnection(container);
            // use a defined port address
            SocketAddress address = new InetSocketAddress(getRemedium().getNet().getPort());
            // Attempt to connect
            getRemedium().getNet().connection.connect(address);
            // output a msgbox if necessary
            if (getRemedium().getNet().debug) {
                getRemedium().log(
                        "network",
                        network.INFO,
                        "Listening for requests on port "
                        + getRemedium().getNet().getPort());
            }

            // update our status to "LISTENING"
            getRemedium().getNet().setStatus(network.LISTENING);
        } //run
        catch (IOException ex) {
            Logger.getLogger(RunnerThreadProcessExternalRequests.class.getName()).log(Level.SEVERE, null, ex);
            //update global status of our network system
            getRemedium().getNet().setStatus(network.ERROR);
            return;
        }
    }//run
}//thread

