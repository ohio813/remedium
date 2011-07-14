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
import system.mq.msg;

/**
 *
 * @author Nuno Brito, 29th of June 2011 in Darmstadt, Germany.
 */
public class ContainerFlatFile implements ContainerInterface {

    // should we output debug messages?
    //private boolean debug = true;
    // maximum number of accepted records per file
    private long maxRecords = 1000;

    //private int test = 1;

    // objects
    // our array of files that contain relevant records
    private HashMap<String, KnowledgeFile> knowledge = new HashMap();

    // An ordered list of files to be checked when reading/writing records
    private String[]
            readPriority = new String[]{},   // files that we will read first
            writePriority = new String[]{};  // files that we will write first

    // where our files will be located
    private File 
            rootFolder = null;
            
    private String id; // identification of our files
    private String[] fields; // columns of data

    // our constant keywords
    private String
            who = "ContainerFlatFile";

    // used for caching purposes
    //private String currentLines = "";
            
    // where we keep our messages
    private LogMessage logger = new LogMessage();


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

        // convert the fields to prevent external exposure of this object
        String out = "";
        for(String field: fields){
            out = out.concat(field + ";");
        }
        this.fields = out.split(";");
        this.rootFolder = rootFolder;
        // instantiate this container
        if(initialization()==false){
            result.add(who, msg.ERROR, "Failed to initialize");
            return;
        }
        // count all our records
        long plus = 0;
        // iterate all knowledge files
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
    private boolean initialization(){

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
        if(utils.text.isEmpty(sort)){
            // avoid empty results
            readPriority = new String[]{};
            writePriority = new String[]{};
        } else {
            readPriority = sort.split(";");
            writePriority = readPriority; // for the moment, they are the same
        }
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
     
        // no knowledge? no need to continue.
        if(knowledge.isEmpty()){
            // create a new knowledge file
            this.createKnowledgeFile();
        }
            
        // iterate all our knowledge files according to their priority
        for(String out : readPriority){
            // Save us some time if nothing changed since last check
            boolean doFullCheck = true;
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
            }
                // no checksum provided, do a full check of contents
                //doFullCheck = true;

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

        // only process if the file is not empty
        if(lines.length()> 0)
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

            // Add the count value to this knowledge file
            check.setCount(i);
        // All done
        return true;
    }

    /** Add a given knowledge file to our internal lists */
    private KnowledgeFile addKnowledge(File newKnowledge){
        // set the properties of this knowledge file
        Properties settings = new Properties();
        settings.setProperty(msg.COUNT, "" + 0);
        // create the new file
        KnowledgeFile result = new KnowledgeFile(settings, newKnowledge);
        // get the identifier
        String identifier = newKnowledge.toString();
        // add to the readPriority list
        readPriority = utils.text.stringArrayAdd(readPriority, identifier);
       // repeat same action for write priority
        writePriority = utils.text.stringArrayAdd(writePriority, identifier);
        // add knowledge to our array list
        knowledge.put(identifier, result);
        // return our result
        return result;
    }

    /** Remove a given knowledge file from our internal lists */
    public void deleteKnowledgeFiles(){
        for(String identifier : this.readPriority){
        // get the current knowledge
        KnowledgeFile current = this.knowledge.get(identifier);
        // if it does not exist, quit here
        if(current == null){
            log(msg.IGNORED, "deleteKnowledgeFile operation: Could not find"
                    + " knowledge file '%1'", identifier);
            return;
        }

        try{
            // now delete the knowledge file
            boolean delete = current.getFile().delete();
            // was this successful?
            if(delete==false){
                  log(msg.ERROR, "deleteKnowledgeFile operation failed: "
                    + "Could not delete file '%1'",
                    current.getFile().getPath());
            }
            }
        catch (Exception e){}
        
        // check if it exists or not
        if(current.getFile().exists()){
            log(msg.ERROR, "deleteKnowledgeFile operation failed: "
                    + "Could not delete file '%1'",
                    current.getFile().getPath());
        }
        // delete from our knowledge base
        this.removeKnowledge(identifier);
        }
        // reset our counters
        readPriority = new String[]{};
        writePriority = new String[]{};
    }


    /** Remove a given knowledge file from our internal lists */
    private void removeKnowledge(String identifier){
        // remove from the readPriority list
        readPriority = utils.text.stringArrayRemove(fields, identifier);
       // repeat same action for write priority
        writePriority = utils.text.stringArrayRemove(fields, identifier);
        // remove knowledge from our array list
        knowledge.remove(identifier);
    }

    /** Write this record onto our containers */
    public Boolean write(final String[] fields) {
        // preflight checks
       if(writePreCheck(fields)==false)
           return false;
        // empty knowledge? create a new file
        if(knowledge.isEmpty()){
            this.createKnowledgeFile();
        } else
        // If this records already exists, overwrite it.
        if(writeOverwrite(fields))
            return true;

       // A previous record was not found, write this one to our knowledge files
        if(writeCreateNew(fields))
            return true;
       
       // if we reached this far, something went wrong.
       log(msg.ERROR, "Write operation: Failed to write key '%1'", fields[0]);
       return false;
    }


    /** Do the preflight checks for the write operation*/
    private boolean writePreCheck(final String[] fields) {
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
        return true;
    }


    /** Try to overwrite a key if it already exists */
    private boolean writeOverwrite(final String[] fields) {
          // If this records already exists, overwrite it.
        String find = fields[0];
        boolean success = false;
        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : readPriority){
            // get the current knowledge file pointer
            KnowledgeFile current = knowledge.get(reference);
            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
                String lines = utils.files.readAsString(file);

//            // quick scan
//                if(lines.contains("\n"+fields[0]+";")==false)
//                    continue;

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
                        success = utils.files.SaveStringToFile(file, lines);
                        // was our write successful?
                        if(success == false){
                            log(msg.ERROR, "Write operation: Failed to save record '%1' "
                                    + "at file '%2'");
                            return false;
                        }
                        log(msg.COMPLETED, "Write operation: Overwrote"
                           + " key '%1' at file '%2'",
                           data[0], file.getPath() );
                        return true;
                    }
                }
        }
        // Not found.. write a new key instead.
        return success;
    }

    /** Create a new file */
    private KnowledgeFile createKnowledgeFile() {

        // create the point to our file
        File file = new File(rootFolder, "db-" + id
                + "_created-" + System.currentTimeMillis()
                + ".txt"
                );
        try {
            // create the empty file
            boolean createNewFile = file.createNewFile();
            // check the result of this operation
            if(createNewFile == false){
                log(msg.ERROR, "Create New KnowledgeFile operation failed:"
                        + " Unable to create file '%1'", file.getPath());
                return null;
            }
        } catch (IOException ex) {
                log(msg.ERROR, "Create New KnowledgeFile operation failed:"
                        + " Unable to create file '%1', an exception was "
                        + "reported. %2", file.getPath(), ex.toString());
                return null;
        }
        // Add this knowledge to our base
        KnowledgeFile addKnowledge = this.addKnowledge(file);

        // All done
        return addKnowledge;
    }


    /** returns the first available knowledge file. Creates a new one 
     *  if none availble */
    private KnowledgeFile getFreeKnowledgeFile(){


        // iterate all know knowledge files
       if(writePriority.length > 0)
        for(String reference : writePriority){
            // get the current knowledge file pointer
            KnowledgeFile current = knowledge.get(reference);
            // respect our limitation of records per file
            if(current.getCount() >= this.maxRecords){
                writePriority = utils.text.stringArrayRemove
                        (writePriority, reference);
                continue; // move onto the next one
            }
            // no need to continue, we found a nice file to write
            return current;
        }
        // create a new knowledge file
        return this.createKnowledgeFile();
    }

    /** Create a new key */
    private boolean writeCreateNew(final String[] fields) {

        // get a file container
        KnowledgeFile current = this.getFreeKnowledgeFile();

            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
            String lines = utils.files.readAsString(file);
            // convert record to new string
            String recordModified = convertRecordToString(fields);
            // add the new line
            lines = lines.concat(recordModified);
            // write back to disk
            boolean success = utils.files.SaveStringToFile(file, lines);
            // was our write successful?
            if(success == false){
                log(msg.ERROR, "Write operation: Failed to save record '%1' "
                        + "at file '%2'");
                return false;
            }
            // increase the record count for the used knowledge file
            current.incCount();
            // do the log
            log(msg.COMPLETED, "Write operation: Wrote"
               + " key '%1' at file '%2'",
               fields[0], file.getPath() );
            // finish here and quit
            return true;
//        }
//        return false;
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
        int pos = utils.text.arrayIndex(field, fields);
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
                    if(data[pos].equalsIgnoreCase(find)){
                        // add this record to our result
                        result.add(convertRecordToProperties(data));
                    }
                }
        }
        // All done
        return result;
    }

    /** This is a clean and mean version of read. It will only retrieve
     the first record that matches our index key, assumed as being
     the first column on the records. It is case-sensitive, IT IS FAST.*/
    public String[] read(String find) {
        if(knowledge.isEmpty())
            return new String[]{""};
        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : this.readPriority){
            // get the current knowledge file pointer
            KnowledgeFile current = this.knowledge.get(reference);
            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
                String lines = //TODO remove this and don't read the whole file
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

    /** Delete a given record from our container **/
    public boolean delete(String field, String find) {
        // empty knowledge? No need to continue
        if(knowledge.isEmpty()){
            log(msg.INACTIVE, "Delete operation not accepted: No knowledge"
                    + " files available to process.");
            return false;
        }
        // grab the index number of the field that we want
        int fieldIndex = utils.text.arrayIndex(field, fields);
        // if it is -1 then fail this operation
        if(fieldIndex < 0){
            log(msg.ERROR, "Delete operation failed: Field %1 was not found.",
                    field);
            return false;
        }
        
        //Iterate through the knowledge files of our container for the record(s)
        for(String reference : this.readPriority){
            // get the current knowledge file pointer
            KnowledgeFile current = this.knowledge.get(reference);
            // get the file pointer
            File file = current.getFile();
            // read all lines from our file
                String lines = //TODO remove this and don't read the whole file
                        utils.files.readAsString(file);
                int i = 0;
                // iterate all lines inside the text file, use \n as separator
                for(String record : lines.split("\n")){
                    ++i;
                    // split each record into fields
                    String[] data = record.split(";");
                    // does it match what we want?
                    if(data[fieldIndex].equals(find)){
                        // delete this record
                        //lines = lines.replaceFirst(record + "\n", "");
                        lines = lines.replaceAll(record + "\n", "");
                        // save the result back to the file
                        boolean result = 
                                utils.files.SaveStringToFile(file, lines);
                        // ensure we decrease the record count
                        if(result == true){
                            // decrease the record count
                            current.decCount();
                        }
                        // output our end result
                        return result;
                    }
                }
        }
        log(msg.ERROR, "Delete operation failed: Record %1 was not found on "
                + "field %2"
                , find
                , field);
        return false;
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
        return this.id;
    }

    public boolean isRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Drop all our records */
    public void drop() {
        String[] target = this.readPriority;
        for(String reference : target){
            this.removeKnowledge(reference);
        }
    }

    public String webRequest(Request request, Response response) {

         String action = utils.internet.getHTMLparameter(request, "action");
        String result = "";

   // count the number of records on this container
        if(action.equalsIgnoreCase("count")){
            result = "" + this.count();
            log(msg.INFO,"DB webRequest. Action 'count', we have " + result
                    + " records inside our container");
            return result;
        }
        return result;
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

    public long getMaxRecordsAllowed() {
        return maxRecords;
    }

    /** Get all field titlrs of this container */
    public String[] getFields() {
        String out = "";
        for(String field : fields)
            out = out.concat(field+";");
        // output the result, not clean but efficient
        return out.split(";");
    }
}


/*
 * Provides the means to manage a given knowledge files
 */
class KnowledgeFile{

    private Properties
            settings; // where all settings are stored
    private File
            file; // the file pointer
    private long 
            count, // count how many records we have
            modified; // when was it last modified?

    /** public constructor */
    public KnowledgeFile(Properties assignedSettings, File assignedFile){
        // preflight checks
        if((assignedSettings == null) || (assignedFile == null))
            return;
        // do the assignments
        settings = assignedSettings;
        file = assignedFile;
        modified = file.lastModified();
        count = Integer.parseInt(settings.getProperty(msg.COUNT, "0"));
    }

    public File getFile() {
        return file;
    }

    public long getModified() {
        return modified;
    }

    public Properties getSettings() {
        return settings;
    }

    public long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void incCount() {
        this.count++;
    }

    public void decCount() {
        this.count--;
    }
}