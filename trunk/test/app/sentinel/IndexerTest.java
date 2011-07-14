/*
 * The Indexer is intended to receive update information from other applications
 and also to provide updates as necessary. For the purposes of this test
 we will proceed with the following verifications:
 *   - Order a scan at Sentinel
 *   - Verify that we receive the file data from Sentinel at the Indexer
 *   - Indexer should add the new data
 *   - When we request the new data, it will provide us a set with it.
 */

package app.sentinel;

import java.io.File;
import java.util.ArrayList;
import system.mq.msg;
import java.util.Properties;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nuno Brito, 20th of March 2011 at Germany
 */
public class IndexerTest implements msg {


    public IndexerTest() {
    }

   // objects
    static Remedium
            instance = new Remedium();

    private int
            time_to_wait = 2, // how many seconds should we wait?
            depth = 4;

    @BeforeClass
    public static void setUpClass() throws Exception {

       // instance = addFilters(instance);

                   // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        //parameters.setProperty(FIELD_ID, id);
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN

        // kickstart the instance
        instance.start(parameters);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    instance.stop();
    }



      private static Remedium addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
        instance.addLogFilter("apps");
        instance.addLogFilter("database");
        instance.addLogFilter("main");
        instance.addLogFilter(centrum_client);
        instance.addLogFilter(sentinel_hot_folders);
        instance.addLogFilter("network_server");
        instance.addLogFilter("triumvir/client");
        instance.addLogFilter("network");
        instance.addLogFilter("message_queue");
      // ignore messages of the following types:
        instance.addLogGenderFilter(ROUTINE);
        instance.addLogGenderFilter(INFO);
     return instance;
     }

      

     @Test
     public void doTests() {
         // get some files to play around with
             File folder = new File(".");
             ArrayList<File> results =
                     utils.files.findfiles(folder, depth);

                 System.out.println("Found "+results.size()+" files");

        Properties box = new Properties();
        box.setProperty(FIELD_FROM, sentinel_scanner);
        box.setProperty(FIELD_TO, sentinel_indexer);
        box.setProperty(FIELD_TASK, PROCESS);

        // this is fast but buggy



                    String data = results.toString();
                    data = data.substring(1, data.length()-1); //



                    box.setProperty(msg.FIELD_MESSAGE, data);

                    // add this value to ensure we don't miss a single file
                    box.setProperty(msg.FIELD_COUNT, ""+results.size());

                    // send this box to the other side
                    instance.getMQ().send(box);

                    // wait for the Indexer to finish
                    waitForIndexer();

          }

     /**
      * Keep things going on a loop until the Indexer has done its work
      */
     private void waitForIndexer(){
                    Boolean running = true;
                    // wait for a while
                    while(running) // we'll run until the Indexer is happy
                    {

                    // wait for a few secs
                    utils.time.wait(time_to_wait);

                    try{
                        running = instance.logContains(sentinel_indexer,
                        "Done!")==false;
                        }catch(Exception e){ 
                            running = true;
                        }
                    }
     }

}