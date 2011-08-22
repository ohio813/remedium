/*
 * Test if Sentinel is working as intended.
 *
 *      - Triumvir asks Centrum for a list of Sentinel assigned to him
 *      - After Centrum provides the list, the triumvir will contact each of
 *      the Sentinel for an update
 *          - provides timestampt of last contact, uses zero if no previous
 *          contact exists
 *      - Sentinel provides an array with containers and records that hold new data
 *          - limits to 10000 records per message
 *      - Triumvir acknowledges that data was received
 *      - Repeat procedure at each hour or prefered interval
 *
 *   This is the big test. If everything works as intended then we have
 *   remedium sharing information as intended.
 *
 *   Would only be missing:
 *      - implement triumvirate interactions
 *      - implement quaestor to provide metrics
 *      - implement (...)
 *
 */

package app.sentinel;

import system.mqueue.msg;
import java.util.Properties;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Nuno Brito, 29th of May 2011 in Darmstadt, Germany.
 */
public class SentinelTest implements msg {

    public SentinelTest() {
    }

   // objects
    static Remedium
            instanceCentrum = new Remedium(), // centrum
            instanceB = new Remedium(),  // triumvir
            instanceC = new Remedium()   // client
            ;


    private int
            time_to_wait = 9; // how many seconds should we wait?


    @BeforeClass
    public static void setUpClass() throws Exception {

        addFilters(instanceCentrum);
        addFilters(instanceB);
        addFilters(instanceC);

        // remove specific logs
        //instanceA.addLogFilter("network");

        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(DELETE, ""); // ask to delete DB when finished
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        parameters.setProperty(APPS, centrum + ";"+sentinel+";"+triumvir);

        // kickstart the instance
        parameters.setProperty(FIELD_ID, "control");
        parameters.setProperty(FIELD_PORT, "10101");
        instanceCentrum.start(parameters);

        
        parameters.setProperty(FIELD_ID, "clientB");
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
    instanceCentrum.stop();
    instanceB.stop();
    instanceC.stop();
    }


     @Test
     public void verifyCentrumAndTriumvir() {
         /**
          * Verify the logs to ensure that both Centrum and Triumvir were
          * launched and are running.
          */

     System.out.println("Check if the centrum has been included or "
             + "not inside remedium");
          assertEquals(true, instanceCentrum.logContains
                            (manager, "Added 'centrum' to the list")
                            );
     System.out.println("Done.");

     System.out.println("Check if the triumvir has been included or "
            + "not inside remedium");
          assertEquals(true, instanceCentrum.logContains
                            (sentinel, "Added 'triumvir' to the list")
                            );
     System.out.println("Done.");

          // wait a few seconds to allow next operations to be completed
          utils.time.wait(time_to_wait);
          utils.time.wait(time_to_wait);

     }


     @Test
     public void triumvirActivate() {
              /** Verify the logs to ensure that Triumvir was activated
               *  by Centrum since there is not better Triumvir at startup
               */

         System.out.println("Check if Centrum is activating our Triumvir");
          assertEquals(true, instanceCentrum.logContains
                            (centrum, "Activating our triumvir")
                            );
         System.out.println("Done.");

         System.out.println("Check if Triumvir is activated");
          assertEquals(true, instanceCentrum.logContains
                            (triumvir, "Setting server state as 'true'")
                            );
         System.out.println("Done.");
     }




     @Test
     public void clientHello() {
         /**
          * Having both the Centrum and Triumvir running, we will check if the
          * other remedium instances that are running as clients have proceeded
          * with the "Hello world" procedure.
          */

      System.out.println("Check if client has sent an Hello World message");
          assertEquals(true, instanceB.logContains
                            (centrum, "Sending hello to centrum")
                            );
      System.out.println("Done.");
     }



      private static void addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
          instance.getLog().filterIncludeGender(DEBUG);
          instance.getLog().filterIncludeGender(ERROR);

          //instance.getLog().filterExcludeGender(INFO);
          instance.getLog().filterExcludeGender(ROUTINE);

          instance.getLog().filterExcludeComponent("network");
          instance.getLog().filterExcludeComponent("main");

          instance.getLog().filterIncludeComponent(centrum);
          instance.getLog().filterIncludeComponent(triumvir);
          instance.getLog().filterIncludeComponent(sentinel);
     }
      
 }