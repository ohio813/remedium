/*
 * On this test we will verify some of the centrum functionality, in more
 * detail we will focus on the centrum server role.
 *
 * Steps for this test:
 *  - start two instances of remedium, one of them as centrum server
 *  - simulate the shipping of a msg targeted to the server
 */

package obsolete;

import java.util.logging.Logger;
import java.util.logging.Level;
import system.msg;
import remedium.Remedium;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito
 */
public class role_server_Test_Obsolete implements msg{

    // objects
    static Remedium
            instanceA = new Remedium(),
            instanceB = new Remedium();

    private int
            time_to_wait = 5; // how many seconds should we wait?


    public role_server_Test_Obsolete() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {


         // starting by filtering unwanted messages before any instance starts up
        instanceA.addLogFilter("apps");
        instanceB.addLogFilter("apps");

        instanceA.addLogFilter("database");
        instanceB.addLogFilter("database");

        instanceA.addLogFilter("main");
        instanceB.addLogFilter("main");

        instanceA.addLogFilter("network_server");
        instanceB.addLogFilter("network_server");

        instanceA.addLogFilter("network");
        instanceB.addLogFilter("network");

        instanceA.addLogGenderFilter(ROUTINE);
        instanceB.addLogGenderFilter(ROUTINE);


        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, "A");
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN

        parameters.setProperty(centrum_server, ""); // start as centrum server

        // start our instance and kickstart all components within
        instanceA.start(parameters);

        // start instance B without listening (but we still need to define a port)
        parameters = new Properties();
        parameters.setProperty(FIELD_ID, "B");
        parameters.setProperty(FIELD_PORT, PORT_B);
        parameters.setProperty(centrum_address, ""); // start as centrum server


        // where can one find the centrum?
        parameters.setProperty(centrum_address, addressA);

        // start the B instance with our customized parameters
        instanceB.start(parameters);

        // At this point, A has the Centrum Server while B is Centrum Client
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // stop our instances
        instanceA.stop();
        instanceB.stop();
    }

   

    /**
     * A centrum client says Hello to the Centrum Server
     */
    @Test
    public void testHello() {

        // let's send a fake msg from B to A
        Properties message = new Properties();

        // the fields that we need to place here
        message.setProperty(FIELD_FROM, centrum_client);
        message.setProperty(FIELD_TO, centrum_server);
        message.setProperty(FIELD_ADDRESS, addressA); // where is my centrum server?
        
        // add the client identification
        message.setProperty(FIELD_NAME, "B guy"); // my name
        message.setProperty(FIELD_MESSAGE, "Hi there Centrum server, I'm a client!");
        message.setProperty(FIELD_URL, addressB); // my address that will be used by others

        // add the score details
        message.setProperty(FIELD_CPU,"none");
        message.setProperty(FIELD_RAM,"none");
        message.setProperty(FIELD_DISK,"none");
        message.setProperty(FIELD_BANDWIDTH,"none");
        message.setProperty(FIELD_UPTIME,"none");

        // send it away to the MQ
        instanceB.getMQ().send(message);


        // wait for some time
        try { Thread.sleep(3 * 1000); } catch (InterruptedException ex) {
            Logger.getLogger(role_server_Test_Obsolete.class.getName()).log(Level.SEVERE, null, ex); }
        // repeat the hello! (should appear welcome back)
        instanceB.getMQ().send(message);


        // wait for some time
        try { Thread.sleep(time_to_wait * 1000); } catch (InterruptedException ex) {
            Logger.getLogger(role_server_Test_Obsolete.class.getName()).log(Level.SEVERE, null, ex); }

        // our msg must appear in the log of instanceA
        assertEquals(true,
                instanceA.logContains(centrum_server, "Hello B guy, welcome aboard")
                );
        // our msg must appear in the log of instanceA when returning back
        assertEquals(true,
                instanceA.logContains(centrum_server, "Hello B guy, welcome back")
                );


    }

   

}