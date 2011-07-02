/*
 * Track changes to file inside a specific folder and respective sub-folders.
 *
 * This test case will:
 *      - Create a new Snapshot Tracker instance
 *      - Create a set of files and folders to be tracked
 *      - Instantiate the tracker
 *      - Introduce changes (new files)
 *          - Verify that changes were detected
 *          - Repeat modifications
 *          - Re-Verify that changes were detected
 *      - Delete temporary folders
 */

package app.sentinel;

import app.sentinel.SnapshotTracker.FileRecord;
import java.io.File;
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  Rules of the testing game:
 *
 *      - If you make changes, add another entry @author below the previous one
 *      - Describe the purpose of the test case and which tests will be done
 *      - Split each group of tests onto its own method and use intuitive names
 *      - Test if something works as intended and also how it reacts to errors
 *      - Add System.out.println comments when:
 *          - A specific test is starting
 *          - A test has finished
 *      - Add an empty line of text between each test to keep results readable
 *      - Be verbose, explain to people what you are doing but keep it simple
 *      - Ensure you test with a clean environment, clean up your mess when done
 *
 *                                          - Thank you.
 *
 * @author Nuno Brito, 13th of June 2011 in Darmstadt, Germany.
 */
public class SnapshotTrackerTest {

    static String
            snapFolder = "testSnapshot";

    SnapshotTracker snap;


    public SnapshotTrackerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Testing the Snapshot Tracker");
        
        System.out.println("  Creating temporary folders");
             // create our temporary files and folders
             Boolean result =
                utils.testware.createTemporaryFoldersAndFiles(snapFolder);
             // verify that we completed this step ok
             assertTrue(result);
        System.out.println("  ..Done!");
 }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("Deleting our temporary folders");
        File root = new File(snapFolder);
        utils.files.deleteDir(root);
     System.out.println("..Done!");
    }

   
     @Test
     public void testTracking() {

         File fileA, fileB, fileC;

      System.out.println("  Test the tracking of our temporary folder");
      // create the file pointer
      File root = new File(snapFolder);
      
      System.out.println("    Initiate our tracker to follow target folder");
      snap = new SnapshotTracker(root);
      System.out.println("    ..Done!");

      System.out.println("    Scan files");
      snap.track();
      System.out.println("    ..Done!");
      // plant additional files
      System.out.println("    Seed two new files to the temporary folder");
      fileA = utils.testware.createFile(root, "HelloA.txt");
      fileB = utils.testware.createFile(root, "HelloB.txt");
      System.out.println("    ..Done!");
      // track new changes
      System.out.println("    Scan files");
        snap.track();
      System.out.println("  ..Done!");
      // grab these changes
      System.out.println("    Test if the two new files were detected or not");
        ArrayList<FileRecord> a = snap.getRecentlyAdded();
        // we placed two new files, we should have two files reported here
        assertEquals(a.size(), 2);
      System.out.println("  ..Done!");

      // Second round of tests
      System.out.println("    Create second round of files");
       fileC = utils.testware.createFile(root, "HelloC.txt");
       utils.testware.createFile(root, "HelloD.txt");
       utils.testware.createFile(root, "HelloF.txt");
      System.out.println("  ..Done!");

      System.out.println("    Scan files");
        snap.track();
      System.out.println("  ..Done!");

      System.out.println("    Test if the three new files were detected or not");
        a = snap.getRecentlyAdded();
        // we placed 3 new files, we should have 3 files reported here
        assertEquals(3, a.size());
      System.out.println("  ..Done!");
//////////////////////
       System.out.println("Test deleting files");
       // delete some files
       System.out.println("    Deleting fileA and fileB");
        fileA.delete();
        fileB.delete();
       System.out.println("  ..Done!");
       // track down these changes
       System.out.println("    Scan files");
        snap.track();
       System.out.println("  ..Done!");
       System.out.println("   Get the missing files (should be two):");
       a = snap.getRecentlyMissing();
       for(FileRecord record : a)
       System.out.println( record.getAbsolutePath());
        assertEquals(2, a.size());
        System.out.println("..Done!");

//////////////////////
       System.out.println("Test modifying a file");
       // write a short string inside a given file
       utils.files.SaveStringToFile(fileC, "ABC");
       System.out.println("..Done!");
       // scan for changes
       System.out.println("    Scan files");
        snap.track();
       System.out.println("  ..Done!");
       // output modifications
       System.out.println("   Get the modified file (should only be one):");
       a = snap.getRecentlyModified();
       for(FileRecord record : a)
       System.out.println( record.getAbsolutePath());
        assertEquals(1, a.size());
        System.out.println("  ..Done!");

     }


}