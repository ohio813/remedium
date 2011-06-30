/*
 * The overall idea is to ensure that we have a user class that lives
 * on his own table.
 *
 * We are providing this class at an abstract level that doesn't depend on any
 * given implementation althought right now we are using HSQL
 */

package players;

/**
 *
 * @author Nuno Brito
 */
public class user extends player{

    // this is class is called everytime "users" is created on the code.
    public user(){
        // define the table name and title for this player
        setType("User");
    }

}