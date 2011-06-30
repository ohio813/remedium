/*
 * Test if the Graph class is producing graphs as intended.
 *
 * On this test we will:
 *  - Create a new graph
 *  - Add some data
 *  - Output an image with the result
 *
 * Please verify if the outputted image is well generated or not.
 */

package utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
 * @author Nuno Brito, 10th of June 2011 in Darmstadt, Germany.
 */
public class GraphTest {

    static Graph graph;

    public GraphTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Starting the Graph Test");
        // create our instance
        graph = new Graph("Test","X values","Y values");

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("..All done!");
    }

     @Test
     public void addData() {
         System.out.println("  Adding dummy data to our graph");
         graph.addValue(10, "series1", "A");
         graph.addValue(20, "series1", "B");
         graph.addValue(30, "series1", "C");
         System.out.println("  ..Done");

         System.out.println("  Outputting a graph at ./httpdocs/out.png");
         graph.output("httpdocs", "out.png", 200, 600);
         System.out.println("  ..Done");
     }

}