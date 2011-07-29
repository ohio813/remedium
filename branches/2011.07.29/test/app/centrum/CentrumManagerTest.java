/*
 * Verify if the Centrum Manager is capable of receiving new clients and
 * managing them as intended.
 */

package app.centrum;

import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito, 17th of May 2011 in Pittsburgh, USA
 */
public class CentrumManagerTest {

    public CentrumManagerTest() {
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

    /** Provide a dummy client of this centrum client */
    private CentrumClient createDummy(String title){
        CentrumClient client = new CentrumClient();
        // set up the default values
        client.setBandwidth(10);
        client.setCpu(10);
        client.setDisk(10);
        client.setUpdated(System.currentTimeMillis());
        client.setUptime(10);
        client.setUrl(title);
        // give back our results
        return client;
    }

    /**
     * Test of add method, of class CentrumManager.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        CentrumManager instance = new CentrumManager();

        try{
            CentrumClient client = createDummy("Hello");
            instance.add(client);
        }
        catch (Exception e){
            fail("Failed to add a new client");
        }
    }

    /**
     * Test of get method, of class CentrumManager.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String URL = "Hello";
        CentrumManager instance = new CentrumManager();

        // Add the client to our list
        CentrumClient expResult = createDummy(URL);
        instance.add(expResult);

        // get the client from the list
        CentrumClient result = instance.get(URL);
        assertEquals(expResult, result);
    }



    /**
     * Test of remove method, of class CentrumManager.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        String URL = "Hello";
        CentrumManager instance = new CentrumManager();

       try{
            CentrumClient client = createDummy(URL);
            instance.add(client);}
        catch (Exception e){
            fail("Failed to add a new client");
        }

        // Remove the client from the list
        instance.remove(URL);
        
        // ensure that we only get null as result
        CentrumClient result = instance.get(URL);
        assertEquals(null, result);
    }


    /**
     * Test of getAll method, of class CentrumManager.
     */
    @Test
    public void testGetAll() {
        System.out.println("getAll");
        CentrumManager instance = new CentrumManager();

        // Add some clients
        instance.add(createDummy("Hello"));
        instance.add(createDummy("World"));
        instance.add(createDummy("What's up?"));

        // get all clients
        Collection result = instance.getAll();

        // we must have a specific number of clients available
        assertEquals( result.size(), 3);
    }

    /**
     * Test of CheckExpiration method, of class CentrumManager.
     */
    @Test
    public void testCheckExpiration() {
        System.out.println("CheckExpiration");
        CentrumManager instance = new CentrumManager();

        CentrumClient removeMe = createDummy("Expired");

        // make this client outdated on purpose
        removeMe.setUpdated(System.currentTimeMillis()
                - removeMe.getInterval() - 100000);

        // Add some clients
        instance.add(createDummy("Hello"));
        instance.add(removeMe);
        instance.add(createDummy("World"));
        instance.add(createDummy("What's up?"));

        // run our test function, it should remove one of the clients (removeMe)
        instance.CheckExpiration();

         // get all clients
        Collection result = instance.getAll();

        // we must have a specific number of clients available
        assertEquals( result.size(), 3);
    }

}