/*
 * Test the time Tracker class. Ensure that we can read several times the
 * provided value without changes across a short amount of time.
 */

package system.core;

import utils.TimeTracker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito, 18th of May 2011 in Pittsburgh, USA
 */
public class TimeTrackerTest {

    public TimeTrackerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getTime method, of class TimeTracker.
     */
    @Test
    public void testGetTime() {
         System.out.println("getTime");
        TimeTracker instance = new TimeTracker();

        utils.time.wait(1); // we need to wait a few seconds

        // grab the first result
        long result1 = instance.getTime();
        utils.time.wait(1);
        // After waiting a second, it should still be the same
        long result2 = instance.getTime();

        System.out.println(result1);
        System.out.println(result2);

        long output = result2 - result1;

        if(output > 0){
           fail("Test failed, please repeat to verify results."); }
        else
            System.out.println("Test completed.");

    }

}