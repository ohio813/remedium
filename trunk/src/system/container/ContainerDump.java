/*
 * The container dump is used to dump all the records from a given container
 * onto a files inside a given directory.
 *
 * These files are later used by other instances to import data onto their
 * containers.
 *
 * On this class we provide the following features:
 *  - Dump all records from a given container
 *  - Import all records from a given set of files
 */

package system.container;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.Message;
import system.core.Component;
import system.database;

/**
 *
 * @author Nuno Brito, 25th of July 2011 in Darmstadt, Germany.
 */
public class ContainerDump {

    // should we output debug messages or not?
    private boolean debug = true;

    private Component component;
    private Container container;
    private database db = null;
    private ContainerFile file;


    /** Public constructor */
    public ContainerDump(Component assignedComponent,
                        Container assignedContainer, database assignedDB){
        //preflight checks
        if(assignedContainer == null){
            System.out.println("ContainerLog error: Assigned container "
                    + "is null");
            return;
        }

        if(assignedComponent == null){
            System.out.println("ContainerLog error: Assigned component "
                    + "is null");
            return;
        }

        // map the provided objects to the objects of this class
        component = assignedComponent;
        container = assignedContainer;
        this.db = assignedDB;

    }


    /** Dump all contents of our container onto a given folder */
    public boolean toFolder(String rootFolder){
        // the first step is preparing our dump folder
        File root = new File(rootFolder);
        // check our own folder
        File folder = new File(rootFolder,
                component.getCanonicalName()
                + File.separator
                + container.getName());
        // do preflight checks, exit if they fail
        if(this.doPreflightChecks(root, folder) == false)
            return false;

        // folder is ready, do the dumping part.
        Boolean result = this.performDump();

        return result;
    }

      /** Import contents for our container from a given folder */
    public boolean fromFolder(String rootFolder){
        // the first step is preparing our dump folder
        File root = new File(rootFolder);
        // check our own folder
        File folder = new File(rootFolder,
                component.getCanonicalName()
                + File.separator
                + container.getName());
        // do preflight checks, exit if they fail
        if(root.exists()==false)
            return false;

        // folder is ready, do the dumping part.
        Boolean result = this.importDump(folder);
        return result;
    }



    /** Do all the initial checks to see if everything is ok*/
    private boolean doPreflightChecks(final File root, final File folder){
            if(root.exists()==false) { // no root folder? quit here.
            log(Message.ERROR,"Dump toFolder operation failed: "
                    + "Root folder does not exist '"+root.getAbsolutePath()
                    +"'");
            return false;
        }


        // we establish that no old data is allowed (this might change soon)
        if(folder.exists()){ // we don't want old folders here
            utils.files.deleteDir(folder);
            // double check if the folder was really deleted
            if(folder.exists()){
                log(Message.ERROR,"Dump toFolder operation failed: "
                    + "Work folder is not clean '"+folder.getAbsolutePath()
                    +"'");
            return false;
            }
        }

        // create our folder
        Boolean result = utils.files.mkdirs(folder.getAbsolutePath());

        if(result == false){
                log(Message.ERROR,"Dump toFolder operation failed: "
                    + "Work folder was not created '"+folder.getAbsolutePath()
                    +"'");
            return false;
            }
        // check if this folder really exists
        if((folder.exists()==false) || (folder.isFile())){
            log(Message.ERROR,"Dump toFolder operation failed: "
                    + "Failed to create '"+folder.getAbsolutePath() +"'");
            return false;
        }

        // create the Container file
        file = new ContainerFile(folder, container.getName());

        return true;
    }

    /** This method will proceed with the dumping onto a selected folder */
    private Boolean performDump(){
        // expression to get our records
        String expression = "SELECT * FROM " + container.getName();
        // initialize our variables
        Statement st = null;
        ResultSet rs = null;
        //run our query
        try {
            st = db.conn.createStatement(); // statement objects can be reused with
            rs = st.executeQuery(expression); // run the query

            // grab the indexing date of this record
            long date = 0;
            // position where the last column is located
            int pos = container.getStoreFields().length;

            for (; rs.next();) {
                String record = "";
                // read all our Message fields
                for (int i = 1; i < pos + 1; i++)
                { // add each record, split it using a ";"
                  record = record.concat(rs.getObject(i + 1).toString())+";";
                }

                try{
                // get the date value, must be placed at the penultimate column
                date = Long.parseLong(rs.getObject(3).toString());
                }catch(Exception e){}
                // add this file to our file record manager
                file.add(record, date);

            }
            st.close(); // NOTE!! if you stop a statement the ResultSet is lost
        } catch (SQLException ex) {
                        log(Message.ERROR,"Dump toFolder operation failed: "
                    + "SQL exception when calling " + expression);
            return false;
        } finally{
            try {
                st.close();
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(Container.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        // close down the file container
        boolean result = file.close();
        // all done
        return result;
    }

     public final void log(int gender, String message) {
        component.log("dump",gender, message);
    }

    /** This method will proceed with the dumping onto a selected folder */
    private Boolean importDump(File folder){

        System.out.println("Testing " + folder.getAbsolutePath());

        // find all files
        for(File importFile : folder.listFiles())
            // test if the his file is valid or not
            if(validFile(importFile)){
                // read all lines from our file
                String lines = 
                        utils.files.readAsString(importFile.getAbsolutePath());

                int i = 0;
                // iterate all lines inside the text file, use \n as separator
                for(String record : lines.split("\n")){
                    ++i;
                    // split each record into fields
                    String[] data = record.split(";");
                    // write this data inside our container
                    container.write(data);
                }
                if(debug)
                    System.out.println("Update ok! Counted " + i + " records"
                            + " at '"+importFile.getAbsolutePath()+"'");
            }

        return true;
    }

    /** Tests if a file is a valid for importing or not*/
    private boolean validFile(File file){
        // this file doesn't exist? quit
            if(file.exists()==false)
                return false;
        // not a file? quit
            if(file.isFile()==false)
                return false;
        // we can't read it? quit
            if(file.canRead() == false)
                return false;
        // not valid file name
            String name = file.getName();
            if( name.substring(0, 2).equals("db")==false)
                return false;
        // all done
        return true;
    }



}