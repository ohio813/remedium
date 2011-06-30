/*
 *
Triumvir is the direct Latin translation for “three men” and is used to
 designate a group of three persons (in these case machines) that perform an
 important role inside the group where they are integrated.

A triumvir is the main communication bridge between each Sentinel and the rest
 of the network.

This role is responsible for ensuring that all cliens assigned to him get
 timely knowledge about the status of the clan or castrum. It is also
 responsible for warning other triumvirs about status of his own cliens.

In case of fail or mistrust, another triumvir is elected from the list of
 Sentinel under his charge. The details for elections and interactions with
 other roles are described in the section entitled “Triumvirate” of the
 Remedium Structure documentation.
 */

package app.sentinel;

import system.core.Component;
import java.util.HashMap;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 19th of May 2011 in Pittsburgh, USA.
 */
public class TriumvirComponent extends Component{

    // Are assigned with the role of Triumvir server or not?
    private Boolean 
            isTriumvir = false;

    private long
            secondsBetweenUpdateRequests = 60; // periodicity to grab updates
    
    private String[] // our list of clients
            clients = new String[]{};

    private HashMap<String, String> // when was each one last updated?
            clientsUpdate = new HashMap();


    public TriumvirComponent(Remedium assignedInstance,
            Component assignedFather, IndexerComponent assignedIndexer){
        // call the super component!
         super(assignedInstance, assignedFather);

         // where we keep all our storage
         //indexer = assignedIndexer;
     }

    @Override
    public void onStart() {
        log(INFO,"Starting fresh");
    }

    @Override
    public void onRecover() {
    }

    @Override
    public void onLoop() {
        // if we are not triumvir, let's exit
        if(isTriumvir == false)
            return;


        // we are triumvir, let's do some neat stuff now..

        // Update all our clients with the data that we have
        // Request clients to upload their gathered data
        doRequestUpdates();


    }

    @Override
    public void onStop() {
        log(INFO,"Stopping");
    }

    @Override
    public String getTitle() {
        return "triumvir";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return getTitle();
    }


    /** Should we set as an active triumvir server or not? */
    private void activateServer(Boolean newState){
       log(ROUTINE, "Setting server state as '"
                    + newState.toString() + "'");
        // change the state
        isTriumvir = newState;
    }

    /**
     * This task updates the list of clients assigned to this triumvir.
     * It is intended to be dispatched by the Centrum server, meaning that it
     * can be received from the local centrum or remote centrum.
     */
    public void digest_updateClientList(Properties message) {
        // import a list of clients from a received message
        
        String clientsList =  message.getProperty(FIELD_MESSAGE);

        log(ROUTINE, "Received an updated list of clients: '"
                + clientsList +"'");

        // import our list of clients directly
        clients = clientsList.split(";");

        clients.getClass(); // dummy holder to fool find bugs, remove one day
    }



    /**
     * Process the server "activate" message from centrum
     */
    public void digest_activate(Properties message) {
        // We can only accept messages from a Centrum
        String fromWho = message.getProperty(FIELD_FROM);

      
        // filter non-authorized messages
        if(fromWho.equalsIgnoreCase(centrum)==false){
            log(ERROR,"Can only accept messages from a Centrum");
            return;
        }

        // should we set ourselves as triumvir or not?
        activateServer(
                message.getProperty(FIELD_MESSAGE).equalsIgnoreCase("true")
                );
    }



    /** Ask for knowledge updates to all registered clients */
    /** Ensure that each registered client has the same knowledge as we do. */
      private void doRequestUpdates(){
    /**
     * This is a tricky procedure.
     *  - As pre-requisite, we forbid this instance from providing updates while
     *    the triumvir update is occuring.
     *  - First we need to know on each client when did the last update occurred
     *  - Second, grab the relevant update information for each client
     *  - Third, individually dispatch this information and confirm reception
     *  - Fourth, if no more updates are available for each client, all done!
     */

        // no need to proceed if there are no clients to bother
        if(clients.length == 0)
            return;

        // forbid any update requests
        

        // iterate all known clients and ask them for updates
        for(String client : clients){
            sendMeUpdates(client);
        }
    }

   /**
     * We want to get updates from a given instance. Triumvir addresses this
     * message to a client that will send him any updates if available.
     */
    private void sendMeUpdates(String who){

        // if the client doesn't exist, place it here as 0
        if(clientsUpdate.containsKey(who)==false)
            clientsUpdate.put(who, "0");
        
        // get the value for this client
        long lastUpdated = Long.parseLong(clientsUpdate.get(who));

        if( // restrict the update request to a given amount of seconds
            (
              (lastUpdated + (secondsBetweenUpdateRequests * 1000))
              > this.getTime()
              )
                 &&
                (lastUpdated > 0)
                 )// not time yet, just quit here
            return;


        // update timer
        clientsUpdate.put(who, "" + this.getTime() );


            log(DEBUG, "Requesting knowledge update to Sentinel at " + who);

//            Properties message = new Properties();
//            // the fields that send our message on the expected direction
//            message.setProperty(FIELD_FROM, this.getCanonicalName());
//            message.setProperty(FIELD_TO, sentinel);
//            message.setProperty(FIELD_ORIGIN,
//                    this.getInstance().getNet().getAddress());
//            message.setProperty(FIELD_TASK, "getTriumvirUpdates");
//            message.setProperty(FIELD_ADDRESS, who);
//            message.setProperty(FIELD_MESSAGE, "Can I have updates? Please?");
//            // dispatch the message out the queue
//            send(message);
    }

      
}
