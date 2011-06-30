/*
 * We define on this interface the minimum set of functionality that all
 * implementations of our message queue must implement.
 *
 * This has the benefit that all changes we add on the interface, will need
 * to forcefully be reflected down on each implementation of the code.
 * 
 */
package system;

import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito
 */
public interface message_queue_interface {

    public static final String // table name inside our databasde
            TABLE_MESSAGES = "messages";
            // fields for this table
            
    
    // start the message queue server
    public boolean start();

    // have we already started before?
    public boolean hasStarted();

    // hold functionining for a while
    public boolean pause();

    // resume our activity
    public boolean resume();

    // stop the message queue, flush all messages
    public boolean stop();

    // flush down all messages on the queue
    //boolean flush();
    // returns an array of messages from the queue for a given recipient
    public ArrayList<Properties> get(String messageRecipient);

    // after reading a message, it can be disposed using the message ID
    public Boolean delete(String messageID);
    //TODO Missing to add support for multiple delete
    // public Boolean delete(ArrayList<int>);

    // send a message on the queue
    public boolean send(Properties data);

    // get all messages that contain a given Ticket identifier
    public ArrayList<Properties> getTicket(String ticket);
    
    // get all messages marked to be shipped outisde this instance
    public ArrayList<Properties> getExternal();

    public void setRemedium(Remedium remedium);

    public Remedium getRemedium();
}
