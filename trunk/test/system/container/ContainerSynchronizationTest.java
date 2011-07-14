/*
 * Test if the Container synchronization is working as intended.
 *
 *      Prerequisites:
 *          - Sentinel component running at localhost:10101 must have
 *          indexed data inside containers.
 *
 * Container synchronization consists in allowing two remote containers to
 * synchronize the records existing at one place to the other.
 *
 * Remedium should handle a database with tens of Gigabytes, we cannot store
 * this size of data in memory nor expect to move it quickly across machines.
 *
 * Therefore, we brake the synchronization task into very small pieces. Each
 * container will be responsible with the synchronization of data with some
 * other instance running remotely. This breaks down efficiently the scale of
 * complexity and even allows to add more containers dynamically without impact
 * on the current architecture: For example, if a container does not exist on a
 * client then it won't bother a server that will use whichever data available.
 *
 * This method also allows nonstop synchronization of data in both directions.
 * Since we pick a moment in time to proceed with updates, it is possible to
 * use parallel threads to either upload or download new information onto each
 * container (when allowed).
 *
 * Last but not least, it is efficient since each container will only report and
 * synchronize new data whenever it is available. This takes away the burdom
 * from triumvirs and Sentinel to constantly request and control this information.
 *
 *  Steps of this test:
 *      - Create two instances
 *      - One of them must have data on the Sentinel storage (prerequisite)
 *      - The other instance must have no previous records
 *      - Launch the synchronization procedure from A to B
 *      - Verify if data matches on both directions
 *      - Launch the batch update between container boxes
 *      - Check if each listed container was updated on the other side
 */

package system.container;

import system.mq.msg;
import java.util.Properties;
import remedium.Remedium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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
 * @author Nuno Brito, 02nd of June 2011 in Darmstadt, Germany.
 */
public class ContainerSynchronizationTest implements msg {

    public ContainerSynchronizationTest() {
    }

   // objects
    static Remedium
            instanceA = new Remedium(), // container A
            instanceB = new Remedium()  // container B
            ;
    
    static Properties parameters = new Properties();



    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println("Starting the Container Synchronization tests\n");

        System.out.println("Delete database of instance B to keep our "
                + "test clean ");
        // delete folder of instance B so that we get a fresh start
        utils.tweaks.deleteDBFolder(PORT_B);

        addFilters(instanceA);
        addFilters(instanceB);

        // set the specific parameters for the centrum server instance
        //parameters.setProperty(DELETE, ""); // ask to delete DB when finished
        parameters.setProperty(LISTEN, ""); // ask to LISTEN
        parameters.setProperty(APPS, sentinel); // authorized apps to start

        // kickstart the instances
        parameters.setProperty(FIELD_ID, "clientA");
        parameters.setProperty(FIELD_PORT, "10101");
        instanceA.start(parameters);

        
        parameters.setProperty(FIELD_ID, "clientB");
        parameters.setProperty(FIELD_PORT, PORT_B);
        instanceB.start(parameters);


        // remove tray icons
        utils.tweaks.removeTrayIcon("localhost:10101");
        utils.tweaks.removeTrayIcon(addressB);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    instanceA.stop();
    instanceB.stop();
    }


   

    @Test
     public void verifyPreRequisites() {
         /**
          * Container on instance A must have data previously stored.
          * If it doesn't fail this test.
          */

        // wait some seconds to allow a proper system initialization
        // unfortunately, this value might need adjustment on slower machines
        System.out.println("Waiting some time until instance A boots up");
//        utils.time.wait(time_to_wait * 2);

        // wait for the indexer to be started (heaviest component)
        while(instanceA.logContains(sentinel_indexer, "Ready to start")==false)
        utils.time.wait(1);

        System.out.println("Verifying pre-requisites to run this test");
        System.out.println("  Getting count of CRC32 checksums from instanceA");
        String result = utils.internet.getTextFile
             ("http://localhost:10101/"+sentinel_indexer+"?db=crc32&action=count");

        // remove annoying white spaces from result
        result = result.trim();

        // we can't receive an empty result here
        if(result.length() == 0)
            fail("No connection to our test container?");

        long count = 0;

        try{
            count = Long.parseLong(result);
        } catch (Exception e){
            fail("Container did not return a valid value to us: '"+result+"'");
        }


        // strike one, try to provide the pre-requisite (a DB with data)
        if(count < 1)
            count = this.attemptToFix();
        // strike two, you're out..
        if(count < 1)
            fail("The count value is not valid for our tests: '" + count + "'");


        System.out.println("  We have a container with " + count + " records"
                + " to synchronize");
        System.out.println("   ..Done\n");
     }

    /** Someone didn't read the pre-requisites, try to fix the mess.. */
    private long attemptToFix(){
   // someone might be trying to run this test with an empty database.
            // why do we bother writing pre-requisites?
            // let's try to solve this matter and provide ourselves the
            // pre requisite without needing to bother anyone..

            // this will start a new instance that will scan for "stuff"
            utils.tweaks.generateIndexedData(instanceA, 
                    utils.files.getRootFolder(), 25);


            // repeat the same test..
            String result = utils.internet.getTextFile
             ("http://localhost:10101/"+sentinel_indexer+"?db=crc32&action=count");
            // remove annoying white spaces from result
            result = result.trim();
            // we can't receive an empty result here
            if(result.length() == 0)
                fail("No connection to our test container?");

            long count = 0;
            try{
            count = Long.parseLong(result);
        } catch (Exception e){
            fail("Container did not return a valid value to us: '"+result+"'");
        }

        return count;
    }

//     @Test
//     public void synchronize() {
//         /**
//          * Launch the synchronization procedure for instance A to update B
//          */
//        System.out.println("Testing synchronization between instance A and "
//                + "instance B");
//
//         // we remotely ask instance A to synchronize with instance B
//        String request =
//             "http://" + instanceB.getNet().getAddress()
//             + "/"
//             + "+sentinel_analyzer+"
//             + "?db=crc32"
//             + "&action=remotesync" // call remote sync, sync is reserved
//             + "&who=" + instanceA.getNet().getAddress()
//             + "&since=0"
//             + "&until=" + instanceB.getTime()
//             ;
//
//        System.out.println("Requesting: " + request);
//
//        // get the record data
//        String result = utils.internet.getTextFile(request);
//
//        System.out.println("Confirm that we received a positive message");
//        // we need a success message
//        assertEquals(result.contains("Update ok!")
//                , true);
//        System.out.println("   ..Done");
//
//        // verify that both databases hold equal values
//        System.out.println("Both databases must hold equal number of records");
//
//
//        String countA = utils.internet.getTextFile
//             ("http://localhost:10101/"+sentinel_analyzer+"?db=crc32&action=count");
//        String countB = utils.internet.getTextFile
//             ("http://localhost:3001/"+sentinel_analyzer+"?db=crc32&action=count");
//
//        // the results must be equal now
//        assertEquals(countA, countB);
//
//
//        System.out.print("End result: " + result);
//        System.out.print("   ..Done\n");
// }


     @Test
     public void batchSynchronize() {
         /**
          * Update all the containers from one point to the other
          */
        System.out.println("Testing synchronization between instance A and "
                + "instance B of the Sentinel containers");


        String result = utils.tweaks.batchSynchronize(
                instanceA.getNet().getAddress(),    // from
                instanceB.getNet().getAddress(),    // to
                sentinel_indexer,                   // what
                0,                                  // since
                instanceB.getTime()                 // until
                );

        // print the result to the console
       System.out.println(result);


         // we remotely ask instance A to synchronize with instance B
//        String request =
//             "http://" + instanceB.getNet().getAddress()
//             + "/"
//             + sentinel_analyzer
//             + "?box=crc32"
//             + "&action=remotesync" // call remote sync, sync is reserved
//             + "&who=" + instanceA.getNet().getAddress()
//             + "&since=0"
//             + "&until=" + instanceB.getTime()
//             ;
//
//        System.out.println("Requesting: " + request);
//
////         get the record data
//       String result = utils.internet.getTextFile(request);
//
//        System.out.println("Confirm that we received a positive message");
//        // we need a success message
//        assertEquals(result.contains("Update ok!")
//                , true);
//        System.out.println("   ..Done");
//
//        // verify that both databases hold equal values
//        System.out.println("Both databases must hold equal number of records");
//
//
//        String countA = utils.internet.getTextFile
//             ("http://localhost:10101/"+sentinel_analyzer+"?db=crc32&action=count");
//        String countB = utils.internet.getTextFile
//             ("http://localhost:3001/"+sentinel_analyzer+"?db=crc32&action=count");
//
//        // the results must be equal now
//        assertEquals(countA, countB);
//
//
//        System.out.print("End result: " + result);
//        System.out.print("   ..Done\n");
     }


      private static void addFilters(Remedium instance){
      // start by filtering messages before any instance begins
          instance.getLog().filterIncludeGender(DEBUG);
          instance.getLog().filterIncludeGender(ERROR);

          instance.getLog().filterExcludeGender(INFO);
          instance.getLog().filterExcludeGender(ROUTINE);

          instance.getLog().filterExcludeComponent("network");
          instance.getLog().filterExcludeComponent("main");

          instance.getLog().filterIncludeComponent(centrum);
          instance.getLog().filterIncludeComponent(triumvir);
          instance.getLog().filterIncludeComponent(sentinel);
     }
      
 }