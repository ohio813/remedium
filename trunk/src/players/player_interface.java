/*
 * This interface defines the player_interface system where we are capable of managing
 * different types of players using a common implementation.
 *
 * This interface will initially be implemented in HSQL and can later be coded
 * under any other technologies as long as they follow our description.
 *
 * There are at least three types of players that will act on our system:
 *      - users (at their workstations)
 *      - groups (clans and castrum that belong to a given group)
 *      - forum (a federation of groups over the internet or WAN structures)
 */

package players;

import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import system.msg;

/**
 *
 * @author Nuno Brito
 */
public interface player_interface extends msg{

    ///////-- Static definitions
   

    public String
    // fields that are common
            FIELD_CONTACT = "pCONTACT", // does it indicates a contact point?
            FIELD_GROUP = "pGROUP"; // to which group does this player belong?
          

    ///////-- System wide methods

     // start our player system
    public boolean start(Properties parameters);
    public boolean start(String uniqueName);// use default settings


    public void setRemedium(Remedium remedium);
    public Remedium getRemedium();

    // is the system running?
    public boolean isRunning();

    // stop our player system
    public boolean stop(Properties parameters);
    public boolean stop();// use default settings


    ///////-- Player management methods

    // adds a player_interface on the system
    public boolean register(Properties parameters);
    public boolean register(String playerName);// use default settings

    // remove a player_interface from the system
    public boolean remove(Properties parameters);
    public boolean remove(String playerName); // use default settings

    // update a given player_interface details
    public boolean update(Properties parameters);

    // does a player_interface exist?
    public boolean exists(Properties parameters);
    public boolean exists(String playerName); // use default settings

    // get the data from a given player_interface
    public Properties get(Properties parameters);
    public Properties get(String playerName); // use default settings

    // get the data from all players on the system
    public ArrayList<Properties> getAll(Properties parameters);
    public ArrayList<Properties> getAll(); // use default settings


}
