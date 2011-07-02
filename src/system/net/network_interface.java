/*
 * This is the network interface, we need it to interact with other clients and
 * machines across the network.
 *
 * On this interface we define only the basic functionality contract that must
 * be provided by all implementations based on our system.
 */
/*
 * This class expects a thread to be launched that will receive a given msg
 * that is marked for going outside of our remedium instance and then track the
 * result.
 *
 * For example, when instance A sends a msg to instance B then B will provide
 * a ticket number with the msg and also provide the status of the requested
 * action.
 *
 * If the status of this msg is PENDING, then this thread running on
 * instance A is responsible for asking instance at periodic time intervals
 * how the ticket is going.
 *
 * At each occasion, the reply from B will provide the status until the status
 * result is COMPLETED, TIMEOUT or FAILED. In case of COMPLETED, we place the
 * provided msg on the Queue. After COMPLETED, TIMEOUT or FAILED we will
 * just remove it from our list of messages to track.
 *
 */
package system.net;

import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import system.msg;

/**
 *
 * @author Nuno Brito
 */
public interface network_interface extends msg{

    ///////-- Static definitions
    // if we don't know who to contact, call ourselves
    public final static String HOST_DEFAULT = "localhost",
             // we use a high number, a meme for binary transmissions
            PORT_DEFAULT = "10101";
    
    //Maximum size of a msg is set to 16kb.
    public static final int MAX_MESSAGE_SIZE = 1024 * 16;

    ///////-- System wide methods
    /** start our network interface
     *  Supported parameters:
     *         - PORT - sets the port to use, by default is PORT_DEFAULT
     *         - PASSWORD - sets a password to validate incoming requests
     */
    public boolean start(Properties parameters);

    public boolean start();// use default settings

    // is the system running?
    public boolean isRunning();

    public boolean isListening();

    public boolean hasStarted();

    // get and set the default port that is used
    public int getPort();

    boolean setPort(int newPort);

    /**
     * This method allows changing the internal settings of the network system
     * while it is already running.
     * Supported parameters:
     *      - PASSWORD - sets a new operating password, requires the old to 
     *                   be provided as OLD_PASSWORD to allow this modification
     *      - MAX_CLIENT - sets a maximum value of messages that may be placed
     *                     on the queue by a  given client
     *      - MAX - defines the maximum number of allowed messages to be placed
     *              on the system queue by outside clients.
     *      - MAX_MESSAGES - sets the maximum number of messages that can be
     *                     be accepted on each post made by external clients
     */
    public boolean update(Properties parameters);

    // stop our system
    public boolean stop(Properties parameters);

    public boolean stop();// use default settings

    ///////-- Management methods
    /**
     * Send  a msg to the msg queue
     * Accepted parameters:
     *      - HOST - location of remote server, either IP address or URL
     *      - PORT - which port to use (if optional will use default)
     *      - PASSWORD - the validation password to access the server
     *
     * Accepted msg options:
     *      - FROM - idenfication of sender
     *      - TO - To whom it is destined inside the server
     *
     * Properties that are returned:
     *      - STATUS - indicates the result from the remote server as PENDIN,
     *                 EXECUTED and so on
     *      - TICKET - returns the ticket number at the msg queue
     *
     *
     */
    public Properties send(Properties message);

    /**
     * An outside client requests to get all messages destined to him.
     * We will query our msg queue and provide back a reply.
     *
     * Typically, if we are A and a client B asks for some information, his
     * request is placed on the queue instead of being replied right away.
     *
     * Then, client B will periodically pool A to see if there are replies to
     * his request.
     *
     * Once they are available, A will deliver the results to B and removes these
     * messages from the msg queue on A's side.
     *
     * If the messages are not requested by B, they are deleted from the queue
     * after a given period of time.
     *
     * -----------------
     *
     * The first item on the Properties array is providing valuable information
     * about the reply from the remote server.
     * Types of information available:
     *      - STATUS - Indicates if the request is still on the queue or not
     *
     */
    public ArrayList<Properties> get(Properties parameters);

    public ArrayList<Properties> get(String clientName); // use default settings

    public void setRemedium(Remedium remedium, long assignedRemLock);

    public Remedium getRemedium();

    // This is used by the Roles, when registering to the Centrum
    public String getAddress();

}
