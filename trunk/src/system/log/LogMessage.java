/*
 *
 * The goal is to abstract our classes from the type of log that is used and
 * this way implement/use different log systems that match our needs.
 *
 * One example is when coding classes that interact with components, with this
 * class we can avoid using a component to test the intended functionalities.
 *
 * The resulting object will be interpreted by the class that receives it and
 * also allows us to assign a code to each message so that we can process them
 * independently of having the message text in English or any other language.
 */

package system.log;

import java.util.HashMap;

/**
 *
 * @author Nuno Brito, 30th of June 2011 in Darmstadt, Germany.
 */




public class LogMessage {

    
    LogRecord mostRecent; // the most recent log record on our records
    long counter = 0; // the counter of logged messages
    HashMap<String, LogRecord> list = new HashMap();
    
    /** Add a new message to our log */
    public void add(final int gender, final String message,
            final String... args){
        // transfer the data to the most recent log record we have
        mostRecent = new LogRecord();
        mostRecent.set(gender, message, args);
        // increase the counter
        counter++;
        // add this log onto our list, we use the counter as reference
        list.put("" + counter, mostRecent);

    }

    /** Return the gender from the most recent message */
    public int getResult() {
        return mostRecent.getGender();
    }

    /** Get the count of all logged records at this moment **/
    public long getCount() {
        return counter;
    }

    /** Get the text for the most recent message */
    public String getRecent(){
        return mostRecent.getMessage();
    }


    /** Checks if a given type of message has occured since a given time */
    public boolean hasOccured(final int gender, final long since){
        // preflight checks
        if(since > counter){// we can't provide requests that haven't occured
            return false;
        }


        // setup our count variable
        long count = since;
        // iterate all records
            while(counter > count){
                LogRecord record = list.get("" + count);
                if(record.getGender()== gender)
                    return true;
                count++;
            }
        return false;
    }

}
