/*
 * The locker class provides basic security functionality for
 * methods that are exposed as public inside each class.
 *
 * By no means this is a fool proof mechanism but it will certainly
 * provide better security in methods that need to be made available
 * as public rather than applying no security at all.
 *
 * Also, using this class provides the nice advantage that we can log all the
 * wrong attempts, slow down on purpose the reply from the class in case of
 * multiple wrong attempts and provide warnings.
 *
 * In the future we might even regenerate the key as necessary and update the
 * key recipients where allowed.
 *
 */

package system.net;

/**
 *
 * @author Nuno Brito, 2nd of April 2011 in Germany.
 */
public class Locker {

    private Boolean
            debug = true;

    private String
            title = "";

    private long
            lock = 0; // the initial status of our lock key

    private int
            wrongAuthCounter = 0, // number of wrong authentication attempts
            waitCounter, // how long do we force bad people to wait for a reply?
            waitMax = 10; // what is the max wait time?

    /** Initiate the constructor and set up our initial values */
    public Locker(){

        if(lock > 0) {
            return ;
        } // don't accept this method to be re-called

        // create a new key for the class
        generateNewKey();
    }

    /** Initiate the constructor with a given key */
    public Locker(long providedKey, String assignedTitle){

        if(lock > 0) {
            return ;
        } // don't accept this method to be re-called

        // use a specific key for the class
        lock = providedKey;
        title = assignedTitle;
    }



    /**
     * Create a new key for this instance
     */
    private void generateNewKey(){
        lock = utils.math.RandomInteger(1, 9999999);
    }

    /**
     * To put it simply, we have received a wrong authentication.
     * We will record these number of wrong attempts to later deal with them
     * if they are excessive.
     */
    private void increaseWrongAuthCounter(){
        wrongAuthCounter++;
    }

    /**
     * There is nothing more annoying than making attackers wait for a reply.
     * We will slow them down depending on the number of bad replies
     */
    private void doWait(){
        /**
         * We increase the counter based on the number of bad attempts,
         * however, we also establish a limit to ensure that we don't suffer
         * badly from DDoS in the future.
         */
        if(waitCounter < waitMax)
            utils.time.wait(waitCounter);
        else
            utils.time.wait(waitMax);
    }


    /**
     * Verify if a given password matches with what we have on record
     *
     * @param unlock The key used to unlock a given functionality
     * @return True if the unlock key matches with the value that we expect
     */
    public Boolean check(long unlock){

        // no match, no dice.
        if(unlock != lock) {
            increaseWrongAuthCounter(); // report this incident
            doWait(); // punish each wrong attempt properly
            if(debug)
                System.out.println("----------------------->"
                        + "Authentication at "+title+" failed. "
                        +"Used "+unlock
                        +" while expecting "+ lock);
            return false;
        }

        // all is good
        return true;
    }


}
