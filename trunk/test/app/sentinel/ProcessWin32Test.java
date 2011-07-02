/*
 * Testing the processing of Win32. We require some .exe files for this test
 * to be possible, so we'll first look for them and then fire up our class
 * to process them as intended.
 */

package app.sentinel;

import java.util.Properties;
import remedium.Remedium;
import system.msg;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nuno Brito, 26th of March 2011 in Germany
 */
public class ProcessWin32Test implements msg {

    public ProcessWin32Test() {
    }

   // objects
    static Remedium
            instance = new Remedium();

    private static String
            id = "processWin32";




    @BeforeClass
    public static void setUpClass() throws Exception {

        instance = addFilters(instance);

                   // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, id);
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


    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void hello() {}



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

}