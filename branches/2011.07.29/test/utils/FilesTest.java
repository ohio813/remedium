/*
 * Test the results from some of the methods inside the files class.
 */

package utils;

import java.io.File;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
 * @author Nuno Brito, 15th of June 2011 in Darmstadt, Darmstadt.
 */
public class FilesTest {

    public FilesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    

     @Test
     public void testFindFolders() {
         
         System.out.println("Testing the Find Folders result");
         // choose the target folder
         File folderRoot = new File(".");
        // get the available folders
         ArrayList<File> folders = utils.files.findFolders(folderRoot, 25);
        // test if we got a valid result
         assertTrue(folders.size() > 0);
        // iterate our list of folders
         for(File folder : folders){
            // output each folder
             System.out.println("  " + folder.getPath());
         }
         System.out.println("..Done!");
     }

}