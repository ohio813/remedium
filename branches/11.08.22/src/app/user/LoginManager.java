/*
 * This class manages multiple attempsts of login for a given user.
 * At each attempt from a given IP address, the time counter will increase up
 * to a maximum waiting time per attempt.
 *
 * In extreme, it should even refuse the login after n attempts within a given
 * time period.
 *
 * This time restriction should be lifed after a given time has passed
 */

package app.user;

/**
 *
 * @author Nuno Brito, 28th of July 2011 in Darmstadt, Germany
 */
public class LoginManager {

       // settings
    int increaseFactor = 2; // multiplier to the time waiting
    int increaseValue = 5;  // unit of time to be multiplied
    long expireTime = 15; // how many minutes until our restrictions expire?

    // objects
    String who; // IP address of the user that is trying to login
    int when = 0; //
    int HowManyTimes = 0;

    public void addUser(String who){

    }

    /** How long should this person wait? */
    public int wait(String who){
        return 0;
    }



}

/** Define our own object that represents the sum of login attempts for a given
 IP address */
class login{



}


/** Where we store details about each person trying to login */
class Login{

}

