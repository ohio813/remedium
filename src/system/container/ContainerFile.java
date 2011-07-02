/*
 * This class creates the text files that contain the records from containers.
 *
 * Typically, one container will go throught each record on his database and
 * submit the retrieved values to this class.
 *
 * This class is responsible for writing each record on a file. When a file
 * reaches the maximum number of allowed records then it will start a new
 * one.
 *
 * It is a tricky operation, we first write data onto files using temporary
 * names until they are filled up to the max capacity.
 *
 * Then, we write the definitive file name that contains info about the contents.
 *
 */

package system.container;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nuno Brito, 25th of June 2011 in Darmstadt, Germany.
 */
public class ContainerFile {

    // should we output debug messages?
    private boolean debug = true;
    // maximum number of accepted records per file
    private long maxRecords = 10000;
    
    
    // our array of files
    private ArrayList<fileRecord> storage = new ArrayList();
    // our current working file
    private fileRecord currentFile = null;
    // where our files will be located
    private File rootFolder = null;
    // identification of our files
    private String id;

    
    /** public constructor for this class */
    public ContainerFile(File assignedRootFolder, String assignedId){
        this.rootFolder = assignedRootFolder;
        this.id = assignedId;
    }

    /** Add another record to our file*/
    public boolean add(String record, long date){
        // preflight checks
        if(this.doPreflightChecks(date)==false)
            return false;

        // write the record onto the file
        write(record);
        // all done
        return true;
    }

    /** Write the new record inside the current file*/
    private void write(String record){
        // write the line
        currentFile.write(record);
        // if we are over the limit, create a new file
        if(currentFile.count > maxRecords){
            // close the current file
            currentFile.close();
            // open a new current file
            createNewCurrentFile();
        }
    }

    /** Close down our file container, write the new filenames */
    public boolean close(){
        // close the current file
        currentFile.close();
        // rename all files to distinctive names
        long
                part = 1, // part number
                total = storage.size(); // number of parts
        // iterate each file
        Iterator<fileRecord> iterate = storage.iterator();
        while(iterate.hasNext()){
            fileRecord file = iterate.next();
            // do the renaming
            file.rename(""+part, ""+total);
            // increase the counter
            part++;
        }

        return true;
    }

    /** Creates a new temporary file */
    private boolean createNewCurrentFile(){
        currentFile = new fileRecord(rootFolder, id, debug);
            // prepare our file to store records
            boolean result = currentFile.start();
            if(result == false) // failed to create file? exit!
                return false;
            // add this file to our storage
            storage.add(currentFile);
            // all done
            return true;
    }

    /** Do all the checks when adding a new file record*/
    private boolean doPreflightChecks(long date){
        // if the currentFile is null, it means we need to create a new one
        if(currentFile == null){
            return createNewCurrentFile();
        }

        // add the dates
        if(date < currentFile.since)
            currentFile.since = date;
        if(date > currentFile.until)
            currentFile.until = date;

        // all done, ready to go
        return true;
    }

}


/** defines the typical storage file */
class fileRecord{
    long
            version = 1, // version of our file record
            since = System.currentTimeMillis(), // date of the earliest record
            until = 0, // date of oldest record
            count = 1 // total number of records included
            ;

    String
           id,        // unique identifier
           checksum,  // SHA2 checksum
           tempName,  // temporary filename assigned to this file
           finalName; // final name that we will use
    File 
           rootFolder; // where this file will be located
    BufferedWriter
            file;      // the file where data will be written
    boolean
            debug = false;
    
    /** public constructor*/
    public fileRecord(File assignedRootFolder, String assignedId,
            boolean debug){
        this.rootFolder = assignedRootFolder;
        this.id = assignedId;
        this.debug = debug;
    }

    public boolean start(){
        if(file != null) // we can only start once
            return false;
        // create a temporay file
        boolean result = createTemp();
        return result;
    }

    /** generate a temporary file to store the records */
    public boolean createTemp(){
        // generate a temporary name
        tempName = "temp-" + System.currentTimeMillis() + ".txt";
        // create this file
        File fileTemp = new File(rootFolder, tempName);
        // what is the full path of our file?
        String where = fileTemp.getAbsolutePath();
        try {
            // open the file for writing
            file = new BufferedWriter(new FileWriter(where));
        } catch (IOException ex) {
            // an error occurred!
            Logger.getLogger(fileRecord.class.getName()).log
                    (Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public void rename(String part, String total){

        checksum =
                app.sentinel.ScannerChecksum.generateStringSHA256(tempName);

        final String newName =
                  "db-" + id
                + "_since-" + since
                + "_until-" + until
//                + "_part-" + part
//                + "_total-" + total
                + "_count-" + (count - 1)
                + "_checksum-" + checksum
                + "_v-" + version
                + ".txt"
                ;

        // open the temp file
        File fileTemp = new File(rootFolder, tempName);
        // open the new file
        File fileNew = new File(rootFolder, newName);
        try{
        // do the renaming part
        fileTemp.renameTo(fileNew);

        if(debug) System.out.println("Renaming from " + tempName
                + " to " + newName);
        }catch (Exception e){
        System.err.println("Exception when rename file from '"
                + tempName
                + "' to '" + newName + "': " + e.getLocalizedMessage());
        }


    }

    /** write a line onto this file */
    public boolean write(String line){
        try {
            // write the line
            file.write(line + "\n");
        } catch (IOException ex) {
            // an error occured!
            Logger.getLogger(fileRecord.class.getName()).log
                    (Level.SEVERE, null, ex);
            return false;
        }
        // increase our counter
        count++;
      return true;
    }

    /** Close our file */
    public boolean close(){
        try {
            // close down our file
            file.close();
        } catch (IOException ex) {
            // an error occured!
            Logger.getLogger(fileRecord.class.getName()).log
                    (Level.SEVERE, null, ex);
        }
        return true;
    }
}
