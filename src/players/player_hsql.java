/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package players;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito
 */
public class player_hsql implements player_interface{

         String
    // table name inside our databasde
            PLAYER_TABLE = "player",
            PLAYER_TYPE  = "player";

    // are we debugging this class?
    private boolean debug = true;
    // what is the status of our system?

    // I belong to a remedium
    private Remedium remedium = null;

    private int status = INACTIVE;

    /**
     * Register a new user on our system. It will fail if the user was not
     * added onto the system for some reason.
     *      - FIELD_NAME - the name of the player to register
     */
    @Override
    public boolean register(Properties parameters) {
        // pre-flight check, we need to ensure that this request is valid
        String playerName = parameters.getProperty(FIELD_NAME, "");

        if(
            (playerName.equalsIgnoreCase("")) || // we need a name here
            (getStatus()!=RUNNING) // we need to be running
                ){
            debug("Failed to register this player");
            return false;
        }

         ///////////////////////////////////////////////////////////////////////
         // Check if this process already exists
        if (exists(playerName)){
            debug("Player "+playerName+" is already listed.");
            return false;}


        // add the details pertaining this process
        parameters.put(FIELD_NAME, playerName);
        parameters.put(FIELD_STATUS, "ACTIVE");
        // add our own timestamp to the data record
        long timestamp = System.nanoTime();
        parameters.put(FIELD_CREATED, Long.toString(timestamp));
        parameters.put(FIELD_UPDATED, Long.toString(timestamp));

        // place the data record on the queue
            Boolean result = getRemedium().getDB().update(
                "INSERT INTO "+PLAYER_TABLE+"("+FIELD_NAME+","
                                             + FIELD_STATUS+","
                                             + FIELD_CREATED+","
                                             + FIELD_UPDATED+","
                                             + FIELD_CONTACT+","
                                             + FIELD_GROUP+","
                                             + FIELD_RANKING+","
                                             + FIELD_PARAMETERS
                                             + ") VALUES("
                             + "'"+parameters.getProperty(FIELD_NAME,"")+"', "
                             + "'"+parameters.getProperty(FIELD_STATUS,"")+"', "
                             + "'"+parameters.getProperty(FIELD_CREATED,"")+"', "
                             + "'"+parameters.getProperty(FIELD_UPDATED,"")+"', "
                             + "'"+parameters.getProperty(FIELD_CONTACT,"")+"', "
                             + "'"+parameters.getProperty(FIELD_GROUP,"")+"', "
                             + "'"+parameters.getProperty(FIELD_RANKING,"")+"', "
                             + "'"+parameters.getProperty(FIELD_PARAMETERS,"")+"') "
                                             ).equalsIgnoreCase(TRUE);

         if(result)debug("Registered " + parameters.get(player.FIELD_NAME));
         else debug("Failed to register " + parameters.get(player.FIELD_NAME));
            
        return result;
    }

    /**
     * Generic action for the register command, uses system defaults.
     */
    public boolean register(String playerName) {
        Properties data = new Properties();
        data.put(player.FIELD_NAME, playerName);
        return register(data);
    }

    /**
     * Remove a given player from our system. It will output this operation
     * as false in case any of the required operations goes wrong.
     */
    public boolean remove(Properties parameters) {
        String playerName = parameters.getProperty(FIELD_NAME, "");

        if(!exists(playerName)){
            debug(PLAYER_TYPE+" "+playerName + " does not exist, cannot remove");
            return false;}

//        String expression = "DELETE FROM " + PLAYER_TABLE + " WHERE "
//                          + FIELD_NAME + " = " + playerName;

                // prepare our query expression
        String expression = "DELETE FROM "+PLAYER_TABLE+" WHERE "
                          + FIELD_NAME + " = '" + playerName + "'";
        debug(expression);
        Boolean result = getRemedium().getDB().update
                (expression).equalsIgnoreCase(TRUE);
        return result;
    }

    /**
     * Generic command for the remove action.
     */
    public boolean remove(String playerName) {
        Properties data = new Properties();
        data.put(player.FIELD_NAME, playerName);
        return remove(data);
    }


    /**
     * Add news data to an existent player.
     * Supported parameters:
     *      -
     */
    @Override
    public boolean update(Properties parameters) {
         // pre-flight check, we need to ensure that this message is valid
        String playerName = parameters.getProperty(FIELD_NAME, "");
        if(
            (playerName.equalsIgnoreCase("")) || // we need a name here
            (getStatus()!=RUNNING) // we need to be running
                ){
            debug("Update request not possible to deliver");
            return false;
        }

        // The process needs to already exist
        if (!exists(playerName)){
            debug(PLAYER_TYPE+" "+playerName+" does not exist");
            return false;}

        // let's get the process details
        Properties process = new Properties();
        process = get(playerName);

        // now that we have the process details, let's update them.
        // we update status if one is provided on "data" or use "ACTIVE" as default

        if(parameters.containsKey(FIELD_STATUS))
        process.put(FIELD_STATUS, parameters.getProperty(FIELD_STATUS));

        if(parameters.containsKey(FIELD_CONTACT))
        process.put(FIELD_CONTACT, parameters.getProperty(FIELD_CONTACT));

        if(parameters.containsKey(FIELD_GROUP))
        process.put(FIELD_GROUP, parameters.getProperty(FIELD_GROUP));

        if(parameters.containsKey(FIELD_RANKING))
        process.put(FIELD_RANKING, parameters.getProperty(FIELD_RANKING));

        if(parameters.containsKey(FIELD_PARAMETERS))
        process.put(FIELD_PARAMETERS, parameters.getProperty(FIELD_PARAMETERS));

        // we refresh the update timestamp of the data record
        long timestamp = System.nanoTime();
        String time = Long.toString(timestamp);
        process.put(FIELD_UPDATED, time);

        // update the data record on the process manager
        String expression = "UPDATE "+PLAYER_TABLE+" SET "
                + FIELD_STATUS + " = '" + process.getProperty(FIELD_STATUS)+"', "
                + FIELD_UPDATED + " = '" + process.getProperty(FIELD_UPDATED)+"', "
                + FIELD_CONTACT + " = '" + process.getProperty(FIELD_CONTACT)+"', "
                + FIELD_GROUP + " = '" + process.getProperty(FIELD_GROUP)+"', "
                + FIELD_RANKING + " = '" + process.getProperty(FIELD_RANKING)+"', "
                + FIELD_PARAMETERS + " = '" + process.getProperty(FIELD_PARAMETERS)+"' "

                + "WHERE " + FIELD_NAME + " = '" + playerName + "'";

            Boolean result = getRemedium().getDB().update
                    (expression).equalsIgnoreCase(TRUE);

          debug("Updated "+playerName+" using: "+expression);
        return result;
    }

  

    /**
     * This command will check if a given player is registered or not.
     * Accepted parameters:
     *      - FIELD_NAME - Set the name you want to check if it exists.
     */
    public boolean exists(Properties parameters) {
        Properties result = new Properties();
            result = get(parameters);
        return result != null;
    }

    /**
     * Generic caller for the exists() action
     */
    public boolean exists(String playerName) {
        Properties data = new Properties();
        data.put(player.FIELD_NAME, playerName);
        return exists(data);
    }

    /**
     * Get all the properties associated with a given member, return null if
     * not found or something wrong happens.
     *
     * Accepted parameters:
     *      - FIELD_NAME - The name of the user
     */
    public Properties get(Properties parameters) {
        // pre-flight check, we need to ensure that this request is valid
        String playerName = parameters.getProperty(FIELD_NAME, "");
        if( playerName.equalsIgnoreCase("")
         || getStatus()!=RUNNING // we need to be running
           ){ // exit since we don't have enough conditions
            debug("Something went wrong on the GET() pre-flight check");
            return null;}

        // initialize our variable.
        Properties answer = new Properties();

        // prepare our query expression
        String expression = "SELECT * FROM "+PLAYER_TABLE+" WHERE "
                          + FIELD_NAME + " = '" + playerName + "'";

        //run our query
                try {
            Statement st = null;
            ResultSet rs = null;
            st = getRemedium().getDB().conn.createStatement(); // statement objects can be reused with
            rs = st.executeQuery(expression); // run the query

            // I tried using getFetchSize but it was always outputting a zero
            // value, as exemplified below:
            //System.out.println("--"+rs.getFetchSize());
            // So, we need to use hasNone to return a proper null value
            // when no records were found.
            Boolean hasNone = false;

            // get all the data from each column on the record, iterate them all
            for (; rs.next();) {

                // clear the variable holder
                answer = new Properties();
                // get all our message fields
                answer.put(FIELD_ID, rs.getObject(1).toString());
                answer.put(FIELD_NAME, rs.getObject(2).toString());
                answer.put(FIELD_STATUS, rs.getObject(3).toString());
                answer.put(FIELD_CREATED, rs.getObject(4).toString());
                answer.put(FIELD_UPDATED, rs.getObject(5).toString());
                answer.put(FIELD_CONTACT, rs.getObject(6).toString());
                answer.put(FIELD_GROUP, rs.getObject(7).toString());
                answer.put(FIELD_RANKING, rs.getObject(8).toString());
                answer.put(FIELD_PARAMETERS, rs.getObject(9).toString());

                hasNone = true;
            }

            // if the result set came back empty, return null
            if(!hasNone) {
                return null;}

            // if the application has stopped, return null
//            if(answer.getProperty(FIELD_STATUS).equalsIgnoreCase(STOPPED)){
//                debug(processName + " is stopped");
//                return null;}

            st.close(); // NOTE!! if you stop a statement the associated ResultSet is lost
           } catch (SQLException ex) {
            return null;
        }

         // return our results
        return answer;
    }

    /**
     * Generic call of the get() function that provides data about a given user
     */
    public Properties get(String playerName) {
        Properties data = new Properties();
        data.put(player.FIELD_NAME, playerName);
        return get(data);
    }




    /**
     * Generic caller of the getAll() method that retrieves all available
     * players inside a given system
     */
    public ArrayList<Properties> getAll() {
        Properties data = new Properties();
        return getAll(data);
    }

    /**
     * This method returns an array of all players registered on the system,
     * beware that on this list are most than just ACTIVE members, even those
     * under the status of SUSPENDED, INACTIVE and forth are also present.
     *
     * Accepted parameters:
     *      -- none -- we have no need (yet) for adding parameters
     */
    public ArrayList<Properties> getAll(Properties parameters) {
        // pre-flight check, we need to ensure that this request is valid
        if( getStatus()!=2 // we need to be running
           ) // exit since we don't have enough conditions
            return null;

        // initialize our variable.
        ArrayList<Properties> answer = new ArrayList<Properties>();

        // prepare our query expression
        String expression = "SELECT * FROM "+PLAYER_TABLE;

        //run our query
                try {
            Statement st = null;
            ResultSet rs = null;
            st = getRemedium().getDB().conn.createStatement(); // statement objects can be reused with
            rs = st.executeQuery(expression); // run the query

            // prepare to output our answer using the provided results
            //ResultSetMetaData meta = rs.getMetaData();
            Properties p = null;

            for (; rs.next();) {

                // clear the variable holder
                p = new Properties();

                // get all our message fields
                p.put(FIELD_ID, rs.getObject(1).toString());
                p.put(FIELD_NAME, rs.getObject(2).toString());
                p.put(FIELD_STATUS, rs.getObject(3).toString());
                p.put(FIELD_CREATED, rs.getObject(4).toString());
                p.put(FIELD_UPDATED, rs.getObject(5).toString());
                p.put(FIELD_CONTACT, rs.getObject(6).toString());
                p.put(FIELD_GROUP, rs.getObject(7).toString());
                p.put(FIELD_RANKING, rs.getObject(8).toString());
                p.put(FIELD_PARAMETERS, rs.getObject(9).toString());

                // add this record to our answer
                answer.add(p);
            }

            st.close(); // NOTE!! if you stop a statement the associated ResultSet is lost
           } catch (SQLException ex) {
            return null;
        }

         // return our results
        return answer;
    }





    /**
     * Generic call to the start command using generic settings
     */
    public boolean start(String uniqueName) {
        Properties data = new Properties();
        data.put("TYPE", uniqueName);
        return start(data);
    }

    /**
     * Accepted parameters:
     *      - FLUSH - drop all data and start from a fresh table
     *      - TYPE - defines the tag (used for table naming), for example,
     *              user, group, clan, castrum, forum, etc
     */
    public boolean start(Properties parameters) {
        // check if database is running
        // check if message queue is running
        // register on the process manager
        //
        boolean result = true;

        // If we are running, no restart should be allowed.
        if(getStatus() != INACTIVE){ return false;}

        // do we need to start up the database system by ourselves?
        if (!getRemedium().getDB().hasStarted()) result = getRemedium().getDB().start();
        if (!result) {
            log("error","Failed to start database system");
            return result;
        }
        // set our status as "RUNNING"
        setStatus(RUNNING);

        // if requested, change the default table name
        if(parameters.containsKey("TYPE"))
            setType(parameters.getProperty("TYPE", PLAYER_TYPE));

        // if requested, flush the table to start from scratch
        if(parameters.containsKey("FLUSH"))
            flush();
        else // only
            setup();

         if (result)
             debug(PLAYER_TYPE+" is available");

        return result;
    }

    /**
     * This smart method will ensure that we can tailor each
     */
    public void setType(String playerType){
        // this is a weak filtering, we might need better like RegEx
        playerType.replace(" ", "_");
        PLAYER_TYPE = playerType;
        // add a "p" to ensure that our table is a valid name in case someone
        // selects a reserved keyword like "FROM"
        PLAYER_TABLE = "p"+playerType.toUpperCase();
        debug("Setting player table to "+PLAYER_TABLE);
    }


    
    // are we running or not?
    public boolean isRunning() {
        return getStatus()==RUNNING;
    }

    /**
     * Stop our system using specific parameters.
     * Accepted parameters:
     *      - FLUSH - clean up our mess
     */
    public boolean stop(Properties parameters) {
       debug("Stopping the "+PLAYER_TYPE + " system");

       // should we clean up our mess?
       if(parameters.containsKey("FLUSH"))
           flush();

       // say to everyone else that we are closed for business.
       setStatus(STOPPED);

       return true;
    }

    /**
     * Generic caller for the stop operation
     */
    public boolean stop() {
        Properties data = new Properties();
        return stop(data);
    }


 // Status handling routines
    void setStatus(int newStatus){
        status = newStatus;
    }

    public int getStatus(){
        return status;
    }

    private void log(String gender, String message){
     System.out.println("["+PLAYER_TYPE+"]["+gender+"] "+message);
    }

    private void debug(String message){
     if(debug)
         log("debug",message);
    }

    private Boolean setup() {
      // we need to simulate the lack of persistence, therefore
      // if any older table exists, we drop it here to start all over again

            // create a table with our required fields
            Boolean table_create = getRemedium().getDB().update(
                "CREATE TABLE IF NOT EXISTS "+PLAYER_TABLE+" ( "
                + FIELD_ID + " INTEGER IDENTITY, "
                + FIELD_NAME + " VARCHAR(256), "
                + FIELD_STATUS + " VARCHAR(256), "
                + FIELD_CREATED + " VARCHAR(256), "
                + FIELD_UPDATED + " VARCHAR(256), "
                + FIELD_CONTACT + " VARCHAR(256), "
                + FIELD_GROUP + " VARCHAR(256), "
                + FIELD_RANKING + " VARCHAR(256), "
                + FIELD_PARAMETERS + " LONGVARCHAR )"
                ).equalsIgnoreCase(TRUE);

        // if all went fine, output a true value
        return table_create;
    }

    private Boolean flush() {
        // if any older table exists, we drop it here to start all over again

           // drop table if it exists
            Boolean table_reset = getRemedium().getDB().update(
                "DROP TABLE IF EXISTS "+PLAYER_TABLE).equalsIgnoreCase(TRUE);

        // if all went fine, output a true value and create a new table
        return (table_reset && setup());
    }

    @Override
    public void setRemedium(Remedium remedium) {
        this.remedium = remedium;
    }

    @Override
    public Remedium getRemedium() {
        return this.remedium;
    }


}
