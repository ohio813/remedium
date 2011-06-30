/*
 * TestWare is a class intended to help developers implement test cases.
 *
 * It adds methdods whose purpose is to create the necessary conditions to
 * ensure that each test condition is replicable and automated.
 *
 * This class is different from the generic tweaks or utils (amongst others)
 * since it is targetted for exclusive use inside test cases scenarios.
 */

package utils;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Nuno Brito, 13th of June 2011 in Darmstadt, Germany.
 */
public class testware {



  /** Create a set of folders and files for our testing purposes */
  public static Boolean createTemporaryFoldersAndFiles(String where) {

     File root = new File(where);

     if(root.exists())
         if(root.isDirectory())
            utils.files.deleteDir(root);

     if(root.exists()){
         System.err.println("Failed to delete the '"+where+"' folder");
         return false;
     }

     Boolean result = root.mkdir();

     if(result == false){
        System.err.println("Something went wrong while creating the directory");
        return false;
      }

     // create a bunch of sub folders
        File test1 = createFolder(root, "test1");
        File test2 = createFolder(root, "test2");
        File test3 = createFolder(root, "test3");
        File test4 = createFolder(root, "test4");

     // Create a bunch of files inside these
        createFile(root, "Hello1.txt");
        createFile(root, "Hello2.txt");
        createFile(root, "Hello3.txt");
        createFile(root, "Hello4.txt");
        createFile(test1, "Hello5.txt");
        createFile(test1, "Hello6.txt");
        createFile(test2, "Hello7.txt");
        createFile(test2, "Hello8.txt");
        createFile(test3, "Hello9.txt");
        createFile(test4, "Hello10.txt");

     return true;
     }


   /** Create folders à lá carte*/
     public static File createFolder(File parent, String where){
         File newFolder = new File (parent, where);
         Boolean result = newFolder.mkdir();
         if(result == false){
             System.err.println("Failed to create '" + where + "'");
             return null;
         }
      return newFolder;
     }

     /** Create temporary files à là carte*/
     public static File createFile(File parent, String name){
         File newFile = new File (parent, name);
         Boolean result;
        try {
            result = newFile.createNewFile();
            if(result == false){
             System.err.println("Failed to create '" + name + "'");
             return null;
            }

        } catch (IOException ex) {
            System.err.println("Failed to create '" + name + "'");
             return null;
        }
      return newFile;
     }

     /** Count the number of records on a given databae*/
     public static long dbCount(String webAddress, String who, String dbName){
         // repeat the same test..
            String result = utils.internet.getTextFile
             ("http://"+webAddress
             +"/"+who
             +"?db="+dbName
             +"&action=count");
            // remove annoying white spaces from result
            result = result.trim();
            // we can't receive an empty result here
            if(result.length() == 0)
                return -1; // no connection

            long count = 0;

            try{
            count = Long.parseLong(result);
        } catch (Exception e){
            return -2; // exception occurred
        }
        return count;
     }


}
