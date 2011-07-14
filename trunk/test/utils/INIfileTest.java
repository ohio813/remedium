/*
 * This class will test the INI manipulation features from the INIfile class.
 * The following cases will be tested:
 *  - Create an INI file
 *  - Write some INI keys inside a section
 *  - Read INI keys from the section
 *  - Write INI lines to a given section
 *  - Read a line from a given section
 */

package utils;

import system.mq.msg;
import java.io.File;
import system.log.LogMessage;
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
 * @author Nuno Brito, 2nd of July 2011 in Darmstadt, Germany.
 */
public class INIfileTest {

    INIfile ini; // our INI class under test
    static LogMessage log; // where the logs will be kept
    static File
            folder; // the folder where we'll place the INI file
          File file; // the INI file that will be written

    static final String
            rootFolder = "testINI",
            inifile = "test.ini";


    public INIfileTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Testing the INI file class");
        // create our folder if it does not exist already
        folder = new File(rootFolder);
        if(folder.exists()==false)
            assertTrue(folder.mkdir());
        // assert that our folder really exists
        assertTrue(folder.exists());
        // start the log system
        log = new LogMessage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("All tests completed!");
    }

  
   @Test
     public void testINI() {
        System.out.println(" Test instantiating the INI file");
           file = new File(folder, inifile);
           // fire up the INI instance
           ini = new INIfile(file, log);
           // result must be "completed"
           assertEquals(msg.COMPLETED,log.getResult());
           System.out.println(" " + log.getRecent());
        System.out.println(" ..Done!");
        // test writing a key
        System.out.println(" Test writing a key/value");
           ini.write("test", "Hello", "World");
           ini.write("test", "Hello1", "World1");
           ini.write("test", "Hello2", "World2");
           ini.write("test", "Hello3", "World3");
           ini.write("test", "Hello4", "World4");
           // mars
           ini.write("mars", "Wake", "World");
           ini.write("mars", "Wake1", "12321");
           ini.write("mars", "Wake2", "weerd2");
           ini.write("mars", "Wake3", "3545d3");
           ini.write("mars", "Wake4", "klfds");

           if(log.getResult()==msg.ERROR){
               System.out.println(log.getRecent());
               return;
           }
           System.out.println(" " + log.getRecent());
        System.out.println(" ..Done!");

        // test reading a key that exists
        System.out.println(" Test reading a key/value that exists");
            String result = ini.read("mars", "wake3");
            System.out.println(" Result -->" + result + "<--");
            assertEquals("3545d3",result);
        System.out.println(" ..Done!");

        // test reading a key that does not exist
        System.out.println(" Test reading a key/value that does not exist");
            result = ini.read("marsExpress", "wake3");
            System.out.println(" Result -->" + result + "<--");
            assertEquals("",result);
        System.out.println(" ..Done!");

        // test reading the lines from a given section
        System.out.println(" Test reading the lines from a section");
            result = ini.readSectionLines("mars");
            System.out.println(" Result -->" + result + "<--");
        assertTrue(result.length() > 0);
        System.out.println(" ..Done!");

        // test writing lines to a given section
        System.out.println(" Test writing lines to a section");
                    assertTrue(ini.writeSectionLine("mars","Hello There!!"));
                    ini.writeSectionLine("nuno","Hello There!!");
        System.out.println(" ..Done!");

//        // test deleting a section
//        System.out.println(" Test deleting a section");
//                    assertTrue(ini.deleteSection("mars"));
//        System.out.println(" ..Done!");

    }
}