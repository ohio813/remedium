package app.sentinel;

import java.io.File;
import java.util.ArrayList;
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
public class filefind_test {

    public filefind_test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

   
     @Test
     public void findfiles() {

         final String where = "d:\\";

         //File folder = new File("./");
         File folder = new File(where);

     ArrayList<File> results = utils.files.findFiles(folder, 4 );

         assertEquals(true,results.size()>0);
         
         for(File file :results)
            System.out.println(file.getAbsolutePath());

         System.out.println("Found "+results.size()+" files");

     }

}