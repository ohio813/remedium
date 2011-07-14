/*
 * This test case will verify if message_tracker is working
 * correctly or not.
 *
 * The purpose of the msg tracker is to manage the incoming
 * and outgoing messages from the network component.
 *
 * It will track messages that were sent to another instance
 * and request a status reply to each msg when it is due.
 *
 * It will also clean any old messages that expire or output
 * an error status. If a msg returns the status of Finished,
 * it will then put the contents of the messsage in the Queue.
 *
 * What we will test?
 *
 * - Add several entries on the ticket list
 * - Get a list of these entries
 * - Dispatch a msg from A to B, simulate reply and test result
 */
package system.net;

import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import org.junit.Test;
import system.mq.msg;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito, 18th of May 2011 in Pittsburgh, USA.
 */
public class network_test implements msg {

    // the two instances that will communicate with each other
    Remedium A = new Remedium(),
             B = new Remedium();
   

    public network_test() {
    }


    @Test
    public void simulateTickets() throws InterruptedException {
        /**
         * Now we will simulate two separate instances exchanging
         * tickets back and forth
         */
        Properties parameters = new Properties();

        System.out.println("Starting communication simulation between "
                + "two instances");

        // defining the custom port/ID for this instance
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(FIELD_ID, "A");
        //parameters.setProperty(LISTEN, "");

        A = addFilters(A);
        B = addFilters(B);


        // start the instance
        A.start(parameters);

        // defining the custom port/ID for this instance
        parameters.setProperty(FIELD_PORT, PORT_B);
        parameters.setProperty(FIELD_ID, "B");
        // set it as listener to outside requests
        parameters.setProperty(LISTEN, "");
        // start the instance
        B.start(parameters);


        Properties message = new Properties();

        // the fields that we need to place here
        message.setProperty(FIELD_FROM, "Nuno");
        message.setProperty(FIELD_TO, "CentrumServer");
        message.setProperty(FIELD_ADDRESS, addressB);
        message.setProperty(FIELD_MESSAGE, "Hi there Centrum server!");

        // send it away to the MQ
        System.out.println("Sending message to instance B at " + addressB);
        A.getMQ().send(message);


        // do the waiting part and check if the msg is picked by network
        // while waiting, you should see the log output of the msg being sent
        System.out.println("Waiting some seconds..");
        utils.time.wait(5);
        System.out.println("Enough waiting");


        // B replies as completed to the request from A
        ArrayList<Properties> AppB = B.getMQ().get("CentrumServer");

        if(AppB.isEmpty())
            fail("There exists no message on the queue of instance B");

        Properties Hello = AppB.get(0); // we have only sent one msg
        // first we delete the msg on the queue
        B.getMQ().deleteTicket(Hello.getProperty(FIELD_TICKET));
        // change the status of the request to COMPLETED
        Hello.setProperty(FIELD_STATUS, Integer.toString(COMPLETED));
        Hello.setProperty(FIELD_MESSAGE, "Hi there guy, how are things?");
        // place back the msg on the queue and wait
        B.getMQ().send(Hello);

        // System.out.println(Hello.toString());

        // do the waiting part and check if the msg is picked by network
        // while waiting, you should see the log output of the msg being sent
        utils.time.wait(8);

        // delete all traces of each instance when shutting down
        parameters.clear();
        parameters.setProperty("DELETE", "");

        // close our running instances
        A.stop(parameters);
        B.stop(parameters);
    }

         private static Remedium addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
        instance.addLogFilter("apps");
        instance.addLogFilter("log");
        instance.addLogFilter("database");
        instance.addLogFilter("main");
        instance.addLogFilter(manager);
        instance.addLogFilter(sentinel);
        instance.addLogFilter(centrum);
        instance.addLogFilter(sentinel + "/indexer");
        instance.addLogFilter(sentinel + "/scanner");
        instance.addLogFilter("network_server");
        instance.addLogFilter("triumvir/client");
        //instance.addLogFilter("network");
        //instance.addLogFilter("message_queue");
      // ignore messages of the following types:
        //instance.addLogGenderFilter(ROUTINE);
        //instance.addLogGenderFilter(INFO);
     return instance;
     }
}
