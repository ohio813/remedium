/*
 * The container dump is used to dump all the records from a given container
 * onto a files inside a given directory.
 *
 * These files are later used by other instances to import data onto their
 * containers.
 *
 * On this class we provide the following features:
 *  - Dump all records from a given container
 *  - Import all records from a given set of files
 */

package system.core;

import java.io.File;
import system.Message;
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
 * @author Nuno Brito, 25th of July 2011 in Darmstadt, Germany.
 */
public class ContainerDumpTest implements Message {


   // objects
    static final String syncFolder = "testSync";

    static Remedium
            instanceA = new Remedium(), // container A
            instanceB = new Remedium()  // container B
            ;

    static Properties parameters = new Properties();



    @BeforeClass
    public static void setUpClass() throws Exception {

        System.out.println("Starting the Container Dump tests\n");

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

        System.out.println("Waiting some time until instance A boots up");

        // wait for the indexer to be started (heaviest component)
        while(instanceA.logContains(sentinel_indexer, "Ready to start")==false)
        utils.time.wait(1);

        System.out.println("Verifying pre-requisites to run this test");
        System.out.println("  Getting count of CRC32 checksums from instanceA");


       long count = utils.testware.dbCount(
                    "localhost:10101",
                    sentinel_indexer,
                    "crc32");

            // we can't receive an empty result here
            if(count == -1)
                fail("No connection to our test container?");
            if(count == -2)
                fail("Container did not return a valid value");


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

            long count = utils.testware.dbCount(
                    "localhost:10101",
                    sentinel_indexer,
                    "crc32");

            // we can't receive an empty result here
            if(count == -1)
                fail("No connection to our test container?");
            if(count == -2)
                fail("Container did not return a valid value");

        return count;
    }


    @Test
    public void doDump() {
    System.out.println("Dump all records from Instance A on test folder");
    System.out.println("    Create temp folder");

    File folder = new File(syncFolder);

    try{
    if(folder.exists()){
        utils.files.deleteDir(folder);
        // wait one second to allow this change to be recognized
        utils.time.wait(1);
    }

    // create our folder
    folder = utils.testware.createFolder(null, syncFolder);
        }catch (Exception e){
        fail("An exception occurred while creating a folder");
        }

    System.out.println("    ..Done");


    //   // wait for the dump operation to tbe finished
//    while(instanceA.logContains
//            ("system", "Ready for action!")==false)
//        utils.time.wait(1);

    System.out.println("    Ask the indexer to dump all his data on the test "
            + "sync folder");

    // ask the indexer to dump all his data on the test sync folder
    instanceA.getSys().send(sentinel_indexer, "dump",
            folder.getAbsolutePath());

   // wait for the dump operation to tbe finished
    while(instanceA.logContains
            (sentinel_indexer, "Dump operation successful")==false){
        utils.time.wait(1);
    }
        System.out.println("    ..Done");
}


     @Test
    public void doImport() {
    System.out.println("Import all records from Instance A on test folder");
    System.out.println("    Request import to instance B");

    // setup our test folder
    File folder = new File(syncFolder);

    // ask the indexer B to import data on the test sync folder
    instanceB.getSys().send(sentinel_indexer, "dumpImport",
            folder.getAbsolutePath());

   // wait for the dump operation to tbe finished
    while(instanceB.logContains
            (sentinel_indexer, "Dump import successful")==false){
        utils.time.wait(1);
    }

    System.out.println("    ..Done");
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


//     @Test
//     public void batchSynchronize() {
//         /**
//          * Update all the containers from one point to the other
//          */
//        System.out.println("Testing synchronization between instance A and "
//                + "instance B of the Sentinel containers");
//
//
//        String result = utils.tweaks.batchSynchronize(
//                instanceA.getNet().getAddress(),    // from
//                instanceB.getNet().getAddress(),    // to
//                sentinel_indexer,                   // what
//                0,                                  // since
//                instanceB.getTime()                 // until
//                );
//
//        // print the result to the console
//       System.out.println(result);


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
