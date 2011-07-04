/*
 * This class tests the LogMessage class. The purpose of this class is to
 * provide an efficient logging service that we can adapt to our needs.
 *
 * On this test case we will:
 *  - Create log events
 *  - Test if these events are retrieved as intended
 *  - Test if the translation feature is working
 */

package utils;

import system.msg;
import system.log.LogMessage;
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
 * @author Nuno Brito, 1st of July 2011 in Darmstadt, Germany.
 */

public class LogMessageTest {

    static LogMessage log;

    static String who = "test";

    public LogMessageTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Initiating the LogMessage test case");
            log = new LogMessage();
        System.out.println("Done!");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void hello() {

        System.out.println(" Logging a few messages");
            log.add(who, msg.ACCEPTED, "Hello");
            log.add(who, msg.ACCEPTED, "World!");
            log.add(who, msg.ACCEPTED, "What's");
            log.add(who, msg.ACCEPTED, "up?");
        System.out.println("Done!");

        System.out.println(" Counting the messages, expecting a certain value");
            long count = log.getCount();
            assertEquals(count, 4);
        System.out.println("Done!");

        System.out.println(" Testing to add a new message with parameters %1");
            log.add(who, msg.MAINTENANCE, "Is %1 different from %2?", "A","B");
            String result = "Is A different from B?";
            assertEquals(log.getRecent(), result);
        System.out.println("Done!");
    }

}