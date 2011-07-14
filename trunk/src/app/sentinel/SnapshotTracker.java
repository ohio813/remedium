/*
 * This class tracks changes on a given folder. There will be as many instances
 * of this class as folders being tracked.
 *
 * Each instance of this class will report found changes to the main class.
 *
 */

package app.sentinel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import utils.TimeTracker;

/**
 *
 * @author Nuno Brito, 13th of June 2011 in Darmstadt, Germany.
 */
public class SnapshotTracker {

    // settings
    int 
            deepness = 5, // up to how many subfolders should be tracked?
            maxSize      = 10000000; // max size of indexable files (10Mb)

    // objects
    private HashMap<String, FileRecord> records = new HashMap();
    private TimeTracker timeTracker = new TimeTracker();

    private ArrayList<FileRecord>
            recentlyAdded = new ArrayList(),
            recentlyMissing = new ArrayList(),
            recentlyModified = new ArrayList();

    private File
            root = null; // the root where all tracking starts

    // constants
    private static int
            FILE_MISSING = -1,
            FILE_FOUND = 1,
            FILE_CHANGED = 2;


    /** public constructor */
    public SnapshotTracker(final File where){
        // pre flight checks
        if(where.exists() == false)
            return; // doesn't exist? exit.
        if(where.isFile() == true)
            return; // is not a directory? exit.

        // assigned our root to the given folder
        root = where;
    }

    /** Is everything ok with this tracker? */
    public boolean isValid(){
        // can't be null
        if(root == null)
                return false;
        // needs to exist
        if(root.exists() == false)
                return false;
          // passed all tests, return as true
        return true;
    }

    /** Check for changes inside the target folder */
    public boolean track(){
        // preflight checks
        if(isValid() == false) // something is wrong, just exit this operation
            return false;
        // Index all files from our target folder
        indexFiles();
        // find changed files
        findChanges();
        // all done
        return true;
    }


    /** Find and index files */
    private void indexFiles(){
        // clean up the list of recently added files
        recentlyAdded = new ArrayList();
        // crawl our sub directory and check for changes
        ArrayList<File> result = utils.files.findfiles(root, deepness);
        // iterate throught all found files
        for(File file : result){
            // create a record from this file
            FileRecord a = createRecord(file);
            // place this record on our library
            if(a != null){ // only add record if it is different from null
                records.put(a.getId(), a);
                // add the record to the recent list
                recentlyAdded.add(a);
            }
        }
    }


    /** Find files that were found previously and are no longer available*/
    private void findChanges(){

        // reset our storage variables
        recentlyModified = new ArrayList();
        recentlyMissing  = new ArrayList();

        // iterate all records, one by one
        for(FileRecord record : records.values()){
            // This file is old, let's see what happened
            // Does the file still exists?
            File file = new File(record.getAbsolutePath());
            if(file.exists()){
                if(file.isFile()){
                    // has the timestamp of this file changed?
                    if(file.lastModified() != record.lastModified){
                      // modifications dates can be changed, but that doesn't
                      // mean that the contents have changed too, we need to
                      // go deeper and also verify the checksum
                     String hash = app.sentinel.ScannerChecksum.
                         generateFileChecksum("SHA-256",file.getAbsolutePath());
                     // if these two don't match, we have a real change here
                     if(hash.equalsIgnoreCase(record.hashContent)==false){
                         // record this event as a modified file
                         record.status = FILE_CHANGED;
                         recentlyModified.add(record);
                     }
                    }
                }
                if(file.isDirectory()){
                    // someone changed from file to dir, count this as missing
                    record.status = FILE_MISSING;
                    recentlyMissing.add(record);
                }
            }
            else {// file does not exist at all, count it as missing
                record.status = FILE_MISSING;
                recentlyMissing.add(record);
            }
            // update the new status values of this record
            records.put(record.getId(), record);
        }
    }

    public ArrayList<FileRecord> getRecentlyAdded() {
        return recentlyAdded;
    }

    public ArrayList<FileRecord> getRecentlyMissing() {
        return recentlyMissing;
    }

    public ArrayList<FileRecord> getRecentlyModified() {
        return recentlyModified;
    }

    


    /** Create a new record based on a given file */
    private FileRecord createRecord(File file){
        // the record holder
        FileRecord record = new FileRecord();
        // preflight checks
        if(file.length() > maxSize)
            return null; // if file is bigger than what we allow, stop the show.

        // fill with data
        record.absolutePath = file.getAbsolutePath();
        record.dateIndexed = timeTracker.getTime();
        record.filename = file.getName();
        record.filepath = file.getPath();
        record.hashContent = app.sentinel.ScannerChecksum.generateFileChecksum
                 ("SHA-256",file.getAbsolutePath());
        record.lastModified = file.lastModified();
        record.lastVerified = timeTracker.getTime();
        record.status = FILE_FOUND;

        // are we unique?
        if(records.containsKey(record.getId())){
            //System.out.println("Not Unique: " + record.getAbsolutePath());
            return null; // we're not unique, let's keep the first record
        }

        //System.out.println("Adding: " + record.getAbsolutePath());


        // all done
    return record;
    }

    /** Provide an array with all the changes detected by the tracker*/
    public ArrayList<FileRecord> getChanges(){

        ArrayList<FileRecord> output = new ArrayList();

        for(FileRecord record : records.values()){
            if(record.getStatus() != FILE_FOUND)
                output.add(record);
        }

    return output;
    }


    /** Manage all aspects regarding each recorded file */
    public static class FileRecord{
        // objects
        private long
                dateIndexed,    // when we first indexed this file
                lastModified,   // time stamp of last modification of this file
                lastVerified;   // time stamp of last time we verified this file
        private int
                status;         // possible status (Missing, Present, etc)
        private String
                filename,       // the file name
                filepath,       // full path where the file is located
                hashName,       // checksum of file name + file path
                hashContent,    // checksum of file contents
                absolutePath;   // the absolute path value

        public long getDateIndexed() {
            return dateIndexed;
        }

        public void setDateIndexed(long dateIndexed) {
            this.dateIndexed = dateIndexed;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilepath() {
            return filepath;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        public String getHashContent() {
            return hashContent;
        }

        public void setHashContent(String hashContent) {
            this.hashContent = hashContent;
        }

        public String getHashName() {
            return hashName;
        }

        public void setHashName(String hashName) {
            this.hashName = hashName;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public long getLastVerified() {
            return lastVerified;
        }

        public void setLastVerified(long lastVerified) {
            this.lastVerified = lastVerified;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public void setAbsolutePath(String absolutePath) {
            this.absolutePath = absolutePath;
        }

        public String getId() {
            return this.hashContent + this.absolutePath;
        }


    }



}
