/**
 * This is the class that does the dirty work of finding files on disk. It
 * will read the settings from this role instance using public methods and
 * a private password.
 */

package app.sentinel;

import java.io.File;
import system.mq.msg;

/**
 *
 * @author Nuno Brito, 20th of March 2011 at Darmstadt, Germany.
 */
public class Scanner_ThreadType extends Thread implements msg{

     // settings
        long
                lock = 0; // special lock key

     // objects
        ScannerComponent role = null;
        IndexerPackager pack;

    public Scanner_ThreadType(long assignLock, ScannerComponent assignedRole) {
            // start the thread
            role = assignedRole;
            lock = assignLock;
            // start up the IndexerPackager with our custom secret word
            pack = new IndexerPackager(lock, role);
    }

    private String getFoundStatus(){
         return "I've found "+pack.getFolderCount(lock)
            + " folders and " + pack.getFileCount(lock) + " files";}

    /**
     * Verify if we are paused or not. If a pause has been requested,
     * place this thread on halt until this status is changed
     */
    synchronized void checkPausedStatus(){
        if(role.getStatus(lock)==PAUSED){
            // if someone paused, trap the execution here
                log(INFO,"Scan is paused, so far "+getFoundStatus());
            // handle resume
            while(role.getStatus(lock)==PAUSED)
                utils.time.wait(1);  // wait for a second
                log(INFO, "Scanning has resumed");
            }
    }

    /** Change the operational status of this role */
    void setStatus(int newStatus){
       role.setOperationalStatus(lock, newStatus);
    }

    /** Are we running or not? */
    synchronized Boolean isRunning(){
    return role.getStatus(lock) != STOPPED;
    }

    /** slowdown the processing */
    synchronized void doThrottle(){
        utils.time.wait( role.getThrottle(lock) );
    }

    /** call the monitoring and management routines */
    synchronized void monitor(){
        checkPausedStatus();
        doThrottle();
    }

    /**
     * Kick start the thread. All the methods within this class that are
     * scanning files and folders will use the monitor() method to check
     * the status of the scanner roler for changes in the status and so on.
     */
    @Override
    public synchronized void run() {

        log(INFO, "Scanning operation has started");
        // Set the status to runnning
        setStatus(RUNNING);
        //log(INFO,"Scanning");

        // do the scanning, get the "Where" to know where to start
        File folder;
//            folder = new File(role.getWhere(lock));
//            findfiles(folder, role.getDepth(lock) );
        
        
        // iterate all mentioned locations (if more than one)
        for(String where : role.getWhere(lock).split(";")){
            folder = new File(where);
            findfiles(folder, role.getDepth(lock) );
        }

        log(INFO,"Scanning operation was completed, "+getFoundStatus());
	}



/**
 * This is a modified version of the findFiles that you find at the utils.files
 * package. The main difference is that insteas of outputting the results to
 * a normal ArrayList, it will output them to a special class that will
 * periodically ship them over the wire to another application for processing
 * @param where
 * @param maxDeep
 */
public void findfiles(File where, int maxDeep){

    File[] files = where.listFiles();

    if(files != null)
    for (File file : files) {

        monitor(); // handle other events
        if(isRunning()==false) return; // check if we are running or not

      if (file.isFile())
          pack.addFile(lock, file);
      else
      if ( (file.isDirectory())
         &&( maxDeep-1 > 0 )
         ){
            pack.addFolder(lock, file); // add this folder to the list
            findfiles(file, maxDeep-1); // do the recursive crawling
          }
        }//for
    }//findfiles


    // output a log msg through the assigned role
    private void log(int gender, String message){
        role.log(//lock,
                gender, message);
    }


}
