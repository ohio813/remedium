/*
 * Update Tracker is intended to authorize the execution of looped instructions
 * at specific intervals of time. This way we prevent the need to start several
 * threads, we use a single thread that checks these update trackers to see if
 * it is time for them to allow the execution of a given command to proceed.
 *
 * It works in a similar manner of Cron.
 *
 * On this test case we will:
 *      - Schedule the execution of a task for every 10 seconds
 *      - Ask to run the task and get a permission denied answer
 *      - Ask after 10 seconds and expect a positive answer
 */

package app.sentinel;

import system.UpdateTracker;
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
 * @author Nuno Brito, 5th  of June 2011 in Darmstadt, Germany.
 */
public class UpdateTrackerTest {

    static UpdateTracker myTask; // the test object

     @BeforeClass
    public static void setUpClass() throws Exception {

         // create the task
         myTask = new UpdateTracker(null);

     }

      @AfterClass
    public static void tearDownClass() throws Exception {
      }

       @Test
     public void testAuthorization() {

           System.out.println("Testing the authorization mechanism");

           int wait = 8; // number of seconds to wait

           System.out.println("Set the test time for 8 seconds");
           myTask.setSecondsBetweenAction(wait);

           System.out.println(" first authorization test - this should be allowed");
           Boolean result = myTask.isAllowed();
           assertEquals(result, true);

           System.out.println(" second authorization test - "
                   + "this shouldn't be allowed "
                   + "(8 seconds haven't passed)");
           result = myTask.isAllowed();
           assertEquals(result, false);

           System.out.println(" waiting the expected time, plus one second");
           utils.time.wait(wait + 1);

           System.out.println(" third authorization test - "
                   + "this should be allowed");
           result = myTask.isAllowed();
           assertEquals(result, true);

           System.out.println(" fourth authorization test - "
                   + "this shouldn't be allowed "
                   + "(8 seconds haven't passed)");
           result = myTask.isAllowed();
           assertEquals(result, false);

           System.out.println(" ..Done!");


       }


}
