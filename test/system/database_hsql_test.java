package system;

/*
 * This test will verify if our implementation of the database is working
 * as intended. We will start the database using default parameters and then
 * try the same test using customized values.
 */

import remedium.Remedium;
import java.io.File;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito
 */
public class database_hsql_test {

    public database_hsql_test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        // clean up our mess
        System.out.println("Cleaning up our temporary directory");
        assertEquals(true,
                utils.files.deleteDir(new File("My_Test-01")));
    }

    @Test
    public void testStartDefault() {
        Remedium main= new Remedium();
        assertEquals(true,
                main.getDB().start() );
//            main.getDB().start()

           assertEquals(true,
                main.getDB().stop());
    }

    @Test
    public void testStartCustomized() {
        /**
         * On this test we will specify a different folder where the knowledge
         * should be kept. Under some implementations this parameter is irrelevant
         * if the no files or directories are used, but we keep it here as an
         * optional parameter
         */
        Remedium main = new Remedium();

        Properties parameters = new Properties();
        // we only accept letters, numbers and - or _
        parameters.setProperty("DIR", "c:/test(er/My_Test-01");

        assertEquals(true,
                main.getDB().start(parameters));
        assertEquals(true,
                main.getDB().stop());
    }
}
