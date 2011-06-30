/*
 * This interface defines the ranking_interface system where we are capable of managing
 * different types of players using a common implementation.
 *
 * This interface will initially be implemented in HSQL and can later be coded
 * under any other technologies as long as they follow our description.
 *
 * There are some types of rankings that will act on our system:
 *      - File content hash ranking
 *      - File name hash ranking
 *      - File path hash ranking
 *      - User ranking
 *      - Group ranking
 */

package players;

import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author Nuno Brito
 */
public interface ranking_interface {

    ///////-- Static definitions

    public String
    // fields that are common
            FIELD_ID = "id",
            FIELD_NAME = "pNAME", // What is the unique ID for this process?
            FIELD_STATUS = "pSTATUS", // ACTIVE, SUSPENDED, INACTIVE, BANNED
            FIELD_CREATED = "pTIMECREATED", // when was it last updated?
            FIELD_UPDATED = "pTIMEUPDATED", // when was it last updated?
            FIELD_PARAMETERS = "pPARAMETERS"; // the message payload
    

    ///////-- System wide methods

     // start our player system
    public boolean start(Properties parameters);
    public boolean start(String uniqueName);// use default settings

    // is the system running?
    public boolean isRunning();

    // stop our player system
    public boolean stop(Properties parameters);
    public boolean stop();// use default settings


    ///////-- Player management methods

    // adds a ranking_interface on the system
    public boolean register(Properties parameters);
    public boolean register(String playerName);// use default settings

    // remove a ranking_interface from the system
    public boolean remove(Properties parameters);
    public boolean remove(String playerName); // use default settings

    // update a given ranking_interface details
    public boolean update(Properties parameters);
    public boolean update(String playerName); // use default settings

    // does a ranking_interface exist?
    public boolean exists(Properties parameters);
    public boolean exists(String playerName); // use default settings

    // get the data from a given ranking_interface
    public Properties get(Properties parameters);
    public Properties get(String playerName); // use default settings

    // get the data from all players on the system
    public ArrayList<Properties> getAll(Properties parameters);
    public ArrayList<Properties> getAll(); // use default settings


}
