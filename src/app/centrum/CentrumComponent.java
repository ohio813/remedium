/*
 * Centrum, from the Latin translation of “center”, is a role assigned to a
 * machine that is considered as the central point of a given network, let it
 * be a clan or castrum.
 * 
 * Actors inside a given network will resort to a specific centrum whenever in
 * need of enrolling for the first time on a clan or castrum. This act is also
 * valid for cases when the connection between two actors is broken and needs
 * to be reestablished.
 *
 */

package app.centrum;

import system.core.Component;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.mqueue.msg;
import utils.UpdateTracker;

/**
 *
 * @author Nuno Brito, 17th of May 2011 in Pittsburgh, USA.
 */
public class CentrumComponent extends Component implements msg{

    // settings
   static private int
            waitHello = 60, // how long between Hello updates?
            waitTriumvirUpdate = 60, // how long between triumvir client update?
            waitSentinelUpdate = 300; // tell Sentinel who is the triumvir?

   private String
           // define the initial locations for our roles
            whoIsOurCentrum = "localhost:10101",
            whoIsOurTriumvir = "localhost:10101";


   // objects
   private UpdateTracker
           updateTriumvirAboutClients,
           updateSentinelAboutTriumvir; // inform Sentinel about triumvir

   private CentrumManager
            clients = new CentrumManager(); // list of clients

   /** public constructor */
   public CentrumComponent(Remedium assignedInstance, Component assignedFather){
        // call the super component!
         super(assignedInstance, assignedFather);
     }


   /** Setup the initial services required for our Centrum */
   private void setupCentrum(){
       // create our time trackers
       updateSentinelAboutTriumvir = new UpdateTracker
               (this.getInstance().getTimeTracker());
       updateTriumvirAboutClients = new UpdateTracker
               (this.getInstance().getTimeTracker());

       // allow these tasks to run at each nn seconds
       updateSentinelAboutTriumvir.setSecondsBetweenAction(waitSentinelUpdate);
       updateTriumvirAboutClients.setSecondsBetweenAction(waitTriumvirUpdate);
   }

    @Override
    public void onStart() {

        // launch initial services
        setupCentrum();

        // no need to continue if we are not a Centrum server
        if(IamNotCentrum()){
            // set the update timer for a slower update
            setTime(waitHello);
            return;
        }
        // if we are centrum, let's begin
         setTime(1);

        // kickstart our own triumvir
        awakeTriumvir();

        // all done
        log(msg.INFO,"Centrum server has started");
    }

    @Override
    public void onRecover() {
        log(msg.INFO,"boinc");
    }


    /** Get the status of current clients */
    private String clientStatus(){

        // no need to show status if we are not centrum
        if(IamNotCentrum())
            return "";

        // Get the number of active clients
        long count = clients.size();

        if(count == 0)
            return "";// "No clients are registered";
         else
        if(count == 1)
           return clients.size() + " client is registered";
         else
           return clients.size() + " clients are registered";
    }


    /** Am I the centrum or not? If we aren't, return as true  */
    boolean IamNotCentrum(){
        return !this.getWebAddress().contains(whoIsOurCentrum);
    }

    /** Am I the triumvir or not? If we aren't, return as true  */
    boolean IamNotTriumvir(){
        return !this.getWebAddress().contains(whoIsOurTriumvir);
    }

    @Override
    public void onLoop() {

        // inform our Sentinel about who is the Triumvir
            sendToSentinelWhoIsTriumvir();

        if(IamNotCentrum()){
            // send the hello message
            sendHello(whoIsOurCentrum);
            return; // nothing else do do, exit this method
        }

    // I am the centrum, what else should I do?

        // clean out expired clients
        clients.CheckExpiration();
        // display the current client status
        String registered = clientStatus();

        if( registered.isEmpty() == false)
            log(ROUTINE, registered);

            // update our own triumvir with our registered clients
            sendToTriumvirAnUpdatedClientList(whoIsOurTriumvir);
            
    }

    @Override
    public void onStop() {
        log(INFO,"Stopping");
    }

    @Override
    public String getTitle() {
        return "centrum";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
       String reply = "The centrum acts as central server machine that"
               + " coordinates the client machines at remedium networks."
               + "<br>"
               + "It is disabled on this version of remedium.";
        
        
        return reply;
    }



    /** Send a message to activate our triumvir */
    private void awakeTriumvir(){

        log(INFO, "Activating our triumvir");

            final Properties message = new Properties();
            // the fields that we need to place here
            message.setProperty(FIELD_FROM, this.getCanonicalName());
            message.setProperty(FIELD_TO, triumvir);
            message.setProperty(FIELD_TASK, "activate");
            message.setProperty(FIELD_MESSAGE,"true");
            // dispatch the message out the queue
            send(message);
    }


    /**
     Send a Hello message to the centrum server. This
     service occurs at a given period time
     */
    public void sendHello(String who){

            log(ROUTINE, "Sending hello to centrum ");

            final Properties message = new Properties();
            // the fields that we need to place here
            message.setProperty(FIELD_FROM, this.getCanonicalName());
            message.setProperty(FIELD_ID_SERIAL, this.getIDserial());
            message.setProperty(FIELD_TO, this.getCanonicalName());
            message.setProperty(FIELD_ADDRESS, who);
            message.setProperty(FIELD_TASK, "hello");
            message.setProperty(FIELD_MESSAGE,
                    this.getInstance().getNet().getAddress());
            // dispatch the message out the queue
            send(message);
            //log(DEBUG, "Hello: " + message.toString());
    }

  /** Send to our Sentinel the location of the Triumvir */
  public void sendToSentinelWhoIsTriumvir(){

        if(updateSentinelAboutTriumvir.isAllowed()== false)
            return; // not time to send an update

            log(ROUTINE, "Informing Sentinel about the Triumvir location");

            Properties message = new Properties();
            // the fields that send our message on the expected direction
            message.setProperty(FIELD_FROM, this.getCanonicalName());
            message.setProperty(FIELD_TO, sentinel);
            message.setProperty(FIELD_TASK, "updateTriumvirLocation");
            message.setProperty(FIELD_MESSAGE, whoIsOurTriumvir);
            // dispatch the message out the queue
            send(message);
    }


    /**
     * Sends an update list of clients for each triumvir.
     * At this point we only use one single triumvir, in the future it shall
     * be possible to provide load balancing using this method.
     */
    public void sendToTriumvirAnUpdatedClientList(String who){
            
            Properties message = new Properties();

            // export all clients onto this message (no limit defined)
            clients.exportMessage(message);
            String result = message.getProperty(FIELD_MESSAGE);

            // if there are no registered clients, no need to bulk
            if(result.isEmpty())
                return;
            // only send updates at a given set of time
            if(updateTriumvirAboutClients.isAllowed() == false)
                return; // Not time yet to update the Triumvir

            // the fields that send our message on the expected direction
            message.setProperty(FIELD_FROM, this.getCanonicalName());
            message.setProperty(FIELD_TO, triumvir);
            message.setProperty(FIELD_TASK, "updateClientList");

            // if the triumvir is at some other machine, add the remote address
            if(IamNotTriumvir())
                message.setProperty(FIELD_ADDRESS, whoIsOurTriumvir);

            // dispatch the message out the queue
            log(ROUTINE, "Informing Triumvir at " + who +" about his clients");
            send(message);
    }


    /** 
     * Process the "Hello" message from client centrums
     */
    public void digest_hello(Properties message) {


        log(ROUTINE,"Hello from " + message.getProperty(FIELD_MESSAGE));
        // pre flight
        CentrumClient client = new CentrumClient();

        // add our details about this client
        client.setUrl(message.getProperty(FIELD_MESSAGE));
        client.setUpdated(this.getTime());

        // fill the score details
        client.setBandwidth(10);
        client.setCpu(10);
        client.setDisk(10);
        client.setUptime(10);

        // add the client to our list
        clients.add(client);
    }

}
