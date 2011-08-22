/*
 * Verify if the msg tracker test is working as intended. We will pick
 * on each method and double check the results when using both valid and invalid
 * results. We will also introduce some "impossible" inputs to check how it
 * reacts to unknown circumstances.
 */

package system.net;

import system.net.message_tracker;
import java.util.Hashtable;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import remedium.Remedium;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito
 */
public class message_trackerTest {

    // setup the instance
    static Remedium A = new Remedium();


    public message_trackerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        final String testID = "TEST";

        // set up the options
        Properties parameters = new Properties();
        parameters.setProperty(msg.FIELD_PORT, "101010");
        parameters.setProperty(msg.FIELD_ID, testID);
        // run it
        A.start(parameters);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
//        Properties parameters = new Properties();
//        parameters.setProperty("DELETE", null);
//                A.stop(parameters);
        A.stop();
    }


    /**
     * Test of getRemedium method, of class message_tracker.
     */
    @Test
    public void testGetRemedium() {
        /**
         * On this test, we verify if the GetRemedium returns an ID that we 
         * specify. This way we can prove that the message_tracker is using
         * the correct instance.
         */
        System.out.println("getRemedium");
        // when you create an instance, it should have a remedium assigned
        message_tracker instance = new message_tracker(A);
        
        String expResult = "rem-101010 TEST";
        Remedium result = instance.getRemedium();
        assertEquals(expResult, result.getIDname());
    }

    /**
     * Test of addEntry method, of class message_tracker.
     */
    @Test
    public void testAddEntry() {
        System.out.println("addEntry");
        Properties parameters = new Properties();
        message_tracker instance = new message_tracker(A);

        parameters.setProperty(msg.FIELD_FROM, "A");
        parameters.setProperty(msg.FIELD_TO, "B");

        // TICKET value is missing, the add entry should return false
        boolean expResult = false;
        boolean result = instance.addEntry(parameters);
        assertEquals(expResult, result);

        // We should now have a positive result
        parameters.setProperty(msg.FIELD_TICKET, "123456");
        expResult = true;
        result = instance.addEntry(parameters);
        assertEquals(expResult, result);

        // Add another entry with the same ticket and expect a fail
        expResult = false;
        result = instance.addEntry(parameters);
        assertEquals(expResult, result);

        // Set a new ticket to symbolize a different ticket and expect a positive result
        parameters.setProperty(msg.FIELD_TICKET, "ABCDEF");
        expResult = true;
        result = instance.addEntry(parameters);
        assertEquals(expResult, result);

    }

    /**
     * Test of getEntries method, of class message_tracker.
     */
    @Test
    public void testGetEntries() {
        System.out.println("getEntries");
        message_tracker instance = new message_tracker(A);

        // create a bogus entry
        Properties parameters = new Properties();
        parameters.setProperty(msg.FIELD_FROM, "A");
        parameters.setProperty(msg.FIELD_TO, "B");
        parameters.setProperty(msg.FIELD_TICKET, "123456");

        // add the first ticket
        instance.addEntry(parameters);
         // Set a new ticket to symbolize a different ticket
        parameters.setProperty(msg.FIELD_TICKET, "ABCDEF");
        instance.addEntry(parameters);

        // on our test, we must get an hastable with two entries
        Hashtable result = instance.getEntries();
        assertEquals(2, result.size());
    }

    /**
     * Test of setTicketStatus method, of class message_tracker.
     */
    @Test
    public void testSetTicketStatus() {
        System.out.println("setTicketStatus");
        message_tracker instance = new message_tracker(A);
        
        String ticket = "ABCDEF";
        int newStatus = msg.COMPLETED;

        // create a bogus entry
        Properties parameters = new Properties();
        parameters.setProperty(msg.FIELD_FROM, "A");
        parameters.setProperty(msg.FIELD_TO, "B");
        parameters.setProperty(msg.FIELD_TICKET, ticket);

        // add the first ticket
        instance.addEntry(parameters);
        // check the old status
  //      assertEquals(instance.getTicketStatus(ticket), msg.PENDING);
        // change the status from PENDINg to COMPLETED
        instance.setTicketStatus(ticket, newStatus);
        // check if the new status is implemented
  //      assertEquals(instance.getTicketStatus(ticket), msg.COMPLETED);

    }
//
//    /**
//     * Test of setStatus method, of class message_tracker.
//     */
//    @Test
//    public void testSetStatus() {
//        System.out.println("setStatus");
//        int newStatus = 0;
//        message_tracker instance = null;
//        instance.setStatus(newStatus);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStatus method, of class message_tracker.
//     */
//    @Test
//    public void testGetStatus() {
//        System.out.println("getStatus");
//        message_tracker instance = null;
//        int expResult = 0;
//        int result = instance.getStatus();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isRunning method, of class message_tracker.
//     */
//    @Test
//    public void testIsRunning() {
//        System.out.println("isRunning");
//        message_tracker instance = null;
//        boolean expResult = false;
//        boolean result = instance.isRunning();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}