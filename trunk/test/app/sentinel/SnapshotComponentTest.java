/*
 * Test if the Snapshot component is working as intended.
 *
 * On this test we will:
 *      - create a set of temporary folders
 *      - populate these folders with a bunch of files at random
 *      - take a snapshot of all the files inside the folder
 *          - verify if all files were recorded (count)
 *      - add changes on files at the temporary folder
 *      - take second snapshot
 *          - verify that changes were detected and recorded
 *      - clean up the temporary folder
 */

package app.sentinel;

import app.sentinel.SnapshotComponent;
import java.io.File;
import system.mq.msg;
import java.util.Properties;
import remedium.Remedium;
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
public class SnapshotComponentTest implements msg {


    public SnapshotComponentTest() {
    }

   // objects
    static Remedium
            instance = new Remedium();

    static String
            snapFolder = "testSnapshot";

    static SnapshotComponent snap;


    @BeforeClass
    public static void setUpClass() throws Exception {
    System.out.println("Testing the Snapshot component");


    System.out.println("  Initializing our instance");
        addFilters(instance);

        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(DELETE, ""); // ask to delete DB when finished
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        parameters.setProperty(APPS, "none"); // start no application

        // kickstart the instance
        parameters.setProperty(FIELD_PORT, PORT_A);
        instance.start(parameters);

    System.out.println("  ..Done!");

    System.out.println("  Adding the Snapshot component");
        snap = new SnapshotComponent(instance, null);


    System.out.println("    Waiting for snapshot to be running..");

    utils.time.waitFor(instance, snap.getCanonicalName(),
            "Snapshot service is ready");

    System.out.println("  ..Done!");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    System.out.println("  Closing down our instance");
    instance.stop();
    System.out.println("  ..Done!");
    System.out.println("All Done!");
    }


     @Test
     public void createTemporaryFolders() {
     System.out.println("  Creating temporary folders");

     // create our temporary files and folders
     Boolean result =
        utils.testware.createTemporaryFoldersAndFiles(snapFolder);
     // verify that we completed this step ok
     assertTrue(result);

     System.out.println("  ..Done!");
     }


     @Test
     public void doSnapshot() {
      System.out.println("  Taking a snapshot of the current structure");



      System.out.println("  ..Done!");
     }


     @Test
     public void deleteTemporaryFolders() {
     System.out.println("  Deleting our temporary folders");
        File root = new File(snapFolder);
        utils.files.deleteDir(root);
     System.out.println("  ..Done!");
     }




      private static void addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
          instance.getLog().filterIncludeGender(DEBUG);
          instance.getLog().filterIncludeGender(ERROR);

          //instance.getLog().filterExcludeGender(INFO);
          instance.getLog().filterExcludeGender(ROUTINE);

          instance.getLog().filterExcludeComponent("network");
          instance.getLog().filterExcludeComponent("main");

          instance.getLog().filterIncludeComponent(centrum);
          instance.getLog().filterIncludeComponent(triumvir);
          instance.getLog().filterIncludeComponent(sentinel);
     }

    



 }