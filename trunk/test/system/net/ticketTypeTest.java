/*
 * Test if the methods inside the ticketType match what we expect for them
 */

package system.net;

import system.net.Network;
import system.net.ticketType;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito
 */
public class ticketTypeTest {

    public ticketTypeTest() {
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
     * Test of prepareTicket method, of class ticketType.
     */
    @Test
    public void testPrepareTicket() {
        /**
         * On this test we will pick on a given ticket and output a
         * properties object that is ready for being dispatched as a message
         */
        System.out.println("prepareTicket");
        ticketType instance = new ticketType();

        instance.address = "somehwere";
        instance.from = "Nuno";
        instance.to = "Santa Claus";
        instance.ticket = "123456";

        Properties result = instance.prepareTicket();
        assertEquals("Nuno", result.getProperty(Network.FIELD_FROM));
    }

    /**
     * Test of isValid method, of class ticketType.
     */
    @Test
    public void testIsValid() {
        /**
         * Check if this ticket hasn't expired yet
         */
        System.out.println("isValid");
        ticketType instance = new ticketType();

        // add dummy values
        instance.address = "somehwere";
        instance.from = "Nuno";
        instance.to = "Santa Claus";
        instance.ticket = "123456";

        // force expiration
        instance.sendDate = System.currentTimeMillis()-(2000 * 1000);

        boolean expResult = false;
        boolean result = instance.isValid();
        assertEquals(expResult, result);

        // should output a valid result this time
        instance.sendDate = System.nanoTime()-(100 * 1000);

        result = instance.isValid();
        assertEquals(true, result);
    }

    /**
     * Test of canAsk method, of class ticketType.
     */
    @Test
    public void testCanAsk() {
        /**
         * At given time interval, a thread will ask each ticket if they can
         * ask for status changes about them on the other instances. Each ticket
         * can have its own interval and therefore we ask the ticket if it is
         * ok or not to ask again.
         */
        System.out.println("canAsk");
        ticketType instance = new ticketType();

        // add dummy values
        instance.address = "somehwere";
        instance.from = "Nuno";
        instance.to = "Santa Claus";
        instance.ticket = "123456";
        instance.interval = "2";
        instance.previousTry = System.currentTimeMillis();

        boolean expResult = false;
        boolean result = instance.canAsk();
        // this test should return false, the time distance is too short
        assertEquals(expResult, result);

        // force the previousTry to go back 10 seconds (10 * 1000)
        instance.previousTry = instance.previousTry - 10000;
        expResult = true;
        result = instance.canAsk();
        // this test should return true, it is ok to ask again
        assertEquals(expResult, result);

    }

}