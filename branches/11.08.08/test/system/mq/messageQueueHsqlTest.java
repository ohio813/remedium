/*
 * This test case will verify the expected functioning of the msg queue
 * based on HSQL.
 *
 * We will:
    - Start the msg queue
 *  - Dispatch a few messages
 *  - Read a specific msg
 *  - Delete another msg
 *  - Flush all messages on the queue
 *  - Close the server
 *
 * All these operations must use the functions specified on the Interface that
 * was written for the msg queue.
 *
 * The idea is to ensure that we can also use this test case to both follow the
 * standard definition and also to ensure that everyone follows the interface
 * rules.
 *
 */

package system.mq;

import system.mqueue.msg;
import remedium.Remedium;
import java.util.ArrayList;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author Nuno Brito
 */
public class messageQueueHsqlTest {

    static Remedium main = null;

    public messageQueueHsqlTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Properties parameters = new Properties();
        parameters.setProperty(msg.FIELD_ID, "MQ");
        parameters.setProperty(msg.APPS, "none"); // authorized apps to start
        // startup our instance
        main = new Remedium();
        // add filters to the produce less messages
        addFilters(main);
        // start our instance
        main.start(parameters);
        }

    @AfterClass
    public static void tearDownClass() throws Exception {
        /**
         * Ideally, if we are stopping the msg Queue service and no other
         * service is using the database system, then it would be nice if our
         * database system could be closed.
         *
         * In practice there is no problem in letting the database to keep
         * running. In theory, it would be more efficient to stop services that
         * are not in use.
         */
        Properties parameters = new Properties();
        parameters.setProperty("DELETE", "");

        assertEquals(true,
                main.stop(parameters)
                    );
    }

    
     @Test
     public void testSend() {
     // add dummy data
         /**
          * On this case we are creating a msg. We define four fields
          * as the bare minimum to ensure that it reaches the destination.
          * We don't define static fields to ensure that we can add more
          * features as needed on the future.
          */
            Properties dummy = new Properties();
            dummy.put(msg.FIELD_TO, "ToSomeone");
            dummy.put(msg.FIELD_FROM, "FromSomeone");
            dummy.put(msg.FIELD_CREATED, "CreatedAt");
            dummy.put(msg.FIELD_PARAMETERS, "TheParameters");

            // place this msg on our queue
            assertEquals(true,
                        main.getMQ().send(dummy)
                        );
            System.out.println(main.getMQ().getLog().getRecent());

            // send more messages, just change them slightly
            refresh("ToSomeone", "FromFred", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());

            //dummy.put(msg.FIELD_FROM, "FromFlintsone");
            refresh("ToSomeone", "FromFlintsone", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());

//            dummy.put(msg.FIELD_FROM, "FromRambo");
            refresh("ToSomeone", "FromRambo", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());

            // now change the destination as well
//            dummy.put(msg.FIELD_TO, "ToRex");
            refresh("ToRex", "FromRambo", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());

//            dummy.put(msg.FIELD_FROM, "FromTRex");
            refresh("ToRex", "FromTRex", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());

//            dummy.put(msg.FIELD_TO, "ToSantaClaus");
            refresh("ToSantaClaus", "FromTRex", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());

//            dummy.put(msg.FIELD_TO, "ToSharktocupus");
            refresh("ToSharktocupus", "FromTRex", dummy);
            main.getMQ().send(dummy);
            System.out.println(main.getMQ().getLog().getRecent());
            
     }

     @Test
     public void testRead() {
     /**
      * Now we'll test the reading
      *
      * If you note with attention on the previous code, we have sent two
      * msg targeted to "ToRex", one was FromRambo and the other is
      * FromTRex
      *
      * So, we need to ensure that they are read when someone queues for
      * messages belonging to ToRex
      *
      */
      // do a test query to grab messages destined to "ToRex"
        ArrayList<Properties> get = main.getMQ().get("ToRex");

      // now we have a variable called get that holds an array of messages.
      // the size of this array needs to be two as explained before.
      assertEquals(2, get.size());
     }

     @Test
     public void testDelete() {
     /**
      * Now we'll repeat the previous test and get all the messages destined
      * to a given person and delete them. We will do an iteration for this step
      */


     // do a test query to grab messages destined to "ToSomeone"
     ArrayList<Properties> get = main.getMQ().get("ToSomeone");

     int size_initial = get.size();
     System.out.println("\nDeleting " + size_initial + " messages:");

     for (Properties message : get){
        System.out.println("    - message from "
                + message.get(msg.FIELD_FROM)
                + " to "
                + message.get(msg.FIELD_TO)
                + " ("
                + message.getProperty(msg.FIELD_ID)
                + ")"
                );

     }
     
     for (Properties message : get){
        // do the deleting part, the result must be true
     assertEquals(true,  // end result needs to be true
         main.getMQ().delete(message.getProperty(msg.FIELD_ID))
                 );

        // if something went wrong, cause an exception
        System.out.println("    - message from "
                + message.get(msg.FIELD_FROM)
                + " deleted: "
                + main.getMQ().getLog().getRecent()
        );
     }
     // repeat the test query to grab messages destined to "ToSomeone"
     ArrayList<Properties> newGet = main.getMQ().get("ToSomeone");
     // display the number of found records
     System.out.println(" We have " + newGet.size() + " records (expecting "
             + "0 as result)");
     // we should have deleted all messages on the queue and result should be 0
     assertEquals(true, newGet.isEmpty());

     }

     private static void addFilters(Remedium instance){
      // start by filtering messages before any instance begins
          instance.getLog().filterIncludeGender(msg.DEBUG);
          instance.getLog().filterIncludeGender(msg.ERROR);

          instance.getLog().filterExcludeGender(msg.INFO);
          instance.getLog().filterExcludeGender(msg.ROUTINE);

          instance.getLog().filterExcludeComponent("network");
          instance.getLog().filterExcludeComponent("main");
     }


    void refresh(String To, String From, Properties dummy){
            dummy.put(msg.FIELD_TO, To);
            dummy.put(msg.FIELD_FROM, From);
            dummy.put(msg.FIELD_CREATED, "CreatedAt");
            dummy.put(msg.FIELD_PARAMETERS, "TheParameters");
    }

}