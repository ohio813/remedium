package system;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import remedium.Remedium;


public class database_hsql implements database_interface{

    // objects
    public Connection conn;   //our connnection to the db - persist for life of program
    private Remedium remedium = null;

    // definitions
    private boolean
       db_started = false;

    private String
            db_name_default = "database",
            db_name = db_name_default,
            db_folder_default = "storage",
            db_folder = db_folder_default;


    private String // errors that are acceptable inside our database
            errorUniqueKey = "unique constraint or index violation";

    @Override
    public void setRemedium(Remedium remedium) {
        this.remedium = remedium;
    }

    /** Check if a given error is acceptable or not */
    private Boolean isErrorAccepted(String error){
        Boolean result = error.contains(errorUniqueKey);
        return result;
    }

    // We create dummy methods that can be overriden by the descendents
    // of this class to suit their particular purposes.

    // called before starting the database_interface system
    @Override
    public boolean start_before(){return true; }
    // called after starting the database_interface system
    @Override
    public boolean start_after(){return true; }
    // called before closing the database_interface system
    @Override
    public boolean stop_before(){return true; }
    // called after closing the database_interface system
    @Override
    public boolean stop_after(){return true; }

    public boolean setName(String name){
        // we need to check if the new given name is valid or not
        db_name = name;
        return true;
    }

    public String getDefaultFolder(){
        return db_folder_default;
    }


    // start using only default values
     @Override
    public boolean start(){
         return start(new Properties());
     }
   /**
    * Start the database system. To allow several instances to work
    * on the same machine, whenever we have a value for port then
    * it will use the port number as location folder for the current
    * instance. This way we allow parallel execution of multiples instances.
    *
    * Accepted parameters:
    *   - PORT - the port name (used for the folder designation)
    *   - DIR - Defines the directory to be used
    */

    @Override
    public boolean start(Properties parameters){

            // if already running, no need to continue
            if (!start_before()) {
                return false;
            }

            // if there is a port definition, use it if this is a valid number
            if(parameters.containsKey(FIELD_PORT)){
                //change the current folder
                String temp = // do a filter to prevent malicious inputs
                    utils.text.findRegEx(
                        parameters.getProperty(FIELD_PORT)
                        ,"[0-9]+$", 0); // only accept 0-9 chars

                if((temp != null)
                 && (temp.length()>0)){ // if the result is bigger than zero, use it
                    log(INFO,"Using "+temp+" as database storage folder ("
                            +parameters.getProperty(FIELD_PORT)+")");
                    // do the change
                    db_name = temp;
                }
            }


            if(parameters.containsKey(DIR)){
                //change the current folder
                //TODO we need checking with regular expressions here
                String temp = // do a filter to prevent malicious inputs
//                    utils.text.findRegEx(
//                        parameters.getProperty(DIR)
//                        // only accept a-Z, 0-9 and -, _ chars
//                        ,"[a-zA-Z0-9_\\-\\/]", 0);
//                        //,"[a-zA-Z0-9-_\\/]+$", 0);
                        parameters.getProperty(DIR);

                if((temp != null)
                 && (temp.length()>0)){ // if the result is bigger than zero, use it
                    log(INFO,"Using '"+temp+"' as database storage folder");
                    // do the change
                    db_folder = temp;
                }
            }

        try {

            Class.forName("org.hsqldb.jdbc.JDBCDriver");

//            System.out.println("---------------------"+db_folder);
//            System.out.println("---------------------"+db_name);

            // handle a particular case for the first database on our system
            if(db_folder.contains(File.separator)==false){
                db_folder = db_folder.concat(File.separator + db_name);
                db_name = this.db_name_default;
            }




            try {
                conn = DriverManager.getConnection("jdbc:hsqldb:" 
                        + db_folder
                        + java.io.File.separatorChar // needed for Unix/Windows
                        + db_name,
                        "remedium", ""); // user name and password
            } catch (SQLException ex) {
                //Logger.getLogger(database_hsql.class.getName()).log(Level.SEVERE, null, ex);
                log(ERROR,"Unable to start our instance, exiting.");
                System.exit(-1);
                return false;
            }

            } catch (ClassNotFoundException ex) {
            Logger.getLogger(database_hsql.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            }

        if (!start_after()) {
                return false;
            }
         log(ROUTINE,"Database system is up and running");
       db_started = true;
     return true;
    }

    @Override
    public boolean stop(){
        return stop(new Properties());
    }

    /**
     * Close the database_interface system
     * Accepted values as parameters:
     *  - DELETE - remove the database file once it is closed
     */
    @Override
    public boolean stop(Properties parameters){

        if(!stop_before())return false;
        if(!shutdown())return false;
        if(!stop_after())return false;

        // let's interpret custom requests
        if(parameters.containsKey(DELETE)){ // delete our database files from disk
            deleteDB();
        }

        return true;
    }


    /**
     * Delete a given file from disk
     */
    private Boolean deleteFile(String filename){
        File dbFile = new File(filename);
        return dbFile.delete();
    }

    /**
     * Delete all traces of database files from this particular instance
     */
    private void deleteDB(){

        // never delete database if this is a single instance
        // the idea is that real work conditions only run one remedium per machine
        if(remedium.getIDname().equalsIgnoreCase(remedium.getIDnameDefault()))
            return;

        String name =     db_folder
                        + java.io.File.separatorChar // needed for Unix/Windows
                        + db_name;

        deleteFile(name+".properties");
        deleteFile(name+".script");
        deleteFile(name+".tmp");
        log(ROUTINE,"Database files were deleted");

    }


    // we dont want this garbage collected until we are done
    public database_hsql(){    // note more general exception
    }

    private boolean shutdown(){
        Statement st = null;

        try {
            st = conn.createStatement();
            // db writes out to files and performs clean shuts down
            // otherwise there will be an unclean shutdown
            // when program ends
            st.execute("SHUTDOWN");
            conn.close(); // if there are no other open connection

        } catch (SQLException ex) {
            //Logger.getLogger(database_hsql.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally{
            try {
               if(st != null)
                st.close();
            } catch (SQLException ex) {
            } finally {
            }
        }
        log(ROUTINE,"Database system has been closed");
        db_started = false;
     return true;
    }

//use for SQL command SELECT
    @Override
    public synchronized boolean query(String expression) {
        Statement st = null;

        try {
             //ResultSet rs = null;
            st = conn.createStatement(); // statement objects can be reused with
            //rs = st.executeQuery(expression); // run the query
            st.execute(expression);
            // do something with the result set.
            //dump(rs);
            st.close(); // NOTE!! if you stop a statement the associated ResultSet is
            // closed too
            // so you should copy the contents to some other object.
            // the result set is invalidated also  if you recycle an Statement
            // and try to execute some other query before the result set has been
            // completely examined.
        } catch (SQLException ex) {
            Logger.getLogger(database_hsql.class.getName()).log(Level.SEVERE, null, ex);
            log(ERROR,"Query failed: "+expression);
            return false;
        }
        finally{
            try {
               if(st != null)
                st.close();
            } catch (SQLException ex) {
            } finally {
            }
        }
        return true;
    }

//use for SQL commands CREATE, DROP, INSERT and UPDATE
    //@Override
    @Override
    public synchronized String update(String expression){

        Statement st = null;
        
        try {
            
            st = conn.createStatement(); // statements
            int i = st.executeUpdate(expression); // run the query
            if (i == -1) {
                log(ERROR,"Update error: " + expression);
            }
        } // void update()
        catch (SQLException ex) {
            //Logger.getLogger(database_hsql.class.getName()).log(Level.SEVERE, null, ex);
            // We accept some special exceptions to occur, filter them out
            if(isErrorAccepted(ex.toString())==false)
                log(ERROR,"Query exception: "+expression);
            return FALSE;
        }
        finally{
            try {
                if(st!= null)
                    st.close();
            } catch (SQLException ex) {
                st = null;
            }finally{
                st = null;
            }
        }
        return TRUE;
    }    // void update()


    @Override
    public boolean dump(ResultSet rs) {
        try {
            // the order of the rows in a cursor
            // are implementation dependent unless you use the SQL ORDER statement
            ResultSetMetaData meta = rs.getMetaData();
            int colmax = meta.getColumnCount();
            int i;
            Object o = null;
            // the result set is a cursor into the data.  You can only
            // point to one row at a time
            // assume we are pointing to BEFORE the first row
            // rs.next() points to next row and returns true
            // or false if there is no next row, which breaks the loop
            for (; rs.next();) {
                for (i = 0; i < colmax; ++i) {
                    o = rs.getObject(i + 1); // Is SQL the first column is indexed
                    // with 1 not 0
                    System.out.print(o.toString() + " ");
                }
                System.out.println(" ");
            }
        } //void dump( ResultSet rs )
        
        catch (SQLException ex) {
            //Logger.getLogger(database_hsql.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    // check if the database_interface server has already started or not
    @Override
    public boolean hasStarted() {
        return db_started;
    }


   private void log(int gender, String message){
     //System.out.println("[database]["+gender+"] "+message);
     remedium.log("database",gender,message);
 }

}    // class hsql_testDB

