/*
 * This container implementation uses flat files to store and retrieve data.
 *
 * Desired properties:
 *      - Allows to add the max number of records per data file
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
 * To read the full specification, visit:
 * http://code.google.com/p/remedium/wiki/ContainerFlatFile
 *
 */

package system.container;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.log.LogMessage;
import system.msg;

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
    // our array of files that contain relevant records
    HashMap<Properties, File> knowledge = new HashMap();

    // an ordered list of files to be checked when reading records
    String[] readPriority;

    // where our files will be located
    private File 
            rootFolder = null,
            indexFile = null
            ;

    // our constant keywords
    final String
            RANK = "rank";



    private String id; // identification of our files
    private String[] fields; // columns of data


    /* Public constructor */
    public ContainerFlatFile(final String title, final String[] fields,
            File rootFolder, LogMessage result){
        // preflight checks
        if(utils.text.isEmpty(title)){
            result.add(msg.ERROR, "Title is empty");
            return;
        }
        if(fields == null){
            result.add(msg.ERROR, "Fields are null");
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
        //result.add(msg.ROUTINE, 2, "All done.");
    }



    /** Initialize the work folder and files */
    private void initialization(LogMessage result){

        //Verify that target folder is valid and available for operations
        if (checkFolder(result)==false)
            return;
        // Find knowledge files inside the target folder, crawl subfolders
        if (findKnowledgeFiles(result)==false)
            return;
        // Sort these files according to their importance level
        sortKnowledgeFiles();
        // Create an index file for our container ID if one does not exist
        if (this.createIndexFile(result)==false)
            return;

        for(String out : this.readPriority){
            System.out.println("-->" + out);
        }

        // output the final result
        result.add(msg.ROUTINE, "All done.");
    }


    /**
     * Sort these files according to their ranking level.
     * Higher number = higher ranking = first to be processed
     */
    private void sortKnowledgeFiles(){
        // where we will hold our sorted ranking of knowledge
        String sort = "";
        // loop all files for each level of rank
        for(int i = 9; i > 0; i--){
            // check if any file matches this specific level or not
            for(Properties current : knowledge.keySet()){
                // check if verifies or otherwise continue
                if(current.getProperty(RANK, "1")
                        .equalsIgnoreCase("" + i)==false)
                    continue;
                // we use getPath instead of absolutePath to save RAM memory
                String name = knowledge.get(current).getPath();
                // add the name to our list
                sort = sort.concat(name + ";");
                
            }
        }
        // output our result to the read Priority list
        readPriority = sort.split(";");
    }

    

    /** Test if our folders are ready and available */
    private boolean checkFolder(LogMessage result){
    // check if the root folder exists or not, if not then create one
        if(rootFolder.exists()==false){
            boolean mkdir = rootFolder.mkdir();
            if(mkdir == false){
                result.add(msg.ERROR, "Unable to create the '%1' folder",
                        rootFolder.getAbsolutePath());
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
                result.add(msg.ERROR, "Unable to create the '%1' folder",
                        rootFolder.getAbsolutePath());
            return false;
        }
        // All done
        return true;
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
                message.add(msg.ERROR,
                        "Invalid key/value pair size of '%1' from '%2'",
                        property,
                        file.getAbsolutePath());
                return null;
            }
            // get the key and value pair
            String key = out[0];
            String value = out[1];
            // write this pair inside the properties object
            result.setProperty(key, value);
        }

        // all done
        message.add(msg.COMPLETED, "readFilename operation completed");
        return result;
    }


    /** Find knowledge files inside the target folder, crawl subfolders */
    private boolean findKnowledgeFiles(LogMessage message){

        // find all files inside our root folder
        ArrayList<File> list = utils.files.findfiles(rootFolder, 25);
        // iterate all files that were found
        for(File file : list){
            // we only want knowledge files
            if( file.getName().substring(0, 2).equals("db")==false)
                continue;
            // process the file name and get the respective properties
            Properties currentKnowledge = this.readFileName(file, message);
            // if an error occurred, quit here
            if(message.getResult()==msg.ERROR)
                return false;
            // Get the knowledge files that match our container ID
            if(currentKnowledge.getProperty("db").equalsIgnoreCase(id)!=true)
                continue; // if things don't match, keep on moving to the next
            knowledge.put(currentKnowledge, file);
        }
        // All done
        return true;
    }


    /** Create an index file for our container ID if one does not exist */
    private Boolean createIndexFile(LogMessage result){
        // create our indexFolder file if it doesn't exist already
        indexFile = new File(rootFolder,"index-" + id + ".txt");
        // attempt to create a new file
        try {
            indexFile.createNewFile();
            // something went wrong, output a message and quit
        } catch (IOException ex) {
            result.add(msg.ERROR, "Unable to create the '%1' file. "
                    + "Reported error: %2",
                    rootFolder.getAbsolutePath(),
                    ex.toString()
                    );
            return false;
        }
        // All done
        return true;
    }


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
