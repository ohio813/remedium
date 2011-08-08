/*
 * A containerBox class is where we group and manage multiple containers.
 *
 * An array is created and individual containers are called by their name inside
 * this array. A containerBox allows adding more containers dynamically and also
 * to perform batch operations from single location.
 *
 * On this test case we will:
 *  - Add two containers to our box
 *  - Add non-existent container to see if it fails
 *  - Get one of them to ensure it works
 *  - Get a non-existent container to see if it fails
 *  - Get a list of all existent containers
 *  - Verify if a container exists (verify the exists() methods)
 *  - Verify that a container does not exist
 *  - Forbid containers from accepting synchronization requests (lock)
 *  - Attempt synchronization with these containers and see if it fails
 *  - Unlock synchronization requests
 *  - Repeat synchronization attempt and see if it works
 *
 */

package system.container;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.mqueue.msg;
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
 * @author Nuno Brito, 04th of June 2011 in Darmstadt, Germany
 */
public class containerBoxTest implements msg {

    public containerBoxTest() {
    }

   // objects
    static Remedium
            instance = new Remedium();

    private static String
            id1 = "data1",
            id2 = "data2"
            ;

    private static int
            time_to_wait = 2, // how many seconds should we wait?
            lock = 123456;

    static Component component;
           
    Container
           container1, // test container
           container2; // test container


    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println("Starting the containerBox tests\n");

        System.out.println("Delete previous databases to keep our test clean ");
        utils.tweaks.deleteDBFolder(PORT_A);

        System.out.println("Filter messages that are not relevant");
        instance = addFilters(instance);

        // set the specific parameters for the centrum server instance
        Properties parameters = new Properties();
        //parameters.setProperty(FIELD_ID, "");
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
        System.out.println("Delete our database");
        utils.tweaks.deleteDBFolder(PORT_A);
    }


     @Test
     public void testAddToBox() {

         // need to wait some time. This value might need to be configured
         // to match the speed of your machine
          utils.time.wait(time_to_wait * 1);
          
         container1 = component.createDB(id1, new String[] {"key","value"});
         container2 = component.createDB(id2, new String[] {"key1","value2"});

         if(container1 == null)
             fail("Failed to create a new container");

         if(container2 == null)
             fail("Failed to create a new container");


   System.out.println("Test adding keys on the container");

        container1.write(new String[] {"key1","1"});
        container1.write(new String[] {"key2","2"});
        container1.write(new String[] {"key3","3"});

        container2.write(new String[] {"key4","4"});
        container2.write(new String[] {"key5","5"});

//          We need to have 4 entries or otherwise fail here
         long result = container1.count() + container2.count();
         if(result != 5)
            fail("Failed to write data on the container as intended");
   System.out.println("  ..Done");

   // Add containers to our box
   System.out.println("Test adding our container to the box");

   Container output = component.box.add(container1);
   assertNotNull(output);

   output = component.box.add(container2);
   assertNotNull(output);
   
   // Add a null container and expect it to fail
   output = component.box.add(null);
   assertNull(output);

   System.out.println("  ..Done\n");
     }

    @Test
     public void testGetContainer() {

      System.out.println("Test the get container method");

      // we need to have 3 entries on the first container
      long count = component.box.get(id1).count();
        assertEquals(count, 3);
      
      System.out.println("Done. Container 1 returned " +count + " entries "
              + "(expecting 3 entries)");

      System.out.println("Repeating the get method for a container that does"
              + " not exist");

      Container output = component.box.get("100");
        assertNull(output);

      System.out.println("  ..Done\n");
    }

     @Test
     public void testContainerExists() {
         
         System.out.println("Test if our containers exist or not");
         
         System.out.println("  - test if container 1 exists");
            assertEquals(component.box.exists(id1), true);
         System.out.println("    ..Done");

         System.out.println("  - test if container 2 exists");
            assertEquals(component.box.exists(id2), true);
         System.out.println("    ..Done");

         // test the result for a container that does not exist   
         System.out.println("  - test a container that does not exist");
            assertEquals(component.box.exists("3"), false);
         System.out.println("    ..Done\n");
     }

     @Test
     public void testGetListOfContainers() {
         System.out.println("Test getting the list of current containers");

         String[] result = component.box.list();

         System.out.println("Found the following containers: ");
         for (String name : result)
             System.out.println("   '"+name+"' ");

         // our list must have returned two containers
         assertEquals(result.length, 2);

         System.out.println("  ..Done\n");
     }

      @Test
     public void testBatchSynchronization() {
     /**
      *  - Forbid containers from accepting synchronization requests (lock)
      *  - Attempt synchronization with these containers and see if it fails
      *  - Unlock synchronization requests
      */
      System.out.println("Test the batch synchronization");

          System.out.println("  Test if we can disallow update requests");
          component.box.batchSyncAuthorize(false);

          System.out.println("  Verify that all our requests are denied");

        
        // get a list of containers
        String[] containers = component.box.list();
            // iterate each one
            for(String container : containers){
                // request a fake synchronization to each container
        String request =
             "http://" + component.getInstance().getNet().getAddress()
             + "/"
             + component.getCanonicalName()
             + "?db=" + container // name of the database
             + "&action=sync" // call sync
             + "&who=" + component.getInstance().getNet().getAddress()
             + "&since=0"
             + "&until=" + component.getTime()
             ;

        // get the record data
        String result = utils.internet.getTextFile(request);

        // output the result:
        System.out.println("  Request to '"+container+"': " + result);
        // verify it says "not authorized" on the outputted message
        assertEquals(result.contains("not authorized"),true);
            }
         System.out.println("  ..Done\n");



         System.out.println("  Test if we can unlock the containers and sync");
         component.box.batchSyncAuthorize(true);
         System.out.println("  ..Done");


         System.out.println("  Verify that we can synchronize");

                // get a list of containers
                containers = component.box.list();
                    // iterate each one
                    for(String container : containers){
                        // request a real synchronization to each container
                String request =
                     "http://" + component.getInstance().getNet().getAddress()
                     + "/"
                     + component.getCanonicalName()
                     + "?db=" + container // name of the database
                     + "&action=sync" // call sync
                     + "&who=" + component.getInstance().getNet().getAddress()
                     + "&since=0"
                     + "&until=" + component.getTime()
                     ;

                // get the record data
                String result = utils.internet.getTextFile(request);

                // output the result:
                System.out.println("  Request to '"+container+"': " + result);
                // Since the container is empty, it should complain with
                // a message saying: "No records to provide"
                assertEquals(result.contains("No records to provide"),true);
                    }
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