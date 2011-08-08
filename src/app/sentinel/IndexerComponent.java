/*
 * The Indexer will pick on the information that is provided from a given
 * source and place it inside the containers.
 */

package app.sentinel;

import system.core.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.html.Table;
import system.mqueue.msg;
import utils.AverageTracker;
import utils.Graph;

/**
 *
 * @author Nuno Brito, 27th of March 2011 in Germany
 */
public class IndexerComponent extends Component implements msg{


    AverageTracker // calculates an average of processed files
            processedFilesTracker = new AverageTracker();

    long previousProcessed = 0; // used to now how many files were added since previous count


    public IndexerComponent(Remedium assignedInstance,
            Component assignedFather){
       super(assignedInstance,  assignedFather);
     }

  @Override
    public String getTitle() {
        return "indexer";
    }


   private Scanner_ProcessWin32
           win32;

      // boolean that are only run once to save time and CPU cycles
    private boolean
            isWindows = false;

    private String
//            folderDocuments,
//            folderDesktop,
//            folderHome,
            folderWin;

    private HashMap<String, Long> counter = new HashMap();

      // list of file records
    private ArrayList<File>
            fileList = new ArrayList<File>(),
            fatFileList = new ArrayList<File>();

    private int
            counterFile = 0,
            counterFatFile = 0,
            processedFiles = 0,         // how many files were processed so far?
            maxFetch     = 500,         // how many records do we want per loop?
            maxSize      = 10000000;    // split big from small files here

    /** Add a new file to our queue list */
    private void addFile(String filename){
        try{
        File temp = new File(filename);
        fileList.add(temp);
        counterFile++; // increase the counter
        } catch (Exception e) {
            log(ERROR, "addFile operation: failed to add '"+filename+"'");
        }
    }

    /** Add a new file to our fat file queue list (lower priority)*/
    private void addFatFile(String filename){
        try{
        File temp = new File(filename);
        fatFileList.add(temp);
        counterFatFile++; // increase the counter
        } catch (Exception e) {
            log(ERROR, "addFatFile operation: failed to add '"+filename+"'");
        }
    }

    /**
     * Adapt our variables to the system where we are running at this moment
     */
    private void startEnvironmentVariables(){
     // TODO We need to also handle Linux, not just windows
     if(utils.currentOS.isWindows()){
            isWindows = true;
            // grab the default user folder
            //folderDocuments = utils.files.getDocumentsDirectory();
            //folderDesktop = utils.files.getDesktopDirectory();
            //folderHome = utils.files.getHomeDirectory();
//            javax.swing.JFileChooser jFileChooser1 = new javax.swing.JFileChooser();
//            folderDocuments = jFileChooser1.getFileSystemView().getDefaultDirectory()
//                    .getAbsolutePath();
//            folderDesktop = jFileChooser1.getFileSystemView().getHomeDirectory()
//                    .getAbsolutePath();
//            folderHome = jFileChooser1.getFileSystemView().getParentDirectory(
//                   jFileChooser1.getFileSystemView().createFileObject(folderDesktop)
//                   ).getAbsolutePath();
            folderWin= System.getenv("windir");
        }
    }

    /**
     * Create the containers that will be used to store information
     */
    private void createContainers(){

           box.add(this.createDB(TABLE_REFERENCE,
                   new String[]{FIELD_REFERENCE,
                    FIELD_UPDATED,
                    FIELD_DATE_CREATED,FIELD_ID_SERIAL})
                    );

           box.add( this.createDB(TABLE_FILE_NAME,
                   new String[]{FIELD_NAME,
                   FIELD_UPDATED,
                   FIELD_REFERENCE, FIELD_DATE_CREATED, FIELD_ID_SERIAL})
                    );

           box.add( this.createDB(TABLE_FILE_PATH,
                   new String[]{FIELD_PATH,
                   FIELD_UPDATED,
                   FIELD_REFERENCE, FIELD_DATE_CREATED, FIELD_ID_SERIAL})
                    );

           box.add( this.createDB(TABLE_FILE_SHA1,
                   new String[]{ FIELD_HASH_SHA1,
                   FIELD_UPDATED,
                   FIELD_REFERENCE, FIELD_DATE_CREATED, FIELD_ID_SERIAL})
                    );

           box.add( this.createDB(TABLE_FILE_CRC32,
                   new String[]{FIELD_HASH_CRC32,
                   FIELD_UPDATED,
                   FIELD_REFERENCE, FIELD_DATE_CREATED, FIELD_ID_SERIAL})
                    );

           box.add( this.createDB(TABLE_FILE_MD5,
                   new String[]{FIELD_HASH_MD5,
                   FIELD_UPDATED,
                   FIELD_REFERENCE, FIELD_DATE_CREATED, FIELD_ID_SERIAL})
                    );

           // the handler of win32 files
           win32 = new Scanner_ProcessWin32(this, box);

   }


    @Override
    public void onStart() {
        // create the containers where we will write/read all gathered information
        createContainers();
        // read the environment variables for the current operative system
        startEnvironmentVariables();
        // all done
        log(INFO, "Ready to start");
    }


    /**
     * This is method is called whenever we receive an incoming message that
     * contains fresh data to process.
     */
    public void digest_process(Properties message){

        // TODO This is not good enough, the count of files does not match
        // but I am unable to find the problem
        String temp = message.getProperty(FIELD_MESSAGE);
        String[] files = temp.split(SPLIT);

        for(String file : files){
            addFile(file);
        }

        log(INFO, "Added "+files.length+" files to processing queue");
    }

    /** Dump all our containers to a given folder*/
    public void digest_dump(Properties message){
        String path = message.getProperty(FIELD_MESSAGE);
        box.dump(path);
        log(INFO, "Dump operation successful: All records placed at " + path);
    }

    /** Import all our containers from a given folder*/
    public void digest_dumpImport(Properties message){
        String path = message.getProperty(FIELD_MESSAGE);
        box.dumpImport(path);
        log(INFO, "Dump import successful: All records imported from " + path);
    }

    /**
     * For each file we will add to our containers in case the information
     * is relevant to be added. This follow different steps, for example,
     * we must avoid duplicate information, we must discard invalid information
     * and so on.
     */
    private void addToContainers(File file){

        // Add this file to the reference table

         String
                resultReference,
                resultFilename,
                resultPath,
                resultDate;

         if(file.length() > maxSize){ // limit our test to 100Mb
             log(INFO,"Not indexing "
                     +file.getAbsolutePath()
                     +" ("+utils.files.humanReadableSize(file.length())
                     +")"
                     );
             addFatFile(file.getAbsolutePath());
             return;
         }

         // if we can't read the file, stop the show here and move onto the next
         if(file.canRead() == false){
            try {
                log(ERROR, "addToContainers operation failed: Can't read '"
                        + file.getCanonicalPath() + "'");
            } catch (IOException ex) {
                Logger.getLogger(IndexerComponent.class.getName()).log
                        (Level.SEVERE, null, ex);
            }
             return;
         }


        // get our reference value, using SHA 256
        resultReference = ScannerChecksum.generateFileChecksum
                 ("SHA-256",file.getAbsolutePath());

        // prepare the path entry
        resultPath = generalizePath(file.getParent());
        //resultPath = anonymize(resultPath);

        // anonymize the filename as well
        resultFilename = file.getName();
        //anonymize(file.getName());

        // get the current date
        resultDate = ""+this.getInstance().getTime();
        
        // now add all this data onto the containers
        writeDB(box.get("reference"),new String[]{
            resultReference,
            resultDate,
            resultDate, getIDserial()});
        // write the file names
        writeDB(box.get("name"),new String[]{resultFilename,
            resultDate,
            resultReference,resultDate, getIDserial()});
        // write the file paths
        writeDB(box.get("path"),new String[]{resultPath,
            resultDate,
            resultReference,resultDate, getIDserial()});

        // perform all the check sum chunking
        Properties sums = checksumFile(file.getAbsolutePath());
        // write all the checksums
        writeDB(box.get("crc32"), new String[]
             {sums.getProperty(FIELD_HASH_CRC32),
              resultDate,
              resultReference,resultDate, getIDserial()});
        writeDB(box.get("md5"), new String[]
             {sums.getProperty(FIELD_HASH_MD5),
              resultDate,
              resultReference,resultDate, getIDserial()});
        writeDB(box.get("sha1"), new String[]
             {sums.getProperty(FIELD_HASH_SHA1),
              resultDate,
              resultReference,resultDate, getIDserial()});

        //if this file is related to windows, get the inner details as well
        win32.index(file, 
                resultReference, resultDate); // no need to add "getIDserial()"

        // increase the counter of processed files
        processedFiles++;

    }


    private Boolean doExit(){

        if(
           (this.getProcess().getStatus() == PAUSED)
        ||
           (this.getProcess().getStatus() == STOPPED)
           ){
            log(INFO,fileList.size() + " files are on the processing queue, "
                +processedFiles+" were processed");
            return true;
        }

        return false;
    }

    /**
     * At each time interval, find new records at our own list and add them
     * to the database containers.
     */
    public void processFiles_every4seconds() {
        if(fileList.isEmpty()) return; // no need to process an empty list

        // only process if we are supposed to be running
        if(getProcess().getStatus() != RUNNING )
            return;

        if(fileList.size() < maxFetch){ // smaller than our maxFetch value
            for(File file : fileList){
                if(doExit())
                    return;
                addToContainers(file); // do the processing
            }
            fileList.clear(); // clear the list
        }
        else
            for(int i = 0; i < maxFetch; ++i){ // just get our quote of files
                if(doExit())
                    return;

                File file = fileList.get(0);
                // do the processing
                 addToContainers(file); // process the file
                 fileList.remove(file); // remove this file
            }

        if(fileList.size() > 0) // no need to output empty results
        log(INFO,fileList.size() + " files are on the processing queue, "
                +processedFiles+" were processed");

        if((counterFile > 0) // have we processed our whole list
                &&(fileList.isEmpty())
                ){
                counterFile = 0; // reset the counter
                // call the stop procedure
                onStop();
        }
    }



        /**
         * This method ensures that our path always follow the same rules before
         * being introduced on the system
         */
        String normalizePath(String pathname){
            String result = pathname;
        return result;
        }

    /**
     * Attempt to make the path as generic as possible, removing details
     * such as the user account name and the differences between paths
     * as seen on the Unix vs Windows changes.
     *
     * We assume UNIX style of paths as the most elegant and use them as default
     */
     public String generalizePath(String pathname){
         String result = pathname;

         // if we are running windows, make the path be unix like
         if(isWindows){
             // replace the back slashes with forward slashes

             // remove user specific settings
//             result = result.replace(folderDocuments, "%documents%");
//             result = result.replace(folderDesktop, "%desktop%");
//             result = result.replace(folderHome, "%home%");

             // remove program files
             // ** missing the program files here
             // ** missing the application data
             result = result.replace(folderWin, "%windows%");

             // convert Windows format to Unix format
             result = result.replace("\\", "/");

             // replace c:/MyFolder/Example with /MyFolder/Example
             result = utils.text.findRegEx(result,"[^\\:]+$",0);
         }


         // common procedures for all operative systems

             // get only the last part of the directory
             result = result.substring(result.lastIndexOf("/")+1);
             result = result.replace("/", "");

         // all done here
         return result;
     }


    /**
     * Let's anonymize a given string
     */
     String anonymize(String content){
          // provide the Scanner_Checksum
         return ScannerChecksum.generateStringSHA256(content);
     }

     /**
     * This method is used to compute several different Scanner_Checksum results
     * for a given file.
      *
      * This a time and resource consuming process but keeping this type of
      * information available will provide a leading advantage when compared
      * to other tools, along with allowing a better integration of our tool
      * with them. (virustotal.com is an example)
      *
      * Look on http://www.timestampgenerator.com/ for popular algorithms
      * being used as years pass
      *
      * This method will convert a file to the following algorithms:
      * - MD5
      * - SHA-1
      * - SHA-256
      * - SHA-512
      * - CRC32
      *
     */
     Properties checksumFile(String filename){
         // create our Scanner_Checksum container
         Properties data = new Properties();

          // provide the Scanner_Checksum for SHA 1
         String  result = ScannerChecksum.generateFileChecksum("SHA-1",filename);
         data.setProperty(FIELD_HASH_SHA1, result);

          // provide the Scanner_Checksum for MD5
         result = ScannerChecksum.generateFileChecksum("MD5",filename);
         data.setProperty(FIELD_HASH_MD5, result);

         // provide the Scanner_Checksum for CRC32
         result = ScannerChecksum.generateFileCRC32(filename);
         data.setProperty(FIELD_HASH_CRC32, result);
         
         // output our set of data with digested algorithm results
         return data;
     }

    @Override
    public void onLoop() {
        // do our looped session
        processFiles_every4seconds();

    }

    @Override
    public void onStop() {
        log(INFO,"Done! "+processedFiles+" files were processed");
        sendMessageForSentinelToStop();

    }

    /** Informs the sentinel that we have stopped indexing files */
    private void sendMessageForSentinelToStop(){
    Properties message = new Properties();
            // the fields that send our message on the expected direction
            message.setProperty(FIELD_FROM, this.getCanonicalName());
            message.setProperty(FIELD_TO, sentinel);
            message.setProperty(FIELD_TASK, "stop");
            message.setProperty(FIELD_MESSAGE, "Time to stop");
            // dispatch the message out the queue
            send(message);
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return getTitle();
    }


    /** output a table with out current status */
    public String getStatus(){

// to save memory, don't display the statistics
//        if(true)
//            return "";

        // store new counting data
        counter.put("name", box.get("name").count());
        counter.put("win32", box.get("win32").count());
        counter.put("path", box.get("path").count());
        counter.put("md5", box.get("md5").count());
        counter.put("crc32", box.get("crc32").count());
        counter.put("sha1", box.get("sha1").count());
        counter.put("reference", box.get("reference").count());


        // Provide data on stored data
        Table store = new Table();
        store.setTitle("Data collected");
        store.setLineVisible(true);
        store.addLine("file names","" + counter.get("name"));
        store.addLine("win32 files","" + counter.get("win32"));
        store.addLine("directories","" + counter.get("path"));
        store.addLine("MD5 hashes","" + counter.get("md5"));
        store.addLine("CRC32 hashes","" + counter.get("crc32"));
        store.addLine("SHA1 hashes","" + counter.get("sha1"));
        store.addLine("SHA2 hashes","" + counter.get("reference"));
        store.add("cellspacing", "9");


        // provide data on operations
        Table operations = new Table();
        operations.setTitle("Operations");
        operations.setLineVisible(true);
        operations.addLine("Received files",""+counterFile);
        operations.addLine("Processed files",""+processedFiles);
        operations.addLine("On queue to process",""+fileList.size());

        // add the average processing of files
        operations.addLine("Average files per minute",""
                + this.calculateAverage(processedFiles)
                );
        operations.add("cellspacing", "9");



        // provide a graphical view of our progress
        Table graph = new Table();
        graph.setTitle("Statistics");
        graph.setLineVisible(true);

        // where the image is located
        String image = "<img src=\"./file?"
                + "name=sentinel_stats.png"
                + "&nocache=true"
                + "\" "
                //+ "width=\"100%\" "
                + "id=\""+utils.math.RandomInteger(0, 9999)+"\" "
                + "alt=\"\">";
        graph.addLine(image);

        // generate a new image
        calculateGraph();

        // Prepare the end result with all tables included
        Table result = new Table();
        result.addLine(
                store.getText(),
                operations.getText(),
                graph.getText()
                );

        // give back the results
        return result.getText();
    }




    /** Create the nice looking graph that we like so much */
    private void calculateGraph(){

      Graph graph = new Graph("","Containers","Knowledge");

       
        graph.addValue( counter.get("name").intValue(), "series1", "Names");
        graph.addValue( counter.get("win32").intValue(), "series1", "Win32");
        graph.addValue( counter.get("path").intValue(), "series1", "Paths");
        graph.addValue( counter.get("md5").intValue(), "series1", "MD5");
        graph.addValue( counter.get("crc32").intValue(), "series1", "CRC32");
        graph.addValue( counter.get("sha1").intValue(), "series1", "SHA1");
        graph.addValue( counter.get("reference").intValue(), "series1", "Unique");

        graph.output("httpdocs", "sentinel_stats.png", 600, 300);

    }


    /** Calculate an average of processed files */
    private long calculateAverage(long processedFiles){
        long result = 0;
        // know how many files were processed since last count
        if(previousProcessed > 0) // ignore this if it is zero
            result = processedFiles - previousProcessed;
        // update the previous processed value
        previousProcessed = processedFiles;

        // add this value to our tracker
        processedFilesTracker.add(result);

        // get the average of 4 secs and multiply by 15 (4 secs * 15 = 1 minute)
        Long average = processedFilesTracker.average() * 15;
        // return our results
        return average;
    }

}

