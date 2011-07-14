/*
 * This test will ensure that our process manager based on HSQL is working
 * as we intend. We will:
 *
 *      - start the system
 *      - launch a few processes
 *      - read the status of a process
 *      - setStatus the status of a process
 *      - send a kill to an active role
 *      - close down our system
 *
 * What we need for this test:
 *      - Sentinel application must be enabled and launched, we use it as example
 *
 */
package system.core;

import system.mq.msg;
import system.process.Status;
import remedium.Remedium;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import system.mq.msg;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito, 31st of March 2011 in Germany.
 */
public class ProcessManagerTest implements msg {

    // objects
    static Remedium
            instance = new Remedium();

    static long
            unlock = 123456;



    @BeforeClass
    /** Start the system */
    public static void setUpClass() throws Exception {

        //instance = addFilters(instance);

         // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(LOCK, ""+unlock);
       // parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN

        // kickstart the instance
        instance.start(parameters);
    }

    @AfterClass
    /** close everything down */
    public static void tearDownClass() throws Exception {
    instance.stop();
    }

     @Test
     public void isSentinelRunning() {
     // verify that our Sentinel application is running
     assertEquals(true,
                instance.logContains(sentinel_scanner,
                        "Ready to start"));
     }


     @Test
     // verify that we can read the status of the sentinel/scanner
     public void readStatus() {

     // the first test is a false start
     assertNull(instance.getProcess().getStatus("dummy"));

     utils.time.wait(4);

     // on the second test we really want the status of the sentinel_scanner
     Status status = instance.getProcess().getStatus(sentinel_indexer);

      // we request a status reply about the scanner, let's check if this was ok
     assertEquals(sentinel_indexer,status.getName());
 
     System.out.println(status.getParameters().toString());

     }



     /** filter out the messages that we don't want to see */
      private static Remedium addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
        instance.addLogFilter("apps");
        instance.addLogFilter("database");
        instance.addLogFilter("main");
        instance.addLogFilter("network_server");
        instance.addLogFilter("triumvir/client");
        instance.addLogFilter("network");
        instance.addLogFilter("message_queue");
      // ignore messages of the following types:
      //  instance.addLogGenderFilter(ROUTINE);
     return instance;
     }

}