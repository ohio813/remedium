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

package system;

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
public class message_queue_hsql_test {

    static Remedium main = null;

    public message_queue_hsql_test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Properties parameters = new Properties();
        parameters.setProperty(msg.FIELD_ID, "MQ");
        main = new Remedium();
        main.start(parameters);
     

        /**
         * The msg queue must be able of starting on its own. This means
         * that when we call "start", it must also be able of starting the
         * database system if it isn't already running.
         */
     assertEquals(false,
        main.getMQ().start()
                  );
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
            dummy.put(message_queue.FIELD_TO, "ToSomeone");
            dummy.put(message_queue.FIELD_FROM, "FromSomeone");
            dummy.put(message_queue.FIELD_CREATED, "CreatedAt");
            dummy.put(message_queue.FIELD_PARAMETERS, "TheParameters");

            // place this msg on our queue
            assertEquals(true,
                        main.getMQ().send(dummy)
                        );

            // send more messages, just change them slightly
            dummy.put(message_queue.FIELD_FROM, "FromFred");
            main.getMQ().send(dummy);
            dummy.put(message_queue.FIELD_FROM, "FromFlintsone");
            main.getMQ().send(dummy);
            dummy.put(message_queue.FIELD_FROM, "FromRambo");
            main.getMQ().send(dummy);
            // now change the destination as well
            dummy.put(message_queue.FIELD_TO, "ToRex");
            main.getMQ().send(dummy);
            dummy.put(message_queue.FIELD_FROM, "FromTRex");
            main.getMQ().send(dummy);
            dummy.put(message_queue.FIELD_TO, "ToSantaClaus");
            main.getMQ().send(dummy);
            dummy.put(message_queue.FIELD_TO, "ToSharktocupus");
            main.getMQ().send(dummy);
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
     System.out.println("Deleting " + size_initial + " messages:");

    // remedium.main.getMQ().delete(0);
     
     for (Properties message : get){

        // do the deleting part, the result must be true
     assertEquals(true,  // end result needs to be true
         main.getMQ().delete
                    (message.getProperty
                       (message_queue.FIELD_ID)
                    )
                 );

        // if something went wrong, cause an exception
        System.out.println("    - message from "
                + message.get(message_queue.FIELD_FROM)
                + " deleted.");
     }

     // repeat the test query to grab messages destined to "ToSomeone"
     get = main.getMQ().get("ToSomeone");
     // we should have deleted all messages on the queue and result should be 0
     assertEquals(true, get.isEmpty());

     }

}