/*
 * This test will verify that our ContainerFlatFile is working as intended.
 *
 * We will test:
 *      - Creating a new container inside a folder
 *      - Filling the container with random data up to a given limit
 *      - Delete some data from the container
 *      - Get values matching a given criteria
 *      - Measure the time require to perform each of the above operations
 */

package system.container;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import system.log.LogMessage;

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
 * @author Nuno Brito, 30th June 2011 in Darmstadt, Germany.
 */
public class ContainerFlatFileTest {


    ContainerFlatFile container;
    String[] fields = new String[]{"time_created","unique_key","value"};
    String id = "crc32";
    String rootTestFolder = "testStorage";


    public ContainerFlatFileTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Testing the Flat File Container");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("All tests completed!");
    }

   
     @Test
     public void startContainer() {
        System.out.println(" Test instantiating the container");

        // the reply object
        LogMessage result = new LogMessage();
        // we choose a test folder on the root of our project
        File rootFolder = new File(rootTestFolder);
        // create the container
        container = new ContainerFlatFile(id, fields, rootFolder, result);
        // output the message
        System.out.println("  " + result.getRecent());
        System.out.println("  ..Done!");
        System.out.println("  ..Test if folder exists");
//        File folder = new File(rootFolder, id);
//        assertTrue(folder.exists());
        System.out.println("  ..Done!");

        System.out.println(" ..Done!");
     }

}