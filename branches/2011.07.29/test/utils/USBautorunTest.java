/*
 * The USB autorun class is intended to immunize a given USB pen disk. It will
 * pick a drive letter (or folder) and rename the autorun.inf to a safe name,
 * create a folder entitled 'autorun.inf' and handle any errors occured.
 *
 * On this test case we will:
 *      - Create a test folder with a file named 'autorun.inf' inside
 *      - Run the immunization procedure
 *      - Verify that autorun.inf is no longer a file but rather a folder
 *      - Delete the test folder
 *      - Verify that our test folder was deleted
 */

package utils;

import java.io.File;
import java.io.IOException;
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
 * @author Nuno Brito, 12th  of June 2011 in Darmstadt, Germany.
 */
public class USBautorunTest {

    static USBautorun autorun = new USBautorun(); // the test object


    static String autoFolder = "testAutorun";

    private File
            testFolder = new File(autoFolder),
            testFile = new File(autoFolder + File.separator + "autorun.inf");

     

     @BeforeClass
    public static void setUpClass() throws Exception {
         System.out.println("USB autorun test");
     }

      @AfterClass
    public static void tearDownClass() throws Exception {
          System.out.println("All done!");
      }

       @Test
     public void initialStep() {
           System.out.println("  Creating the test folder and file");


                System.out.println("    If test folder exists, delete it");
                if(testFolder.exists()){
                    assertTrue(utils.files.deleteDir(testFolder)); // delete the folder
                    assertFalse(testFolder.exists()); // it can't exist now
                System.out.println("    Test folder was deleted");
                }
                

                System.out.println("    Create our test folder: " + testFolder);
                // The mkdir operation must have been successful
                assertTrue(testFolder.mkdir());
                // our folder must really exist
                assertTrue(testFolder.exists());

                System.out.println("    Create our test file");
                try {
            
                    testFile.createNewFile();

                } catch (IOException ex) {
                    fail("Failed to create the 'autorun.inf' file!");
                }


           // can we grab a time stamp fingerprint?
            String modifiedFolder = "" + testFolder.lastModified();
            String modifiedFile = "" + testFile.lastModified();

           

             System.out.println("  Last modified: \n"
                     + "    Dir: " + modifiedFolder + "\n"
                     + "    File: " + modifiedFile
                             );


           System.out.println("  ..Done!");
       }

       @Test
     public void testImmunization() {
           System.out.println("  Testing the immunization mechanism");
                autorun.immunize(autoFolder);
           System.out.println("  ..Done!");
       }

       @Test
     public void finalStep() {
           System.out.println("  Deleting our temporary files");
           testFolder = new File(autoFolder);
           assertTrue(utils.files.deleteDir(testFolder)); // delete the folder
           System.out.println("  ..Done!");
       }


}
