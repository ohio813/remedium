/*
 * This container implementation uses flat files to store and retrieve data.
 *
 * Desired properties:
 *      - Allows to set the max number of records per data file
 *      - Least amount possible of data files
 *      - Quick to retrieve data
 *          - Should read a record from a DB with 100 000 records
            under an average of 0,05 seconds after 100 random record reads
 *      - Quick to store data
 *          - Should write onto a DB 100 000 records under 10 minutes
 *      - Scale up to millions of records
 *          - Should write a DB with 100 000 000 records and manage them
 *      - Memory efficient
 *          - Overall memory usage shouldn't surpass 30Mb regardless of DB size
 * 
 *
 *
 */

package system.container;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.LogMessage;
import system.Message;

/**
 *
 * @author Nuno Brito, 29th of June 2011 in Darmstadt, Germany.
 */
public class ContainerFlatFile implements ContainerInterface {

    // should we output debug messages?
    //private boolean debug = true;
    // maximum number of accepted records per file
    private long maxRecords = 1000000;

    // objects
    // our array of files
    private ArrayList<fileRecord> storage = new ArrayList();
    // our current working file
    private fileRecord currentFile = null;
    // where our files will be located
    private File 
            rootFolder = null,
            indexFolder = null,
            indexFile = null
            ;

    private String id; // identification of our files
    private String[] fields; // columns of data


    /* Public constructor */
    public ContainerFlatFile(final String title, final String[] fields,
            File rootFolder, LogMessage result){
        // preflight checks
        if(utils.text.isEmpty(title)){
            result.set(Message.ERROR, 0, "Title is empty");
            return;
        }
        if(fields == null){
            result.set(Message.ERROR, 1, "Fields are null");
            return;
            }

        // do the assignments
        this.id = title;
        // conver the fields to prevent external exposure of this object
        String out = "";
        for(String field: fields){
            out = out.concat(field + ";");
        }
        this.fields = out.split(";");
        this.rootFolder = rootFolder;
        // instantiate this container
        this.initialization(result);
        // output a success message
        //result.set(Message.ROUTINE, 2, "All done.");
    }


    /** Test if our folders are ready and available */
    private boolean checkFolder(LogMessage result){
    // check if the root folder exists or not, if not then create one
        if(rootFolder.exists()==false){
            boolean mkdir = rootFolder.mkdir();
            if(mkdir == false){
                result.set(Message.ERROR, 3, "Unable to create the '"+
                    rootFolder.getAbsolutePath() + "' folder");
                return false;
            }
        }
        // if the root folder is not valid, assume this operation as failed
        if(     (rootFolder.exists()==false)
                ||
                (rootFolder.isDirectory()==false)
                ||
                (rootFolder.canRead()==false)
                ||
                (rootFolder.canWrite()==false)
                ){
            result.set(Message.ERROR, 4, "Unable to create the '"+
                    rootFolder.getAbsolutePath() + "' folder");
            return false;
        }
        // All done
        return true;
    }


    /** Initialize the work folder and files */
    private void initialization(LogMessage result){

        //Verify that target folder is valid and available for operations
        if (checkFolder(result)==false)
            return;

        // Find knowledge files inside the target folder, crawl subfolders
        if (findKnowledgeFiles(result)==false)
            return;

        

        // output the final result
        result.set(Message.ROUTINE, 2, "All done.");
    }


    /** Read the file name and decompose all the properties mentioned */
    private Properties readFileName(File file, LogMessage message){
        // create the properties object
        Properties result = new Properties();
        // get the file name
        String filename = file.getName();
        // split and iterate through each property
        for(String property : filename.split("_")){
            // split and iterate through the key/value
            String[] out = property.split("-");
            // do safety check on this key/value pair
            if(out.length != 2){
                message.set(Message.ERROR, 5, "Invalid key/value pair size "
                        + "of '"+property+"' from '"
                        + file.getAbsolutePath() + "'");
                return null;
            }
            // get the key and value pair
            String key = out[0];
            String value = out[1];
            // write this pair inside the properties object
            result.setProperty(key, value);
        }

        // all done
        message.set(Message.COMPLETED, "readFilename operation was completed");
        return result;
    }


    /** Find knowledge files inside the target folder, crawl subfolders */
    private boolean findKnowledgeFiles(LogMessage result){

        // find all files inside our root folder
        ArrayList<File> list = utils.files.findfiles(rootFolder, 25);
        // iterate all files that were found
        for(File file : list){
            // process the file name and get the respective properties
            Properties currentName = this.readFileName(file, result);
            // if an error occurred, quit here
            if(result.getGender()==Message.ERROR)
                return false;
            System.out.println(currentName.toString());
        }
        // All done
        return true;
    }


//    /** Starts the container indexFolder file */
//    private Boolean startIndexFile(LogMessage result){
//        // create our indexFolder file if it doesn't exist already
//        indexFile = new File(indexFolder,"index.txt");
//        // attempt to create a new file
//        try {
//            indexFile.createNewFile();
//            // something went wrong, output a message and quit
//        } catch (IOException ex) {
//            result.set(Message.ERROR, 6, "Unable to create the '"+
//                    rootFolder.getAbsolutePath() + "' file. "
//                    + "Reported error: " + ex.toString());
//            return false;
//        }
//        // All done
//        return true;
//    }


    public Boolean write(String[] fields) {
        return true;
    }

    public boolean delete(String field, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<Properties> read(String field, String find) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long count() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long countBetween(long since, long until) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String webRequest(Request request, Response response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
