/*
 * This test aims to set the record in the longest name assigned to a test case!
 *
 * But seriously, it is intended to test the batch synchronization of all the
 * containers from the Sentinel component onto another remedium instance.
 *
 * It will replicate the expected behavior from Triumvir-Sentinel and vice-versa.
 *
 * As pre-requisite, we do require that one of the container has data. If there
 * is no data, it will just generate data by invoking the scanner and running it
 * for a few seconds.
 *
 * Testing steps:
 *      - Setup two instances
 *      - One of the instances must have data previously generated
 *      - If there is no data available, generate required data automatically
 *      - Request a Box to synchronize its containers with another
 *      - Evaluate results
 */

package obsolete;

import java.util.Properties;
import system.Message;
import remedium.Remedium;
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
 * @author Nuno Brito, 5th of June 2011 in Darmstadt, Germany.
 */
public class ContainerBoxBatchSynchronizeTest  implements Message {

     // objects
    static Remedium
            instanceA = new Remedium(),
            instanceB = new Remedium();

    private int
            time_to_wait = 3; // how many seconds should we wait?

    public ContainerBoxBatchSynchronizeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println("Starting the Box Synchronization tests\n");

        System.out.println("Delete database of instance B to keep our "
                + "test folder clean ");
        // delete folder of instance B so that we get a fresh start
        utils.tweaks.deleteDBFolder(PORT_B);

        // add filters to hide undesirable messages out of the way
        addFilters(instanceA);
        addFilters(instanceB);

        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        parameters.setProperty(APPS, sentinel+";"+centrum+";"+triumvir);

        // kickstart the instance
        parameters.setProperty(FIELD_ID, "A");
        parameters.setProperty(FIELD_PORT, "10101");
        instanceA.start(parameters);

        parameters.setProperty(FIELD_ID, "B");
        parameters.setProperty(FIELD_PORT, PORT_B);
        instanceB.start(parameters);

        // remove tray icons
        utils.tweaks.removeTrayIcon("localhost:10101");
        utils.tweaks.removeTrayIcon(addressB);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        instanceA.stop();
        instanceB.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

     @Test
     public void hello() {}


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