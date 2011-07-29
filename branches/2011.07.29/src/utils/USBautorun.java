/*
 * Handle autorun.inf files that are common to find at removable media. This
 * class works across popular OS such as Windows, Mac and Unix.
 *
 * This class should:
 *      - Rename autorun.inf files onto a harmless extension
 *      - Create a folder called "autorun.inf" with safe permissions
 *      - React properly in case of errors (write not authorized, ...)
 */

package utils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Nuno Brito, 12th of June 2011 in Darmstadt, Germany
 */
public class USBautorun {

    public final String
            autorunFile = "autorun.inf"; // the inphamous phile

    private File
            autoFolder,
            immuneFolder,
            autoFile;

    /** Process autorun files on a given location*/
    public Boolean immunize(String drive){

        autoFolder = new File(drive);
        // autorun is placed on the root of the drive, find it.
        autoFile = new File(autoFolder + File.separator + autorunFile);

  // check if it was previously immunized: a folder "autorun.inf" must exist
        if(autoFile.isDirectory()){
            // all is done, no need to proceed
            return true;
        }

  // check if a file called "autorun.inf" already exists
        if(autoFile.exists() && autoFile.isFile()){
            // rename the file to a safe name
            rename(autoFile);
        }

  // check if we can create a directory entitled Autorun.inf
        if(autoFile.exists()){
            //System.out.println("USBautorun: Failed to rename 'autorun.inf'");
            return false;
        }

  // create the directory
        createFolder(drive);

        //System.out.println("USBautorun: " + drive + " is immunized");
        return true;
    }


    /** Rename a given autorun.inf file to a harmless extension */
    public File rename(File file){
        // create the new autorun.inf file
        File newFile = new File // use autorun plus the current timestamp
                (
                autoFolder
                + File.separator
                + "autorun."
                + System.currentTimeMillis()
                + ".inf"
                );

        // do the renaming operation
        Boolean result = file.renameTo(newFile);

        if(result == null)
            return null; // something went wrong, return null

        return newFile ;
    }

    /** Create an autorun.inf folder with all expected security permissions*/
    public Boolean createFolder(String Drive){
        // assign the new file
        immuneFolder = new File(autoFolder + File.separator + autorunFile);
        // create the autorun.inf folder
        Boolean result = immuneFolder.mkdir();
        // if the previous operation failed, return false.
        if(result == false)
            return false;


        // this command will change according to the OS where we are running
        String attrib = "cls";

        // protect the folder (Windows)
        if(utils.currentOS.isWindows())
            attrib = "attrib +s +h +r " + immuneFolder.getAbsolutePath();

       // protect the folder if we are on Unix
       if( utils.currentOS.isUnix() || utils.currentOS.isMac()){

            // create additional folders:
        File con = utils.testware.createFolder(immuneFolder, "con");
        File nul = utils.testware.createFolder(immuneFolder, "Nul.protected");

           attrib =
                     "chmod 644 " + immuneFolder.getAbsolutePath() + ";"
                    +"chmod 644 " + con.getAbsolutePath() + ";"
                    +"chmod 644 " + nul.getAbsolutePath() + ";"
                    ;
        }
            try {
                // run a system command
                Runtime.getRuntime().exec(attrib);
            } catch (IOException ex) {
            }
            //System.out.println(attrib);

        // return true if we have a new autorun.inf folder
        return immuneFolder.exists();
    }

}