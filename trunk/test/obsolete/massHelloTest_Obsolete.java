/*
 * This test case will simulate a significant number of remedium instance
 * running at the same time and saying Hello to the centrum server
 */

package obsolete;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import system.Message;
import remedium.Remedium;
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito
 */
public class massHelloTest_Obsolete implements Message{

    // definitions
    private final static int
            mass = 3, // number of simultaneous instances
            time_to_wait = 15; // how many seconds should we wait?
    private static
            ArrayList<Remedium> // the array of instances
            instances = new ArrayList<Remedium>();

    private static Remedium
            rem_centrum = new Remedium(); // where the centrum server will run

    private static final String
            addressServer = "localhost:"+PORT_A;
            

    public massHelloTest_Obsolete() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        // add our filters to the centrum server instance
        rem_centrum = addFilters(rem_centrum);
        rem_centrum.addLogFilter("centrum/client");


        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, "server");
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN

        parameters.setProperty(centrum_server, ""); // start as centrum server
        // kickstart the centrum server instance
        rem_centrum.start(parameters);

        // iterate all instances
        for(int i = 1; i - 1 < mass; ++i){ // create the specified number of instances
            Remedium instance = new Remedium();


        // add the filters to all of them
        instance = addFilters(instance);
        // customized filters for these mass instances
        instance.addLogGenderFilter(INFO);

        // generic parameters for them all
        parameters = new Properties();
        parameters.setProperty(centrum_server, addressServer); // start as client
        parameters.setProperty(FIELD_ID, "client_"+i);
        parameters.setProperty(FIELD_PORT, PORT_A+i); // increase the port by one
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(NO_STATS, ""); // ask to delete DB when this finishes

        // kickstart the instance
        instance.start(parameters);
        // add it to our list
        instances.add(instance);
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        rem_centrum.stop(); // stop the centrum instance

        for(Remedium instance : instances){
            instance.stop(); // stop all instances
        }
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
     public void sayHello() {
     /**
      * On this test we will check if each one of the instances got a reply
      * back saying Hello. This must be checked both on the Server and each
      * client at the same time.
      */

        // wait for some time
        try { Thread.sleep(time_to_wait * 1000); } catch (InterruptedException ex) {
            Logger.getLogger(role_server_Test_Obsolete.class.getName()).log(Level.SEVERE, null, ex); }

        // verify the results
        for(Remedium instance : instances){
              // verify that each instance got registered
                assertEquals(true,
                        rem_centrum.logContains
                            (centrum_server, "Hello "+instance.getIDname()+", welcome aboard")
                            );
              // verify that each instance was recognized when saying hello again
                assertEquals(true,
                        rem_centrum.logContains
                            (centrum_server, "Hello "+instance.getIDname()+", welcome back")
                            );
         }

     }

}