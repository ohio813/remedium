package system.raw;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;




/**
 * Some links:
 * http://kishorekumar.net/pecoff_v8.1.htm
 * http://blogs.msdn.com/b/oldnewthing/archive/2006/12/21/1340571.aspx
 *
 * @author Nuno Brito, 26th of March 2011 in Germany
 */
public class image_win_executable {

//    // variables
//      private String
//          version,
//          language,
//          architecture;

      private RandomAccessFile file;

      // objects
      private image_dos_header dos_header;
      private image_nt_header nt_header;
      private boolean
          hasVersion = false;

      /** Get the architecture compiled for this file (x64, x86) */
    public String getArchitecture() {
        return nt_header.getFileArchitecture();
    }

    /** Have we found a version mentioned in this file? */
    public boolean hasVersion() {
        return nt_header.hasVersion();
    }

    /** To which language was it compiled to? */
    public String getLanguage() {
        return nt_header.getFileLanguage();
    }

    /** Get the version reported on this file */
    public String getVersion() {
        return nt_header.getFileVersion();
    }


/**
 * Read the details from a given file, assuming it is a Windows executable.
 * @param filename
 * @return True if the read was successful
 */
   public Boolean read (String filename)
       {

        try {

            file = new RandomAccessFile(filename, "r");



        } catch (FileNotFoundException ex) {
            log("ERROR","Read operation failed. Exception occured");
        }

  // read the DOS header of this file
        dos_header = new image_dos_header();
        dos_header.read(file);

  // read the NT header inside the file
        nt_header = new image_nt_header();
        try{
        nt_header.read(file, filename, dos_header);
           }
        catch (Exception e){
            log("ERROR", "Failed to read " + filename);
            return false;
        }
        
        // inherit the value from our nt_header
        this.hasVersion = nt_header.hasVersion();

        return hasVersion;
    }

 private void log(String gender, String message){
     //TODO we should default log messages to our system
     //global.rem.cliens.log.out
     //("image_win_executable", "["+gender+"] "+message);
     System.out.println("[image_win_executable]["+gender+"] "+message);
 }
 
}