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
    private long maxRecords = 10000;

    // objects
    // our array of files that contain relevant records
    HashMap<String, KnowledgeFile> knowledge = new HashMap();

    // An ordered list of files to be checked when reading/writing records
    String[]
            readPriority,   // files that we will read first
            writePriority;  // files that we will write first

    // where our files will be located
    private File 
            rootFolder = null;
            
    private String id; // identification of our files
    private String[] fields; // columns of data

    // our constant keywords
    String
            who = "ContainerFlatFile";
            
    // where we keep our messages
    LogMessage logger = new LogMessage();


    /* Public constructor */
    public ContainerFlatFile(final String title, final String[] fields,
            File rootFolder, LogMessage result){
        // preflight checks
        if(utils.text.isEmpty(title)){
            result.add(who, msg.ERROR, "Title is empty");
            return;
        }
        if(fields == null){
            result.add(who, msg.ERROR, "Fields are null");
            return;
            }

        // do the assignments
        this.id = title;
        // define who we are
        this.who = "db-" + title;

        // conver the fields to prevent external exposure of this object
        String out = "";
        for(String field: fields){
            out = out.concat(field + ";");
        }
        this.fields = out.split(";");
        this.rootFolder = rootFolder;
        // instantiate this container
        if(initialization(result)==false){
            result.add(who, msg.ERROR, "Failed to initialize");
            return;
        }
        // count all our records
        long plus = 0;
        for(String reference : readPriority){
            KnowledgeFile current = knowledge.get(reference);
            plus += current.getCount();
        }
        // output a success message
        result.add(who, msg.COMPLETED, "Container has started, %1 records are "
                + "available",
                "" + plus);
    }



    /** Initialize the work folder and files */
    private boolean initialization(LogMessage result){

        //Verify that target folder is valid and available for operations
        if (checkFolder()==false)
            return false;
        // Find knowledge files inside the target folder, crawl subfolders
        if (findKnowledgeFiles()==false)
            return false;
        // Sort these files according to their importance level
        sortKnowledgeFiles();
       
        // Process each Knowledge file that was found
        processKnowledgeFiles();
        
        // All done
        return true;
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
            for(KnowledgeFile current : knowledge.values()){
                // check if verifies or otherwise continue
                if(current.getSettings().getProperty(msg.RANK, "1")
                        .equalsIgnoreCase("" + i)==false)
                    continue;
                // we use the pointer to save RAM memory
                String name =
                        //current.getFile().getPath();
                        current.toString();
                // add the name to our list
                sort = sort.concat(name + ";");
                
            }
        }
        // output our result to the read Priority list
        readPriority = sort.split(";");
        writePriority = readPriority; // for the moment, they are the same

    }

    

    /** Test if our folders are ready and available */
    private boolean checkFolder(){
    // check if the root folder exists or not, if not then create one
        if(rootFolder.exists()==false){
            boolean mkdir = rootFolder.mkdir();
            if(mkdir == false){
                log(msg.ERROR, "Unable to create the '%1' folder",
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
                log(msg.ERROR, "Unable to create the '%1' folder",
                        rootFolder.getAbsolutePath());
            return false;
        }
        // All done
        return true;
    }



    /** Read the file name and decompose all the properties mentioned */
    private Properties readFileName(File file){
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
                log(msg.REFUSED,
                        "Invalid key/value pair size of '%1' from '%2'",
                        property,
                        file.getAbsolutePath());
                continue; // invalid parameter, ignore it
            }
            // get the key and value pair
            String key = out[0];
            String value = out[1];
            // write this pair inside the properties object
            result.setProperty(key, value);
        }

        // all done
        log(msg.COMPLETED, "readFilename operation completed");
        return result;
    }


    /** Find knowledge files inside the target folder, crawl subfolders */
    private boolean findKnowledgeFiles(){

        // find all files inside our root folder
        ArrayList<File> list = utils.files.findfiles(rootFolder, 25);
        // iterate all files that were found
        for(File file : list){
            // we only want knowledge files
            if( file.getName().substring(0, 2).equals("db")==false)
                continue;
            // process the file name and get the respective properties
            Properties currentKnowledge = this.readFileName(file);
            // if an error occurred, quit here
            if(logger.getResult()==msg.ERROR)
                return false;
            // Get the knowledge files that match our container ID
            if(currentKnowledge.getProperty("db").equalsIgnoreCase(id)!=true)
                continue; // if things don't match, keep on moving to the next
            // create an object to hold our data
            KnowledgeFile knowhow = new KnowledgeFile(currentKnowledge, file);
            // place this data on our list
            knowledge.put(knowhow.toString(), knowhow);
        }
        // All done
        return true;
    }



     /** Process each Knowledge file that was found */
    private void processKnowledgeFiles(){
        /**
         * We save time if there have been no content changes since the last
         * start, first we check if there have been changes on a superficial
         * level
         */
        // iterate all our knowledge files according to their priority
        for(String out : this.readPriority){
            // Save us some time if nothing changed since last check
            boolean doFullCheck = false;
            // get the currently selected knowledge file
            KnowledgeFile current = knowledge.get(out);
            // get the settings for this knowledge file
            Properties settings = current.getSettings();
            // get the file pointer
            File file = current.getFile();            
            // verify checksum
            if(settings.containsKey(msg.CHECKSUM)){
                // get modification date mentioned on file name
                String checksumProvided = settings.getProperty(msg.CHECKSUM);
                // compute the checksum of the file content
                String checksumGenerated = app.sentinel.ScannerChecksum
                    .generateStringSHA256(file.getAbsolutePath());
                // compare them both
                 doFullCheck = checksumProvided // check if they match
                      .equalsIgnoreCase(""+checksumGenerated)==false;
            }else{
                // no checksum provided, do a full check of contents
                doFullCheck = true;
            }

            // do we need to check the accuracy of this knowledge file?
            if(doFullCheck){
                boolean result = check(current);
                if(result == false)
                    System.out.println(logger.getRecent());
            }
        }
    }


    /** Will verify that a given knowledge file is correct*/
    private boolean check(KnowledgeFile check){
        // get the data holders
        Properties settings = check.getSettings();
        File file = check.getFile();

        // first test: Do we have the correct format of data?

        // read all lines from our file
                String lines =
                        utils.files.readAsString(file);
        long i = 0;
        int size = fields.length;
                // iterate all lines inside the text file, use \n as separator
                for(String record : lines.split("\n")){
                    ++i;
                    // split each record into fields
                    String[] data = record.split(";");
                    // write this data inside our container
                    if(size != data.length){
                        log(msg.REFUSED,
                                "Knowledge file '%1' has a data field sized"
                                + " in %2 while we are expecting %3. Error"
                                + " occurred in line %4",
                                "" + file.getAbsolutePath(),
                                "" + data.length,
                                "" + size,
                                "" + i
                               );
                        // remove this file from our list
                        removeKnowledge(check.toString());
                        return false;
                    }
                }

        // second test: is the record count accurate?

        // parse the reported number of records on this file
        long count = Long.parseLong(settings.getProperty(msg.COUNT,""+0));

        // do the comparison test
        if(count > 0) // only proceed if there is a value to compare
            if(count != i){
                log(msg.REFUSED,
                        "Knowledge file '%1' has %2 records but reported %3",
                        "" + file.getAbsolutePath(),
                        "" + i,
                        "" + size
                       );
                // remove this file from our list
                removeKnowledge(check.toString());
                return false;
            }
                // All done
                return true;
    }

    /** Remove a given knowledge file from our internal lists */
    private void removeKnowledge(String identifier){
        // remove from the readPriority list
        String result = "";
        for(String current : readPriority)
            if (current.equalsIgnoreCase(identifier))
                continue;
            else
                result = result.concat(current + ";");
        // write back our list
        readPriority = result.split(";");
     // repeat same action for write priority
        for(String current : writePriority)
            if (current.equalsIgnoreCase(identifier))
                continue;
            else
                result = result.concat(current + ";");
        // write back our list
        writePriority = result.split(";");

        // remove knowledge from our array list
        knowledge.remove(identifier);
    }

    public Boolean write(String[] fields) {
        // preflight checks
        if(fields.length != this.fields.length){ // do field sizes check?
            log(msg.ERROR, "Write operation failed: Attempted to write a "
                    + "record with %1 columns on a table with %2 columns",
                    "" + fields.length,
                    "" + this.fields.length);
            return false;
        }
        if(utils.text.isEmpty(fields[0])){
            log(msg.ERROR,"Write operation failed: Cannot accept an empty "
                    + "value on the first parameter");
            return false;
        }
        //Get a list of the available read-write knowledge files
        // ignore, for now all files are read-write up to max capacity

        // If this records already exists, overwrite it.
        String find = fields[0];
        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : writePriority){
            // get the current knowledge file pointer
            KnowledgeFile current = knowledge.get(reference);
            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
                String lines = utils.files.readAsString(file);
                int i = 0;
                // iterate all lines inside the text file, use \n as separator
                for(String record : lines.split("\n")){
                    ++i;
                    // split each record into fields
                    String[] data = record.split(";");
                    // does it match what we want?
                    if(data[0].equals(find)){
                        // add this record to our result
                        String recordModified = convertRecordToString(fields);
                        // do the replacement
                        lines = lines.replaceFirst(record+"\n",recordModified);
                        // write back to disk
                        utils.files.SaveStringToFile(file, lines);
                        log(msg.COMPLETED, "Write operation: Overwrote"
                           + " key '%1' with values '%2' at file '%3'",
                           data[0], recordModified, file.getPath() );
                        return true;
                    }
                }
        }

  // A previous record was not found, write this one to our knowledge files

        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : writePriority){
            // get the current knowledge file pointer
            KnowledgeFile current = knowledge.get(reference);
            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
            String lines = utils.files.readAsString(file);
            // convert record to new string
            String recordModified = convertRecordToString(fields);
            // add the new line
            lines = lines.concat(recordModified);
            // write back to disk
            utils.files.SaveStringToFile(file, lines);
            // do the log
            log(msg.COMPLETED, "Write operation: Wrote"
               + " key '%1' with values '%2' at file '%3'",
               fields[0], recordModified, file.getPath() );
            // finish here and quit
            return true;
        }
             
//        log(msg.COMPLETED, "Write operation: Wrote "
//           + " key '%1' with values '%2' at file '%3'",
//           data[0], recordModified, file.getPath() );
        return false;
    }




    /**
     * Find all records that match a given string.
     * @param field the column to look
     * @find the text string to find
     *
     * The parameter field is case insensitive, the parameter find is case
     * sensitive.
     */
    public ArrayList<Properties> read(String field, String find) {
        // create our object
        ArrayList<Properties> result = new ArrayList();
       
        // get the column number
        int pos = -1; // sets the position of the field to look data
        int temp = -1; // used as temporary reference
        for(String title : fields){ // iterate all fields
            temp++;
            if(title.equalsIgnoreCase(field)) // compare each one
                pos = temp; // if it matches set the position with this number
        }
        // have we found something?
        if(pos == -1){
            log(msg.ERROR,"Read operation failed: Field '%1' was not found.",
                    field);
            return null;
        }
        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : this.readPriority){
            // get the current knowledge file pointer
            KnowledgeFile current = this.knowledge.get(reference);
            // respect our limitation of records per file
//            if(current.getCount() >= this.maxRecords)
//                continue; // move onto the next one

            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
                String lines =
                        utils.files.readAsString(file);
                int i = 0;
                // iterate all lines inside the text file, use \n as separator
                for(String record : lines.split("\n")){
                    ++i;
                    // split each record into fields
                    String[] data = record.split(";");
                    // does it match what we want?
                    if(data[0].equalsIgnoreCase(find)){
                        // add this record to our result
                        result.add(convertRecordToProperties(data));
                    }
                }
        }
        // All done
        return result;
    }

    /** This is a clean and mean version of read. It will only retrieve
     the first records that it finds and the index key is assumed as being
     the first column. It is case-sensitive, IT IS FAST.*/
    public String[] read(String find) {
        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : this.readPriority){
            // get the current knowledge file pointer
            KnowledgeFile current = this.knowledge.get(reference);
            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
                String lines =
                        utils.files.readAsString(file);
                int i = 0;
                // iterate all lines inside the text file, use \n as separator
                for(String record : lines.split("\n")){
                    ++i;
                    // split each record into fields
                    String[] data = record.split(";");
                    // does it match what we want?
                    if(data[0].equals(find)){
                        // add this record to our result
                        return data;
                    }
                }
        }
        return new String[]{""};
    }



    /** Converts a given record onto a Properties object */
    private Properties convertRecordToProperties(String[] record){
        Properties result = new Properties();
        int i = -1;
        // iterate all fields
        for(String field : fields){
            i++; // increment our counter
            result.setProperty(field, record[i]);
        }
        return result;
    }

  /** Converts a given record onto a string that can be written on a file */
    private String convertRecordToString(String[] record){
        String result = "";
        // iterate all fields of this record
        for(String field : record) // add a comma to split them
                result = result.concat(field + ";");
            // add the breakline
            result = result.concat("\n");
        return result;
    }


    public boolean delete(String field, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Return the number of records available in our container */
    public long count() {
        long result = 0;
        for(String reference : readPriority){
            KnowledgeFile current = knowledge.get(reference);
            result += current.getCount();
        }
        return result;
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


    /** central logger for this class */
    private void log(final int gender, final String message,
            final String... args){
        // log a new message
        logger.add(who, gender, message, args);
    }

    public LogMessage getLog() {
        return logger;
    }




}


/*
 * Provides the means to manage a given knowledge files
 */
class KnowledgeFile{

    private Properties settings; // where all settings are stored
    private File file; // the file pointer
    private long count; // count how many records we have

    /** public constructor */
    public KnowledgeFile(Properties assignedSettings, File assignedFile){
        // preflight checks
        if((assignedSettings == null) || (assignedFile == null))
            return;
        // do the assignments
        settings = assignedSettings;
        file = assignedFile;
        count = Integer.parseInt(settings.getProperty(msg.COUNT, "0"));
    }

    public File getFile() {
        return file;
    }

    public Properties getSettings() {
        return settings;
    }

    public long getCount() {
        return count;
    }


}