/*
 * This test case will validate the functioning of the clientType class
 * that is used for the Centrum server
 */

package obsolete;

import app.centrum.CentrumManager;
import system.mqueue.msg;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito
 */
public class clientTypeTest_Obsolete {

    public clientTypeTest_Obsolete() {
    }

    /**
     * Test for adding a record of data and calling the isValid method
     */
    @Test
    public void testIsAlive() {
        System.out.println("isValid");
        CentrumManager instance = new CentrumManager();
        

        // Create a dummy set of data
        Properties client = new Properties();

        client.setProperty(msg.FIELD_NAME, "Hello");
        client.setProperty(msg.FIELD_URL, "localhost");
        client.setProperty(msg.FIELD_UPDATED,
                Long.toString(System.currentTimeMillis()));
        // set the score result
        client.setProperty(msg.FIELD_CPU,"none");
        client.setProperty(msg.FIELD_RAM,"none");
        client.setProperty(msg.FIELD_DISK,"none");
        client.setProperty(msg.FIELD_BANDWIDTH,"none");
        client.setProperty(msg.FIELD_UPTIME,"none");

//        // write the data
//        assertEquals(true, instance.write(client));
//
//        // test with all require fields available, should output TRUE
//        boolean expResult = true ;
//        boolean result = instance.isAlive();
//        assertEquals(expResult, result);
    }

}