/*
 * This test case will validate the functioning of the component template class.
 * It will create an extended version of the component and verify if the most
 * critical functions are working as intended.
 */

package system.core;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.mqueue.msg;
import java.util.Properties;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nuno Brito, 3rd of April 2011 at Germany
 */
public class component_Test implements msg {

    public component_Test() {
    }

   // objects
    static Remedium
            instance = new Remedium();

    private static String
            id = "example";

    private static int
            time_to_wait = 4, // how many seconds should we wait?
            lock = 123456;

    private static Component
            component;

    @BeforeClass
    public static void setUpClass() throws Exception {

        instance = addFilters(instance);

        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, id);
        parameters.setProperty(LOCK,""+lock);
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        
        // kickstart the instance
        instance.start(parameters);

        // create our component
        component = new Component(instance,lock, false){

            @Override
            public void onStart() { // check if the onStart is working
                log(DEBUG,"Hello world!");
                return;
            }

            @Override
            public void onStop() {
                log(DEBUG,"Goodbye world!");
            }

            @Override
            public void onLoop() {
                log(DEBUG,"Looping");
            }

            @Override
            public String getTitle() {
                return "test";
            }

            @Override
            public String doWebResponse(Request request, Response response) {
                        return getTitle();
            }
        };


    }

    @AfterClass
    public static void tearDownClass() throws Exception {
       // instance.stop();
    }


     @Test
     public void testHello() {
         
         utils.time.wait(time_to_wait);
         System.out.println("Closing things down..");
     }



      private static Remedium addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
        instance.addLogFilter("apps");
        instance.addLogFilter("database");
        instance.addLogFilter("main");
        instance.addLogFilter("network_server");
        instance.addLogFilter("triumvir/client");
        instance.addLogFilter("system");

        instance.addLogFilter(sentinel_scanner);
        instance.addLogFilter(sentinel_indexer);
        instance.addLogFilter(sentinel_hot_folders);

        instance.addLogFilter("centrum/client");

        instance.addLogFilter("network");
        instance.addLogFilter("message_queue");
      // ignore messages of the following types:
        instance.addLogGenderFilter(ROUTINE);
        instance.addLogGenderFilter(INFO);
     return instance;
     }

}