/*
 * Verify the container portion of the Component class.
 *
 * Containers are used to store data used by a given component. On this test
 * case we will:
 *  - Create a container
 *  - Add data to container
 *  - Read data from container
 *  - Remove data from container
 *
 * Each container creates a log of the data that is being handled, we will
 * also test:
 *  - Export data from log
 *  - Import data onto a new container
 *  - Rollback changes from a given point in time
 *
 */

package system.container;

import java.util.ArrayList;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.mq.msg;
import java.util.Properties;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import system.core.Component;
import static org.junit.Assert.*;

/**
 *  Rules of the testing game:
 *
 *      - If you make changes, add another entry @author below the previous one
 *      - Describe the purpose of the test case and which tests will be done
 *      - Split each group of tests onto its own method and use intuitive names
 *      - Test if something works as intended and also how it reacts to errors
 *      - Add System.out.println comments when:
 *          - A specific test is starting
 *          - A test has finished
 *      - Add an empty line of text between each test to keep results readable
 *      - Be verbose, explain to people what you are doing but keep it simple
 *      - Ensure you test with a clean environment, clean up your mess when done
 *
 *                                          - Thank you.
 *
 * @author Nuno Brito, 25th of May 2011 in Darmstadt, Germany
 */
public class containerTest implements msg {

    public containerTest() {
    }

   // objects
    static Remedium
            instance = new Remedium();

    private static String
            id = "test";

    private static int
            time_to_wait = 2, // how many seconds should we wait?
            lock = 123456;

    static Component component;
           Container container;


    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println("Starting the container tests\n");

        System.out.println("Delete previous databases to keep our test clean ");
        utils.tweaks.deleteDBFolder(PORT_A);

        System.out.println("Filter messages that not relevant");
        instance = addFilters(instance);

        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        parameters.setProperty(FIELD_ID, id);
        parameters.setProperty(LOCK,""+lock);
        parameters.setProperty(FIELD_PORT, PORT_A);
        parameters.setProperty(DELETE, ""); // ask to delete DB when this finishes
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        parameters.setProperty(APPS, "none"); // allowed components

        // kickstart the instance
        instance.start(parameters);

        // create our component
        component = new Component(instance,lock, false){

            @Override
            public void onStart() { // check if the onStart is working
                return;
            }

            @Override
            public void onStop() {}

            @Override
            public void onRecover() {}

            @Override
            public void onLoop() {}

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
        instance.stop();
        utils.time.wait(time_to_wait * 2);
        System.out.println("Deleting our database to keep things clean");
        utils.tweaks.deleteDBFolder(PORT_A);
    }


     @Test
     public void testOperations() {

         // need to wait some time. This value might need to be configured
         // to match the speed of your machine
          utils.time.wait(time_to_wait * 1);

         container = component.createDB(id, new String[] {"key","value"});

         if(container == null)
             fail("Failed to create a new container");

   System.out.println("Test adding keys on the container");
         container.write(new String[] {"key1","1"});
         container.write(new String[] {"key2","2"});
         container.write(new String[] {"key3","3"});
         container.write(new String[] {"key4","4"});

         // We need to have 4 entries or otherwise fail here
         long result = container.count();
         if(result != 4)
            fail("Failed to write data on the container as intended");
   System.out.println("  ..Done");


   System.out.println("Test reading keys from the container");

   ArrayList<Properties> readResult = container.read("key", "key1");
   String testRead = readResult.get(0).getProperty("value", "");

   System.out.println(testRead);


   System.out.println("Test removing keys from the container");
         container.delete("key", "key2");
         container.delete("key", "key4");

         // We need to have 2 entries or otherwise fail here
         result = container.count();
         if(result != 2)
            fail("Failed to remove data from the container as intended");
   System.out.println("  ..Done");


   System.out.println("Add our container to the component box");
   Container output = component.box.add(container);
   assertNotNull(output);

   System.out.println("  ..Done\n");

//     }
//
//
//     @Test
//     public void testGetStoreFields() {
         System.out.println("Testing the getStoreFields() method");

         // get our container from the box
         Container out = component.box.get("test");

         // Calling the getStoreFields() method
         String[] fields = out.getFields();

         System.out.println("Found the following fields: ");
         for (String field : fields)
             System.out.println("   - '"+field+"' ");

         System.out.println("  ..Done\n");
     }


      private static Remedium addFilters(Remedium instance){
      // starting by filtering unwanted messages before any instance starts up
          instance.getLog().filterIncludeGender(DEBUG);
          instance.getLog().filterIncludeGender(ERROR);
          instance.getLog().filterIncludeComponent("test");

     return instance;
     }

}