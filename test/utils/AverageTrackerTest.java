/*
 * The Average Tracker class provides performance metrics. We use this class
 * to get an estimation of how many files are being processed the scanner during
 * a given period of time.
 *
 * This class will test:
 *  - Setting up a limit to the data that is computed
 *   (to erase old entries when new data is arriving)
 *  - Add new data
 *  - Compute the average and verify that it is correct
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
public class AverageTrackerTest {

    AverageTracker averageTracker = new AverageTracker();

    long testLimit = 10;

    public AverageTrackerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Starting the AverageTracker Test");

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("..All done!");
    }

  
     @Test
     public void addData() {
        System.out.println("  Setting the limit to 10 entries");
        // set our limit
        averageTracker.setLimit(testLimit);

        System.out.println("  Verifying the limit of entries");
        // get our limit
        long result = averageTracker.getLimit();

        // verify the results
        assertEquals(result, testLimit);
        System.out.println("  ..Done");


        System.out.println("  Adding new data");
        averageTracker.add(3000);
        averageTracker.add(3000);
        averageTracker.add(3000);
        averageTracker.add(3000);
        averageTracker.add(3000);
        // first 5 were added
        averageTracker.add(3000);
        averageTracker.add(3000);
        averageTracker.add(3000);
        averageTracker.add(3000);
        averageTracker.add(3000);
        // the limit of 10 was reached
        averageTracker.add(1000);
        averageTracker.add(1000);
        averageTracker.add(1000);
        averageTracker.add(1000);
        averageTracker.add(1000);
        // these values should have replaced the older ones


        System.out.println(averageTracker.getValues());

        String[] output = averageTracker.getValues().split(";");

        System.out.println("  Verify that our array has the right dimension");
        assertEquals(testLimit, output.length);
        System.out.println("  Size of array is " + output.length);
        System.out.println("  ..Done");


        System.out.println("  Computing the average of our data");
        long out = averageTracker.average();
        System.out.println("  Average is " + out);
        assertEquals(out, 2000);
        System.out.println("  ..Done");



     }

}