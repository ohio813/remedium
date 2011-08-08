/*
 * This test should verify if our user interface for the Sentinel is working as
 * intended
 */

package app.sentinel;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.mqueue.msg;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nuno Brito
 */
public class UI_test implements msg{

    public UI_test() {
    }

    // objects
    static Remedium
            instance = new Remedium();

    private int
            time_to_wait = 15; // how many seconds should we wait?


    @BeforeClass
    public static void setUpClass() throws Exception {

           instance = addFilters(instance);

                   // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, "SentinelGUI");
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN

        // kickstart the centrum server instance
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
        instance.addLogFilter("network_server");
        instance.addLogFilter("triumvir/client");
        instance.addLogFilter("network");
        instance.addLogFilter("message_queue");
        // ignore messages of the following types:
        instance.addLogGenderFilter(ROUTINE);

     return instance;
     }

     @Test
     public void hello() {
     /**
      * Request for the User Interface to become visible
      */
         Properties message = new Properties();

           // the fields that we need to place here
        message.setProperty(FIELD_FROM, "Sentinel/GUI");
        message.setProperty(FIELD_TO, "Sentinel/GUI");
        message.setProperty("GUI", "visible");
         // send it away to the MQ
        instance.getMQ().send(message);

        // wait for some time
        try { Thread.sleep(time_to_wait * 1000); } catch (InterruptedException ex) {
            Logger.getLogger(UI_test.class.getName()).log(Level.SEVERE, null, ex); }

     }


}