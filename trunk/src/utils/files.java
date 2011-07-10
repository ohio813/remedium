/*
 * Methods to ease the handling of files
 */

package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Nuno Brito, 6th of June 2011 in Darmstadt, Germany.
 */
public class files {






        /**
         * This method will read the contents from a text file onto a string
         *
         * @author Nuno Brito
         * @version 1.0
         * @date 2010/06/07
        */
        public static String readAsString(final File textFile){

            StringBuffer result = null;
            BufferedReader br = null;
            try {
            FileReader f = new FileReader(textFile);
            br = new BufferedReader(f);
            String line;
            result = new StringBuffer();
            try {
                while ((line = br.readLine()) != null) {
                    result.append(line + "\n");
                }
            } catch (Exception ex) {
            } finally {
                try {
                    br.close();
                } catch (IOException ex) {
                }
            }
            return result.toString();
        } catch (FileNotFoundException ex) {
        }
            return result.toString();
            }


// Deletes all files and subdirectories under dir.
// Returns true if all deletions were successful.
// If a deletion fails, the method stops attempting to delete and returns false.
public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
        String[] children = dir.list();
        for (int i=0; i<children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success) {
                return false;
            }
        }
    }
    // The directory is now empty so delete it
    return dir.delete();
}

/**
 * Find all files in a given folder and respective subfolders
 * @param where A file object of the start folder
 * @param maxDeep How deep is the crawl allowed to proceed
 * @return An array containing all the found files, returns null if none is
 * found
 */
 public static ArrayList<File> findfiles(File where, int maxDeep){

    File[] files = where.listFiles();
    ArrayList<File> result = new ArrayList<File>();

    if(files != null)
    for (File file : files) {
      if (file.isFile())
         result.add(file);
      else
      if ( (file.isDirectory())
         &&( maxDeep-1 > 0 ) ){
            // do the recursive crawling
            ArrayList<File> temp = findfiles(file, maxDeep-1);

                for(File thisFile : temp)
                        result.add(thisFile);
      }
    }
    return result;
    }

  /** Get all the drives listed under windows, ignore read only ones */
    public static String getWindowsDrives(boolean ignoreReadOnly){

     // get a list of all drives
     File[] roots = File.listRoots();
     String result = "";

     // iterate all reported drives
    for(File folder: roots){
       if(ignoreReadOnly==false) // include drives such as CD-ROM and the such
            result = result.concat(folder.getAbsolutePath()+";");
       else
       if(folder.canWrite()) // only add disk drives (USB drives included)
            result = result.concat(folder.getAbsolutePath()+";");
     }
     // remove the last separator
     result = result.substring(0,result.lastIndexOf(";"));

     return result;
    }

      /** Provides the root folders according to the OS that we are using */
    public static String getRootFolders(){
        if(utils.currentOS.isWindows())
            return getWindowsDrives(true);
        if(utils.currentOS.isUnix())
            return "/";
        if(utils.currentOS.isMac())
            return "/";

        return "";
    }

  /** Provides a root folder according to the OS that we are using */
    public static String getRootFolder(){
        if(utils.currentOS.isWindows())
            return "c:\\";
        if(utils.currentOS.isUnix())
            return "/";
        if(utils.currentOS.isMac())
            return "/";

        return "";
    }


/**
    * Ensures that we can pick on a value and present a readable
    * size of the file instead of plain bytes
    *
    * @param size
    * @return String
    */
   public static String humanReadableSize(Long size){

       long l = size;
       //Long.parseLong(size.trim());
       String output;

                long b = 0;
                long MEGABYTE = 1024L * 1024L;
                long KILOBYTE = 1024;
                if (l > MEGABYTE){
                    b = l / MEGABYTE;
                    output = Long.toString(b)+" Mb";}
                else
                if (l > KILOBYTE){
                    b = l / KILOBYTE;
                    output = Long.toString(b)+" Kb";}
                else
                    output = size+" bytes";
    return output;
   }


   /** get the folder where our user documents are placed */
   public static synchronized String getDocumentsDirectory(){

       String result = "";

       java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            javax.swing.JFileChooser jFileChooser1 = new javax.swing.JFileChooser();
            String myresult = jFileChooser1.getFileSystemView().getDefaultDirectory()
                    .getAbsolutePath();
            }
        });
               
            return result;
   }

   /** get the desktop folder  */
   public static synchronized String getDesktopDirectory(){
               javax.swing.JFileChooser jFileChooser1 = new javax.swing.JFileChooser();
            return jFileChooser1.getFileSystemView().getHomeDirectory()
                    .getAbsolutePath();
   }

   /** get our home folder (under windows this value is not certain)*/
   public static synchronized String getHomeDirectory(){
               javax.swing.JFileChooser jFileChooser1
                       = new javax.swing.JFileChooser();
            return jFileChooser1.getFileSystemView().getParentDirectory(
                   jFileChooser1.getFileSystemView().createFileObject
                   (getDesktopDirectory())
                   ).getAbsolutePath();
    }


   /** create a folder along with respective parent folders if needed */
   public static Boolean mkdirs(String folder){
       boolean result = false;

        File docs = new File(folder);
        result = docs.mkdirs();

        return result;
    }

  /** create a folder along with respective parent folders if needed */
   public static Boolean mkdirs(File docs){
       boolean result = false;
        result = docs.mkdirs();
        return result;
    }


   /**
     * This method saves the contents from a string to a file
     *
     * @author Nuno Brito
     * @version 1.0
     * @date 2010/06/06
    */
   public static boolean SaveStringToFile(File inputFile, String inputString){
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(inputFile));
            out.write(inputString);
            out.close();
            }
            catch (IOException e){
                System.out.println(e.getMessage());
                return false;
            }
        return true;
	}


   /**
   * Load a text file contents as a <code>String<code>.
   * This method does not perform enconding conversions
   *
   * @param file The input file
   * @return The file contents as a <code>String</code>
   * @exception IOException IO Error
   */
  public static String deserializeString(File file)
      // code copied from http://goo.gl/5qa3y
    throws IOException {
      int len;
      char[] chr = new char[4096];
      final StringBuffer buffer = new StringBuffer();
      final FileReader reader = new FileReader(file);
      try {
          while ((len = reader.read(chr)) > 0) {
              buffer.append(chr, 0, len);
          }
      } finally {
          reader.close();
      }
      return buffer.toString();
  }


  /* Provide the relevant folders.
   * We want to get the folders such as desktop, documents and so on.
   */
  static public String[] getHotFolders(){
      // get the documents folder
      String result = "";

      if(getDocumentsDirectory().length() > 0)
         result = result.concat(getDocumentsDirectory()) + ";";

      if(getDesktopDirectory().length() > 0)
         result = result.concat(getDesktopDirectory()) + ";";

      // if we are inside Windows Vista or 7, get some extra candy folders
      if(utils.currentOS.isWindows()){
        String home = getHomeDirectory();
          if(home.contains("\\Users")){
          // add the downloads folder
           result = result.concat(home + "\\Downloads") + ";";
           result = result.concat(home + "\\Documents") + ";";
          }
      }
      else
      if(getHomeDirectory().length() > 0)
         result = result.concat(getHomeDirectory()) + ";";


      System.out.println("-->"+result);
      return result.split(";");
  }
}
