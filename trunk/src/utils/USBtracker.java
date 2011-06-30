/*
 * This class detects whenever new USB drives are inserted onto the computer
 * It should:
 *  - Detect when multiple drives are inserted
 *  - Maintain a list of removable drives
 *  - When a drive is removed, update the list
 */

package utils;

import java.io.File;

/**
 *
 * @author Nuno Brito, 11th of June 2011 in Darmstadt, Germany
 */
public class USBtracker {

    public final int // indicate possible actions that we detect
            hasNewDrive = 1, // at least one drive was added
            driveRemoved = -1, // at least one drive was removed
            noChanges = 0; // no changes to report


    private Boolean
            isWindows = false;

    private String
            unixRoot = "/media";

    private String[]
            currentDrives, // lists the available drives on this machine
            previousDrives; // used to extract changes
    private String
            newDrives;  // used to place the newer drives

    /** Public constructor */
    public USBtracker(){

        // are we Windows?
        isWindows = utils.currentOS.isWindows();

        // are we Mac or Ubuntu?
        if (utils.currentOS.isMac()){
            unixRoot = "/Volumes"; // oerhaps BSD is also this folder?
        }
          else
        if (utils.currentOS.isUnix()){
            unixRoot = "/media"; // for Ubuntu and Debian
        }

        // find our drives
        updateDrives();
        // ensure that we have a clean initial state
        previousDrives = currentDrives;

    }


    /** If a new drive has been inserted, this method returns true */
    public int updateDriveStatus(){
        // update the current situation
        updateDrives();
        // define initial state
        int result = noChanges;
        // this is a bit flawed. If this method is not called fast enough, then
        // we will risk not having detected anything.
        // still, better to live with this risk and keep things simple for now
        if(currentDrives.length > previousDrives.length){
            outputNewDrives();
            result = hasNewDrive;
        }
        else // no changes to report
            if(currentDrives.length == previousDrives.length){
                result = noChanges;
            }
        else // at least one drive was removed
            if(currentDrives.length < previousDrives.length){
                outputNewDrives(); // update current situation
                result = driveRemoved;
            }

        // return true if there is a new drive
        return result;
    }

    /** New drives were added, output this difference on newDrives[] */
    private void outputNewDrives(){
        String result = ""; // where the new drives will be temporarily stored
        String older = ""; // where older drives will be temporarily stored
        // populate a string with the older drives
        for(String old : previousDrives)
            older = older.concat(old + ";");
        // iterate all the newer drives
        for(String newer : currentDrives)
            // if the older string does not contain a new string..
            if(older.contains(newer)==false)
                // ..add it up to the end result
                result = result.concat(newer + ";");

       // update the new Drives variable
       newDrives = result;
    }


    /** Updates the drives that are currently connected on the system */
    private void updateDrives(){
        // grab the old value
        previousDrives = currentDrives;
        // if we are windows, then pick the drive letters
        if(this.isWindows)
            this.updateWindowsDrives();
        else // otherwise check a specific folder for changes
            this.updateUnixDrives();
    }

    /** List current drives */
    private void updateWindowsDrives(){
           File[] roots = File.listRoots();

           String result = "";
           // iterate all found drives
           for(File f: roots) // placed results on a string
            result = result.concat(f.getAbsolutePath() + ";");
           // update variable with our results
           currentDrives = result.split(";");
    }

    /** List current drives */
    private void updateUnixDrives(){

        File dir = new File(unixRoot);
        File[] folders = dir.listFiles();

        String result = "";
        // iterate all folders
           for(File folder: folders) // placed results on a string
               if(folder.isDirectory())
                   result = result.concat(folder.getAbsolutePath() + ";");
           // update variable with our results
           currentDrives = result.split(";");
    }

    /** Return the drives added to the system */
    public String[] getRemovableDrives() {
        return newDrives.split(";");
    }

    /** Return the number of removable drives that were detected */
    public int count(){
        return this.getRemovableDrives().length - 1;
    }

}
