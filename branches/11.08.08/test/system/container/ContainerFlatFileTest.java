/*
 * This test will verify that our ContainerFlatFile is working as intended.
 *
 * We will test:
 *      - Creating a new container inside a folder
 *      - Filling the container with random data up to a given limit
 *      - Delete some data from the container
 *      - Get values matching a given criteria
 *      - Measure the time require to perform each of the above operations
 */

package system.container;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import system.log.LogMessage;
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
 * @author Nuno Brito, 30th June 2011 in Darmstadt, Germany.
 */
public class ContainerFlatFileTest {


    String[] fields = new String[] // 5 fields
        {"uid","time_created", "unique_key", "update", "author"};
    String id = "test";
    static String rootTestFolder = "testStorage";

    // setup our container
    static ContainerFlatFile
            container;



    public ContainerFlatFileTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Testing the Flat File Container");
   // first step: remove any previous tests
        System.out.println(" Deleting the work folder");
        File file = new File(rootTestFolder);
        utils.files.deleteDir(file);
        if(file.exists())
            fail("Failed to delete the work folder");
        System.out.println(" ..Done");

        // wait at least one second before proceeding
        utils.time.wait(1);

   // second step: create a fresh new folder
        System.out.println(" Creating the work folder");
            utils.files.mkdirs(file);
        if(file.exists()==false)
            fail("Failed to create the work folder");
        System.out.println(" ..Done");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("All tests completed!");
    }

   
    @Test
    public void initializationTest() {
        System.out.println(" Test instantiating the container");

        // the reply object
        LogMessage result = new LogMessage();
        // we choose a test folder on the root of our project
        File rootFolder = new File(rootTestFolder);
        // create the container
        container = new ContainerFlatFile(id, fields, rootFolder, result);
        // output the message
        System.out.println("  " + result.getRecent());
        System.out.println("  ..Done!");
    }


     @Test
     public void initialWriteReadTest() {
        
//       first step: write a key onto our structure
         System.out.println(" Test writing");
            container.write(new String[]{"1","A","3","4","5"});
            container.write(new String[]{"2","B","3","4","5"});
            container.write(new String[]{"3","C","3","4","5"});
            container.write(new String[]{"4","D","3","4","5"});
            container.write(new String[]{"5","E","3","4","5"});
         System.out.println(" ..Done!");


//      second step: test the read of these values
         System.out.println(" Test counting");
            long count = container.count();
            if(count != 5)
                fail("We have " + count + " instead of the expected record "
                        + "count");
         System.out.println(" ..Done!");


//      second step: test the read of one specific value
         System.out.println("  Test reading after write");
            String[] record = container.read("4");
            String out = record[1];
            assertEquals(out, "D");
         System.out.println("  ..Done!");


//       third step: write a key onto our structure
         System.out.println(" Test overwriting");
            container.write(new String[]{"1","A","AA","4","5"});
            container.write(new String[]{"2","B","BB","4","5"});
            container.write(new String[]{"3","C","CC","4","5"});
            container.write(new String[]{"4","D","XX","4","5"});
            container.write(new String[]{"5","E","ZZ","4","5"});
         System.out.println(" ..Done!");

//      second step: test the read of one specific value
         System.out.println("  Test reading after overwrite");
            record = container.read("2");
            out = record[2];
            assertEquals(out, "BB");
         System.out.println("  ..Done!");
          }


    @Test
    public void writeToLimitTest() {
        // we're going to write as many keys as allowed to keep inside a file
        System.out.println(" Test the limits of storage for each file");
        // get the maximum of files allowed
        long max = container.getMaxRecordsAllowed() * 3;
        System.out.println("  Creating " + max + " records..");
        long count = 6;
        long timeBegin = System.currentTimeMillis();
        long timePrevious = timeBegin;
        for(int i = 6; i < max + 1; i++){
        // do a nice progress output to let us know how it is going
            if(count == container.getMaxRecordsAllowed()){
                long timeResult = System.currentTimeMillis() - timePrevious;
                timePrevious = System.currentTimeMillis();
                String timeCount = utils.time.timeNumberToHumanReadable(timeResult);
                System.out.println("   " + i 
                        + " records and " + timeCount
                        + " per file."
                        + " (" + (i * 100)/max+ "% processed in "
                        + utils.time.timeNumberToHumanReadable
                         (System.currentTimeMillis() - timeBegin)
                        + ")");
                // reset the counter
                count = 0;
            }
            count++;
            container.write(new String[]{"" + i,"A","AA","4","5"});
        }
        System.out.println("  ..Done!");

        // do the time calculation to know how long it took
        long timeEnd = System.currentTimeMillis();

        long timeResult = timeEnd - timeBegin;
        String timeCount = utils.time.timeNumberToHumanReadable(timeResult);

        System.out.println("  Write operation took " + timeCount
                + " to write " + max + " records");
        // 10 minutes -> 100 000 records
        // (10 * 60 * 1000)
        // 21 seconds -> 10 000 records
        long timeToGoal = (timeResult * 100000) / max;//(10 * 60 * 1000) ;

        System.out.println("  Comparing to objective: \n"
                + "    " + utils.time.timeNumberToHumanReadable(timeToGoal)
                + " to write 100 000 records");

        // verify if the number of records is correct
        System.out.println("  Testing number of created records");
        System.out.println("  Created " + container.count() + " records");
            assertEquals(container.count(), max);
        System.out.println("  ..Done!");
    }

    @Test
    public void deleteTest() {

        long initialCount = container.count();

        // test deleting some records and see the result
        System.out.println(" Test deleting some records to see the result");
            container.delete("uid", "1");
            container.delete("uid", "3");
            container.delete("uid", "5");
            container.delete("uid", "7");
            container.delete("uid", "9");
        System.out.println(" ..Done!");

        // verify if the number of records is correct
        System.out.println("  Testing number of deleted records");
            System.out.println("  Counting " + container.count() + " records");
            assertEquals(container.count(), initialCount - 5);
        System.out.println("  ..Done!");

        // test deleting some records and see the result
        System.out.println("  Test deleting all records and files");
            container.deleteKnowledgeFiles();
        System.out.println("  ..Done!");
    }
}