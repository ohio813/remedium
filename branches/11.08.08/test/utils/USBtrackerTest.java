/*
 * The USB Tracker class helps to react when a new USB drive is inserted.
 * Java 6 includes a nifty set of tools that can already do this action, but
 * we aim to support Java 1.5 and 1.4 so we'll make our own way of supporting
 * the USB detection.
 *
 * It is intended to work regardless of the underlying operative system.
 *
 * Prerequisites:
 *      - A USB pendisk to test this procedure
 *
 * This class will test:
 *      - Initializing the class
 *      - Waiting for a USB drive to be inserted
 *
 */

package utils;

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
 * @author Nuno Brito, 8th  of June 2011 in Darmstadt, Germany.
 */
public class USBtrackerTest {

    static USBtracker tracker;


    public USBtrackerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Starting the USB tracker Test");

        System.out.println("  Initializing our tracker");
        tracker = new USBtracker();
        System.out.println("  ..Done");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("..All done!");
    }

  
     @Test
     public void waitForUSB() {
        System.out.println("  Please insert a USB drive on the computer..");
    
        int newUSB = tracker.noChanges;

        while(newUSB == tracker.noChanges){
            utils.time.wait(1);
            newUSB = tracker.updateDriveStatus();
        }

        System.out.println( "  The new drive can be found at:");
        for(String drive : tracker.getRemovableDrives())
            System.out.println("  -> " + drive);

        assertEquals(tracker.getRemovableDrives().length, 1);

        System.out.println("  ..Done");



     }

}