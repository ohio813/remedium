/*
 * On this test we are focusing on the generic implementation of the player
 * component. We will test the following actions:
 *
 *      - Register a few users
 *      - Update the data from a given user
 *      - Get the list of registered users
 *      - Delete a given user
 */

package players;

import players.player;
import players.user;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nuno Brito
 */
public class user_test {

    static players.user users = new players.user();


    public user_test() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

        // do a general cleansing
        Properties data = new Properties();
        // clean data previously available
        data.put("FLUSH", "true");
        // start the players system
        assertEquals(true,users.start(data));

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    /**
     * On this part we will close down our player system and clean out
     * any traces of our testing.
     */
        // do a general cleansing
        Properties data = new Properties();
        // clean data previously available
        data.put("FLUSH", "true");
        // start the players system
        assertEquals(true,users.stop(data));
    }


     @Test
     public void registerTest() {

         // we create a new user called "Nuno"
         assertEquals(true,
                users.register("Nuno")
                     );

         // Now we create another user
         assertEquals(true,
                users.register("Chuck Norris")
                     );

         // we try to create a second user with the same identifier (it must fail)
         assertEquals(false,
                users.register("Nuno")
                     );

         // we also try to create a user without providing a name
         assertEquals(false,
                users.register("")
                     );

     return;
     }


     @Test
     public void updateTest() {
         /**
          * On this test we will pick on a user that already exists and we'll
          * add some extra details like contact information and the such
          */

         Properties data = new Properties();
         
         // put some rubbish on the parameters
         data.put(user.FIELD_NAME, "Nuno");
         data.put(user.FIELD_PARAMETERS, "rubbish");

         // update our data
         assertEquals(true,users.update(data));

         // now repeat the test but do it for someone that doesn't exist
         data.put(user.FIELD_NAME, "Bruno");
         assertEquals(false,users.update(data));
     }

     @Test
     public void getAllTest() {
         /**
          * Now we'll get a list of everyone registed at our system
          * Given our previous testings, this should output two users.
          */
         for(Properties p : users.getAll())
             System.out.println(p.get(user.FIELD_NAME));

         // verify if we have 2 users or not
         assertEquals(2,users.getAll().size());
     }

     @Test
     public void deleteTest() {
         /**
          * On this test we will delete a particular user and also test the case
          * where we delete a user that doesn't exist
          */
         assertEquals(true, users.remove("Nuno"));

         // should output as false
         assertEquals(false, users.remove("Nuno"));

         // should output as false
         assertEquals(false, users.remove("Bruno"));

     }

}