/*
 * Test if the Centrum component is capable of registering itself and also
 * manage new clients.
 *
 * - Verify if the centrum component is running on the system or not
 * - Launch three instances of remedium, one server and two clients
 * - Produce Hello messages from each client to the server
 * - Evaluate if Hello messages have arrived or not
 * 
 */

package app.centrum;

import system.mq.msg;
import java.util.Properties;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Nuno Brito, 17th of May 2011 in Pittsburgh, USA
 */
public class CentrumTest implements msg {

    public CentrumTest() {
    }

   // objects
    static Remedium
            instanceA = new Remedium(),
            instanceB = new Remedium(),
            instanceC = new Remedium()
            ;

    private int
            time_to_wait = 3; // how many seconds should we wait?


    @BeforeClass
    public static void setUpClass() throws Exception {

        addFilters(instanceA);
        addFilters(instanceB);
        addFilters(instanceC);


        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        parameters.setProperty(APPS, sentinel+";"+centrum+";"+triumvir);

        // kickstart the instance
        parameters.setProperty(FIELD_ID, "A");
        parameters.setProperty(FIELD_PORT, "10101");
        instanceA.start(parameters);

        parameters.setProperty(FIELD_ID, "B");
        parameters.setProperty(FIELD_PORT, PORT_B);
        instanceB.start(parameters);

        parameters.setProperty(FIELD_ID, "C");
        parameters.setProperty(FIELD_PORT, PORT_C);
        instanceC.start(parameters);

        // remove tray icons
        utils.tweaks.removeTrayIcon("localhost:10101");
        utils.tweaks.removeTrayIcon(addressB);
        utils.tweaks.removeTrayIcon(addressC);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    instanceA.stop();
    instanceB.stop();
    instanceC.stop();
    }


    

     @Test
     public void hello() {
        // instances B and C send Hello to instance A

        // wait a few secs
        utils.time.wait(time_to_wait);

        // we need to have two registered clients at this point
        assertEquals(true,
                  instanceA.logContains
                            (centrum, "2 clients are registered")
                            );
     }



      private static void addFilters(Remedium instance){
      // filter messages before an instance starts up
          instance.getLog().filterExcludeGender(ROUTINE);
          instance.getLog().filterExcludeGender(INFO);
          instance.getLog().filterIncludeComponent("centrum");
     }

    
}