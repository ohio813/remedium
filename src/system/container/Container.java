/*
 * This is the implementation of the db_storage class
 *
 * The idea is to abstract applications and role from the need of dealing
 * directly with a database. This means abstraction from the tecnology applied
 * on the database itself, removing also the need of knowing how use JDBC.
 *
 * It is not as sophisticated as other methods such as hibernate, but it works
 * as intended and simplifies a task that would otherwise be dauting to manage,
 * introducing a layer to replace this solution with better ones in the future.
 *
 */
package system.container;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.database;
import system.msg;
import system.core.Component;

/**
 *
 * @author Nuno Brito, 19th of March 2011 in Darmstadt, Germany
 */
public class Container implements msg{

    // definitions
    protected boolean 
            debug = false, // should we display debugging messages or not?
            uniqueRecords = true;  // accept duplicates or not?

    private ContainerLog
            containerLog; // where we store our log of changes on the container

    private ContainerSync
            sync;

    public ContainerDump
            dump;

    private long
            containerLock = 0; // are we locked to a given instance?

    protected String[]
            store_fields; // the fields for our database table

    protected String 
            store_name; // the name of our table

    private int 
            status = STOPPED; // the current status of the container

    private Boolean
            reset = false; // should we reset the container data upon start?

    // objects
    private Component
              instance;

    private database 
            db;  // where we will perform our actions


 
    public Container(Component designedComponent,
            String title, String[] fields, Boolean doReset){
        this.construct(designedComponent, 
                title, fields, doReset,
                false);
    }

    public Container(Component designedComponent,
            //long assignedLock,
            String title, String[] fields, Boolean doReset,
            Boolean IsThisLog){
        this.construct(designedComponent, 
                //assignedLock,
                title, fields, doReset,
                IsThisLog);
    }

       
    public Container(Component designedComponent,
            //long assignedLock,
            String title, String[] fields){
        this.construct(designedComponent, 
                //assignedLock,
                title, fields, false,
             false);
        }

    /**
     * The constructor for the container.
     * @param designedRem The active instance that will be associated with this
     * container
     * @param assignedLock The unlock key that is required to use the public
     * methods
     * @param title The title assigned to this container that is used to
     * distinguish from others
     * @param fields The columns where each set of data will be stored
     * @param doReset Should we clean away the data from previous runs?
     */
    private void construct(Component designedComponent,
            //long assignedLock,
            String title, String[] fields, Boolean doReset,
            Boolean AreWeLog){
           // pre flight check
        if(designedComponent == null){
            System.out.println("Container failed to be created because it can't"
                    + " be used with a null instance");
            return;
        }

        if(designedComponent.getDB() == null)
                {
            System.out.println("Container failed to be created because it can't"
                    + " be used with a null database instance");
            return;
        }

        // all seems good, let's move on
        //containerLock = assignedLock;
        instance = designedComponent;
        db = designedComponent.getDB();
        reset = doReset;


        // create our log instance
        containerLog = new ContainerLog(this.instance, this);
        containerLog.isLog = AreWeLog; // should the log be active or not?
        containerLog.store_name = title;

        if(containerLog.isLog)
            uniqueRecords = false;
         else
            uniqueRecords = true;

        // create the synchronization provider
        sync = new ContainerSync(this.instance, this, db);
        dump = new ContainerDump(this.instance, this, db);

        // start this container
        start(title, fields);

        log(ROUTINE, "Container is available");
    }





    /**
     * This method will create a table based on the fields that were mentioned
     * during the startup routine
     */
    private boolean createTable() {
        // pre-flight check
        if (!isRunning()) {
            log(ERROR,"Create table operation failed. The storage instance is not running");
            return false;
        }

        String extra = "";

        // we can specify a constraint to keep a specific field unique
        // http://goo.gl/gvwlY
        if(store_fields.length > 1){
            extra = ", CONSTRAINT UNIQUE_"+store_fields[0]
                    +" UNIQUE ("+store_fields[0]+")";
        }

        if(uniqueRecords == false) //allow duplicates or not?
            extra = "";

        //String dataType = "VARCHAR(256)";
        String dataType = "LONGVARCHAR";

        String query =
                "CREATE TABLE IF NOT EXISTS " + store_name + " ( "
                + FIELD_ID + "_" + store_name + " INTEGER IDENTITY, "
                + utils.text.arrayToString(store_fields, " "+dataType+", ")
                + " "+dataType
                + extra
                + " )";

        //debug(query);
        Boolean table_create = db.update(query).equalsIgnoreCase(TRUE);
        // return the result from the previous operation
        return table_create;
    }


    /**
     * Drop all records and table
     */
    private boolean drop() {
        // pre-flight checks

        if (!isRunning()) {
            log(ERROR,"Drop operation failed. The storage instance is not running");
            return false;
        }

        log(ROUTINE,"Dropping all records at table '" + store_name + "'");
        return db.update(
                "DROP TABLE IF EXISTS " + store_name
                ).equalsIgnoreCase(TRUE);
    }

    public boolean drop(long unlock) {
        // pre-flight checks
        if(containerLock != unlock) return false; // Failed basic security check
        return this.drop();
    }

 

    /**
     * kickstart our service
     */
    private boolean start(
            //long unlock,
            String title, String[] fields) {
        // pre-flight checks
        if (isRunning()) {
            log(ERROR,"Start operation failed because it is already running");
            return false;
        }

        if(utils.text.isEmpty(title)){
            log(ERROR, "Start operation failed because the title is empty");
            return false;
        }

        if(  (fields == null)
           ||(fields.length == 0)  ){
            log(ERROR, "Start operation failed because the provided fields "
                    + "are empty");
            return false;
        }

        // all clear, let's start
        store_fields = fields;
        store_name = title;

        // we'll need database support
        if (!db.hasStarted()) {
            db.start();
        }

        // if it hasn't started, something is wrong
        if (!db.hasStarted()) {
            return false;
        }

        // change our status
        setStatus(RUNNING);

        // do reset of this table if requested
        if(reset)
            drop();

        // create our table
        createTable();

        // we're done with the start, let's continue
        return true;
    }

    /**
     * This method puts a given file record onto our database
     */
    public Boolean write(String[] fields) {
        // pre-flight checks
        //if(containerLock != unlock) return;// false; // Failed basic security check

        if (!isRunning()) {
            log(ERROR,"Write operation failed. The storage instance is not running");
            return false;
        }
        if (fields.length != (store_fields.length)) {
            log(ERROR,"Write operation failed. The fields length doesn't match, the accepted size is "
                    + store_fields.length+ " and we got "
                    + fields.length);
            return false;
        }
        String query =
                "INSERT INTO " + store_name + " ("
                + utils.text.arrayToString(store_fields, ",")
                + ") VALUES("
                + "'" + utils.text.arrayToString(fields, "', '")
                + "') ";

        debug(query);
        // do the query update
                db.update(query);

        // we've reached this far, output this operation as successful
        // albeit successfull: it is not a proof that our DB was written
        return true;
    }

     /**
     * This method puts a given file record onto our database
     */
    public boolean delete(
            String field, String key) {
        // pre-flight checks

        if (!isRunning()) {
            log(ERROR,"Delete operation failed. The storage instance is not running");
            return false;
        }

        if(utils.text.isEmpty(field)){
            log(ERROR, "Delete operation failed. Can't call delete using an empty field");
            return false;}

        if(utils.text.isEmpty(key)){
            log(ERROR, "Delete operation failed. Can't call delete using an empty key");
            return false;}

        String query =
                "DELETE FROM " + store_name
                + " WHERE "+field+" = '"+key+"'";

        debug(query);
        // do the query update
        Boolean result = true;
        db.update(query);

        // Write this entry on the log
        this.containerLog.writeLog(query);

        return result;
    }


    public ArrayList<Properties> read(String field, String find){
        // preflight checks
        //if(containerLock != unlock) return null; // Failed basic security check

        if (!isRunning()) {
            log(ERROR,"Get operation failed. The storage instance is not running");
            return null;
        }

        if(utils.text.isEmpty(field)){
            log(ERROR, "Get operation failed. Can't call GET using an empty field value");
            return null;
        }
        if(utils.text.isEmpty(find)){
            log(ERROR, "Get operation failed. Can't call GET using an empty find value");
            return null;
        }

        // expression to use on the JDBC
        String expression = "SELECT * FROM " + store_name + " WHERE "
                + field + " = '" + find + "'";
        return this.get(expression);
    }


    /** count the number of records available on this container */
    public long count(){
        long result = 0;

        try {
            // Some exceptions might occur, always return 0
            result = count("SELECT * FROM " + store_name);

        }catch (Exception e){}
        finally {}

        return result;
    }

    // SELECT * FROM "PUBLIC"."MD5" WHERE "CDATE_CREATED" > '0' AND
    // "CDATE_CREATED" < '1307053620779'

    /** Count all records published between a given time interval. */
    public long countBetween(long since, long until){
        String expression =
                "SELECT * FROM " + store_name + " WHERE "
                + FIELD_DATE_CREATED
                + " > '" + since + "'"
                + " AND "
                + FIELD_DATE_CREATED
                + " < '" + until + "'"
                ;
        //log(DEBUG, "countBetween: " + expression);
        return count(expression);
    }

    /** Return all records on this container */
    private long count(String expression){

        //String expression =  "SELECT * FROM " + store_name;
        Statement st = null;
        ResultSet rs = null;
        long result = 0;
          //run our query
        try {
            st = db.conn.createStatement(); // statement objects can be reused
            rs = st.executeQuery(expression); // run the query

            long i = 0;
            //TODO this is an inefficient count. Any suggestions to improve?
            while(rs.next())
                i++;

            result = i;

            st.close();
        } catch (SQLException ex) {
//            // annoying error: happens when the table has no records (count = 0)
//            if(ex.getLocalizedMessage().contains("object not found")){
//                // this
//                result = 0;
//            }
//            else // this might be a serious error, output it accordingly
                log(ERROR,"Count operation failed. An exception occured when calling "
                    + expression
                    + "\n Error message: "
                    + ex.getLocalizedMessage());
            //return -1;
        } finally{
            try {
                st.close();
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(Container.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
        return result;
    }

    /** Get the public store name */
    public String getName() {
        return store_name;
    }

    /** Get the fields of data for this container */
    public String[] getStoreFields() {
        String out = "";
        for(String field : store_fields)
            out = out.concat(field+";");
        // output the result, not clean but efficient
        return out.split(";");
    }
    
   
    /**
     * Get all the records that a given string inside a specific field
     */
    public ArrayList<Properties> get(String expression) {
        // pre flight checks
        if (!isRunning()) {
            log(ERROR,"Get operation failed. The storage instance is not "
                    + "running");
            return null;
        }

        if(utils.text.isEmpty(expression)){
            log(ERROR,"Get operation failed. Can't use "
                    + "an empty expression");
        }

        // initialize our variable.
        ArrayList<Properties> answer = new ArrayList<Properties>();

        Statement st = null;
        ResultSet rs = null;

          //run our query
        try {
            st = db.conn.createStatement(); // statement objects can be reused with
            rs = st.executeQuery(expression); // run the query

            // prepare to output our answer using the provided results
            // ResultSetMetaData meta = rs.getMetaData();
            Properties p = null;

            for (; rs.next();) {
                // clear the variable holder
                p = new Properties();
                // read all our msg fields
                for (int i = 1; i < store_fields.length + 1; i++) {
                    p.put(store_fields[i - 1], rs.getObject(i + 1).toString());
                }

                // add this record to our answer
                answer.add(p);
            }
            st.close(); // NOTE!! if you stop a statement the associated ResultSet is lost
        } catch (SQLException ex) {
            log(ERROR,"Get operation failed. An exception occured when calling "
                    + expression
                    + "\n Error message:"
                    + ex.getSQLState());
            return null;
        } finally{
            try {
                st.close();
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(Container.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // return our results
        return answer;
    }

    /** log messages to standard output */
    private void log(int gender, String message) {
        instance.log("db/"+store_name,gender, message);
    }
    
    /** log messages to standard output */
    private void debug(String message) {
        if (debug)
            log(DEBUG, message);
    }


    // Status handling routines
    private void setStatus(int newStatus) {
        status = newStatus;
    }

    /** Should this container be authorized to accept sync requests?*/
    public void setSyncAuthorize(Boolean allowSyncAuthorize) {
        sync.setSyncAuthorize(allowSyncAuthorize);
    }

    public int getStatus(long unlock) {
        if(containerLock != unlock) return -1; // Failed basic security check
        return status;
    }

    public boolean isRunning() {
        return (status == RUNNING);
    }

    /**
     * Stop our service
     */
    public boolean stop(long unlock) {
        if(containerLock != unlock) return false; // Failed basic security check
        setStatus(STOPPED);
        return true;
    }

    /** Export the contents of this database after a given date */
    public ArrayList<Properties> export(Long date){
        return containerLog.export(date);
    }

    /** Process incoming web requests.
     * Supported parameters:
     *      - count
     *      - sync
     */
    public String webRequest(Request request, Response response){

        String action = utils.internet.getHTMLparameter(request, "action");
        String result = "";

   // count the number of records on this container
        if(action.equalsIgnoreCase("count")){
            result = "" + this.count();
            log(INFO,"DB webRequest. Action 'count', we have " + result
                    + " records inside our container");
            return result;
        }

   // synchronize this container with someone else
        if(action.equalsIgnoreCase("remotesync")){
            // get our parameters
            String who = utils.internet.getHTMLparameter(request, "who");
            String since = utils.internet.getHTMLparameter(request, "since");
            String until = utils.internet.getHTMLparameter(request, "until");

            log(DEBUG,"DB webRequest. Action 'remotesync'. "
                    + "Synchronizing our data"
                    + " with container at " + who
                    + " since " + since
                    + " until " + until
                    );

            return sync.synchronizeRequester(who, since, until);
        }

        if(action.equalsIgnoreCase("sync")){
            // get our parameters
            String who = utils.internet.getHTMLparameter(request, "who");
            String since = utils.internet.getHTMLparameter(request, "since");
            String until = utils.internet.getHTMLparameter(request, "until");

            log(DEBUG,"DB webRequest. Action 'sync'. "
                    + "Synchronizing our data"
                    + " with container at " + who
                    + " since " + since
                    + " until " + until
                    );

            return sync.synchronizeProvider(who, since, until);
        }





        return result;
    }

}
