/*
 * This implementation of the message queue is based on Flat Files. The previous
 * implementation was based on HSQL and the memory/usability of this technology
 * was deemed as not appropriate for our usage.
 *
 * The present flat file implementation should provide independence from a SQL
 * based database.
 */

package system.mqueue;

import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import system.container.Container;
import system.log.LogMessage;
import system.net.protocols;

/**
 *
 * @author Nuno Brito, 11th of July 2011 in Darmstadt, Germany.
 */
public class MessageQueueFlatFile implements messageQueueInterface{

    // our assigned instance of Remedium
    private Remedium instance = null;

    // have we started or not?
    private boolean started = false;
    // where we store all our data
    private Container container;
    // the title of our database
    private String title = "mq";
    // the fields of our database
    private String[] fields = new String[]{
            msg.FIELD_ID,
            msg.FIELD_TO,
            msg.FIELD_FROM,
            msg.FIELD_CREATED,
            msg.FIELD_TICKET,
            msg.FIELD_STATUS,
            msg.FIELD_ADDRESS,
            msg.FIELD_PARAMETERS
            };


    /** Public constructor */
    public MessageQueueFlatFile(final Remedium assignedInstance){
        this.instance = assignedInstance;
    }

    /** Kick start our container*/
    public boolean start() {
        // create the log object
        LogMessage result = new LogMessage();
        // start the container itself
        container = new Container(title, fields,
            this.instance.getStorage(), result);
        // delete all the previous records
        container.deleteKnowledgeFiles();
        // have we started as intended?
        if(result.getResult() != msg.ERROR)
            started = true;
        // return our results
        return started;
    }

    /** Has this class started or not? */
    public boolean hasStarted() {
        return started;
    }

    public boolean pause() {
        // no reaction
        return true;
    }

    public boolean resume() {
        // no reaction
        return true;
    }

    public boolean stop() {
        // no reaction
        return true;
    }

     /** Place a message on our queue */
    public boolean send(final Properties data) {
        // pre-flight check, we need to ensure that this msg is valid
        if (  !data.containsKey(msg.FIELD_TO)
           || !data.containsKey(msg.FIELD_FROM)
                ) {
            log(msg.ERROR, "Send operation failed: '%1' is not valid.",
                    data.toString());
            return false;
        }

        // add our own timestamp to the data record
        long timestamp = instance.getTime();
        data.put(msg.FIELD_CREATED, Long.toString(timestamp));

        // include all other properties of this object inside the PARAMETERS field
        // we convert the Properties object onto a simple string that is later reconstructed
        data.setProperty(msg.FIELD_PARAMETERS,
                protocols.propertiesToString(data));

        // the holder of our message
        final String[] message = new String[]{
            ""+System.currentTimeMillis() 
                    + "-" // random number is needed for providing unique ID's
                    + utils.math.RandomInteger(1, 999),
            data.getProperty(msg.FIELD_TO),
            data.getProperty(msg.FIELD_FROM),
            data.getProperty(msg.FIELD_CREATED),
            data.getProperty(msg.FIELD_TICKET, ""),
            // if not status is specified then set status to PENDING
            data.getProperty(msg.FIELD_STATUS,
                    utils.text.translateStatus(msg.PENDING)),
            data.getProperty(msg.FIELD_ADDRESS, ""),
            data.getProperty(msg.FIELD_PARAMETERS,"")
        };

        // write the message down
        boolean result = container.write(message);
        // output a message if sucessful
        if(result == true)
            log(msg.COMPLETED,"Send operation: Placed on queue message %1",
                    data.toString());
        // all done
       return result;
    }

   /** Get all the messages addressed to someone */
    public ArrayList<Properties> get(String messageRecipient) {
        return container.read(msg.FIELD_TO, messageRecipient);
    }

    /** get a message associated with a given ticket number*/
    public ArrayList<Properties> getTicket(String ticket) {
         return container.read(msg.FIELD_TICKET, ticket);
    }

    public ArrayList<Properties> getExternal() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return null;
    }

   /** Delete a message using the ID field */
    public Boolean delete(String messageID) {
        boolean result = container.delete(msg.FIELD_ID, messageID);
        return result;
    }

    /** Deletes a message with a given ticket ID */
    public Boolean deleteTicket(String ticketID) {
        boolean result = container.delete(msg.FIELD_TICKET, ticketID);
        return result;
    }

    /** Set the new remedium instance */
    public void setRemedium(Remedium remedium) {
        // do nothing as we don't allow to change after it has been instantiated
        return;
    }

    /** Get our remedium instance */
    public Remedium getRemedium() {
       return this.instance;
    }

    /** Log our occurences */
    private void log(int gender, String message, String... args) {
        // log this occurence
        container.getLog().add(title, gender, message, args);
    }

    public LogMessage getLog() {
        return container.getLog();
    }

}
