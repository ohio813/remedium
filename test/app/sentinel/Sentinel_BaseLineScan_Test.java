/*
 * Test the implementation of the scanner and Indexer inside the
 * Sentinel application.
 *
 * We will initiate a new instance and kick start the scanner, we will try
 * several operations that must be supported like:
 *  - Pause
 *  - Resume
 *  - Start
 *  - Stop
 *  - Change throttle of scanning
 */

package app.sentinel;

import java.util.Properties;
import system.msg;
import remedium.Remedium;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito, 16th of April 2011 in Darmstadt, Germany.
 */
public class Sentinel_BaseLineScan_Test implements msg{

    public Sentinel_BaseLineScan_Test() {
    }

    // definitions
    private int
            time_to_wait = 5, // how many seconds should we wait?
            folder_depth = 5; // how many subfolders should be crawled?

    private String
            where = "c:\\"; // where should we look?

     // objects
    static Remedium
            instance = new Remedium();

    
    @BeforeClass
    public static void setUpClass() throws Exception {

        instance = addFilters(instance);

                   // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, "scanner");
        parameters.setProperty(FIELD_PORT, PORT_A);
        //parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN

        // kickstart the instance
        instance.start(parameters);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    instance.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

 
     @Test
     public void hello() {

        Properties message = new Properties();

      // the fields that we need to place here
        message.setProperty(FIELD_FROM, sentinel_gui);
        message.setProperty(FIELD_TO, sentinel_scanner);
        message.setProperty(FIELD_TASK, "scan");
        message.setProperty(FIELD_DIR, where);
        message.setProperty(FIELD_DEPTH,  ""+folder_depth);

   // After waiting, we need to ensure that we have started the scanning
        System.out.println("Starting a base line scan");
        testScannerAction(message, START, "Scanning");
        message.remove(FIELD_DIR); // no need to repeat this one again

   // pause the scanning
        System.out.println("Pausing the file finder");
        testScannerAction(message, PAUSED, "paused");

   // resume the scanning
        System.out.println("Resuming the file finder");
        testScannerAction(message, RESUME, "resumed");

   // resume the scanning
        System.out.println("Stopping the file finder");
        testScannerAction(message, STOPPED, "completed");

   // now we will repeat the same steps for the analyzer
         utils.time.wait(time_to_wait+5);
         
//        System.out.println("Pausing the file Indexer");
//        testIndexerAction(PAUSED, "PAUSED");
//
//        System.out.println("Resuming the file Indexer");
//        testIndexerAction(RESUME, "RESUME");

        System.out.println("Stopping the file Indexer");
        testIndexerAction(STOPPED, "STOPPED");


      // wait for the analyzer to finish his stuff
        waitForIndexer();
     }


     /** test the operational actions of the Indexer to see if they work */
   private void testIndexerAction(int action,  String expectedResult){

       Properties message = new Properties();
            // the fields that we need to place here
            message.setProperty(FIELD_FROM, sentinel_gui);
            message.setProperty(FIELD_TO, sentinel_indexer);
            message.setProperty(FIELD_TASK, CHANGE_STATUS);
            message.setProperty(CHANGE_STATUS, ""+ action);

      // send it away to the MQ
        instance.getMQ().send(message);
      // wait for some time
        utils.time.wait(time_to_wait);

//         assertEquals(true,
//                instance.logContains(sentinel_indexer,
//                        expectedResult));
    }


    /**
     * Dispatch a msg and evaluate the result
     */
    private void testScannerAction(Properties msg, int Action, String expectedResult){

      // now it is time to close down the scanning
        msg.setProperty(SCAN, ""+Action);
      // send it away to the MQ
        instance.getMQ().send(msg);
      // wait for some time
        utils.time.wait(time_to_wait);

         assertEquals(true,
                instance.logContains(sentinel_scanner,
                        expectedResult));
    }


    private static Remedium addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
        instance.addLogFilter("apps");
        instance.addLogFilter("database");
        instance.addLogFilter("manager");
        instance.addLogFilter("main");
        instance.addLogFilter("network_server");
        instance.addLogFilter(triumvir_client);
        instance.addLogFilter(centrum_client);
        instance.addLogFilter("network");
        instance.addLogFilter("message_queue");
      // ignore messages of the following types:
        instance.addLogGenderFilter(ROUTINE);
     return instance;
     }

         /**
      * Keep things going on a loop until the Indexer has done its work
      */
     private void waitForIndexer(){
                    Boolean running = true;
                    // wait for a while
                    while(running) // we'll until until the Indexer is happy
                    {

                    // wait for a few secs
                    utils.time.wait(time_to_wait);

                    try{
                        running = instance.logContains(sentinel_indexer,
                        "Done!")==false;
                        }catch(Exception e){ running = true;}
                    }
     }

}