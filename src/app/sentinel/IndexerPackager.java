/*
 * The IndexerPackager will provide an object that other classes (roles) can use
 * to store new files that are found during a processing event.
 *
 * These files will then be split onto packages that are dispatched as messges
 * to the Indexer.
 *
 * Packages are sent on a time or number basis. For example, if we have only
 * 10 files to process, it will sent them after n seconds. If we have more
 * than 10 000 files to process, it will send only 10 000 and iteratively
 * send more 10 000 at each nn seconds until the whole queue is left empty.
 *
 * This decoupling allows the Indexer to slowly digest files without stress.
 */

package app.sentinel;

import system.core.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import system.mq.msg;

/**
 *
 * @author Nuno Brito, 20th of March 2011, Darmstadt, Germany.
 */
public class IndexerPackager implements msg{

    private long lock = 0;

    // list of file records
    private ArrayList<File> 
            fileList = new ArrayList<File>(),
            fatFileList = new ArrayList<File>(),
            folderList = new ArrayList<File>();
    
    private int 
            counterFile = 0,
            counterFatFile = 0,
            counterFolder = 0;

    private Scheduler
            schedule; // the clean up lady

    private Component role;

    /**
     * Start our instance and setup the unlock code for public methods
     */
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public IndexerPackager(long unlock, Component assignedRole){
        if(lock!=0) return; // prevent any weird thing from happening here
        lock = unlock;
        // get our nice role
        role = assignedRole;

        schedule = new Scheduler(lock, this, role); // create the clean up lady
        schedule.start();

    }

    /** Add a new file to our list */
    public void addFile(long unlock, File file){
        if(lock != unlock) return;
        fileList.add(file);
        counterFile++;
    }
    /** Add a new file to our list */
    public void addFatFile(long unlock, File file){
        if(lock != unlock) return;
        fatFileList.add(file);
        counterFatFile++;
    }
    /** Add a new folder to our list */
    public void addFolder(long unlock, File file){
        if(lock != unlock) return;
        folderList.add(file);
        counterFolder++;
    }

    /** Get the number of files in our list */
    public int getFileCount(long unlock){
        if(lock != unlock) return -1;
        return counterFile;
    }
    
   /** Get the number of fat files in our list */
    public int getFatFileCount(long unlock){
        if(lock != unlock) return -1;
        return counterFatFile;
    }

    /** Get the number of folders in our list */
    public int getFolderCount(long unlock){
        if(lock != unlock) return -1;
        return counterFolder;
    }

    /** Get an array of our files up to a specified limit */
    public ArrayList<File> getFiles(long unlock, int maxFetch){
        if(lock != unlock) return null;

        // the holder of our results
        ArrayList<File> result = new ArrayList<File>();

        if(fileList.size() < maxFetch){ // Is it below our threshold?
            result.addAll(fileList); // get them all in that case
            fileList.clear(); // cleam them up
        }
        else
           for(int i = 0; i < maxFetch; ++i){ // iterate throught n records
               File temp = fileList.get(0); // get the oldest one from the index
               fileList.remove(temp); // safe remove (0 could have changed in the meanwhile)
               result.add(temp); // add the first element onto our result
        }

        if(result.size() > 0) // no need to output empty results
        log(DEBUG,result.size()+" files were dispatched"
                //+ ", "+fileList.size()+" files on queue to dispatch"
                );
        return result;
    }


    /** output a log msg, using the assigned role container */
    private void log(int gender, String message){
        role.log(//lock,
                gender, message);
    }


}


/**
 * The purpose of the scheduler is to clean up the list from a given
 * IndexerPackager and send out the nicely packaged records onto somewhere
 * else. At this moment it makes sense to send them out to the Indexer role
 * from the Quaestor application.
 *
 * @author Nuno Brito, 20th of March of 2011 at Germany.
 */
class Scheduler extends Thread implements msg{
    private long 
            lock = 0;

    private IndexerPackager pack;
    private Component role;


    private int
            time_to_wait = 3, // wait a few seconds before the next loop
            maxFetch     = 10000; // how many records do we want per loop?

    /** constructor */
    public Scheduler(long unlock, IndexerPackager assignedPackager,
            Component assignedRole){
        // set up our lock key for public methods
        lock = unlock;
        pack = assignedPackager;
        role = assignedRole;
    }

    /** send a msg using the assigned role as dispatcher */
    private void send(Properties msg){
         role.send(lock, msg);
    }

    /** Set up a properties object to carry our data onto the processing role */
    private Properties prepareMessage(){
        Properties msg = new Properties();
        msg.setProperty(FIELD_FROM, sentinel_scanner);
        msg.setProperty(FIELD_TO, sentinel_indexer);
        msg.setProperty(FIELD_TASK, PROCESS);
        return msg;
    }


    @Override
    public void run(){
        // prepare our box for delivery of data
        Properties box = prepareMessage();

        while(true){
            // only dispatch files when we are running
          if(role.getProcess().getStatus() == RUNNING ){
              // get a new batch of files to dispatch
            ArrayList<File> results = pack.getFiles(lock, maxFetch);
            // if we have files, send them over the wire
            if((results != null)
                    &&(results.size() > 0)){

                // this is fast but buggy
                    String data = results.toString();
                    data = data.substring(1, data.length()-1); //

                    box.setProperty(msg.FIELD_MESSAGE, data);

                    // add this value to ensure we don't miss a single file
                    box.setProperty(msg.FIELD_COUNT, ""+results.size());

                    // send this box to the other side
                    send(box);
            }
          }
            // sit and wait for a given time
            utils.time.wait(time_to_wait);
        }
    }

    
}