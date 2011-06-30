/*
 * Typically, we would expect to see the log output its messages onto a given
 * output such as the screen console. The problem is that too much information
 * is bothersome to control and we need a more efficient way of filtering the
 * data that we really want.
 *
 * This class allows to perform such filtering and also allows to query for
 * specific strings inside the log entries which are particularly useful when
 * doing test cases since we typically have no access to the application and
 * roles, but we can still analyze their communication.
 *
 *
 *
 * //TODO We need to clean up old log entries or store them in the DB
 * //TODO Add a start time and end time to know how long each instance survives
 * //TODO Warn when errors occur
 * //TODO Allow triggers to call specific methods
 * //TODO Add a UI to let us browse through the log entries
 */

package system;

import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 14th April 2011 in Darmstadt, Germany
 */
public class log_handler implements Message{

    // settings
    //Boolean debug = true; // true if you want debugging messages

    // objects
    private Remedium
            remedium = null;

    private ArrayList<log_record>
            logs = new ArrayList<log_record>();

    private Properties
            filterExcludeComponent = new Properties(),
            filterExcludeGender = new Properties(),
            filterIncludeComponent = new Properties(),
            filterIncludeGender = new Properties();


    public log_handler(Remedium rem){
        // preflight checks
        if(rem == null){
            System.out.println("[log_handler] We can't initiate with "
                    + "a null instance");
            return;
        }
        // assign our local instance to running instance
        this.remedium = rem;
    }

    /**
     * Interpret the gender type of a given record
     */
    private String interpret(int gender){

        String temp = "";

        switch (gender) {
            case INFO: temp =  "info"; break;
            case DEBUG: temp =  "debug"; break;
            case EXTRA: temp =  "extra"; break;
            case ROUTINE: temp = "routine"; break;
            case ERROR: temp =  "error"; break;
            case WARNING: temp =  "warning"; break;
            default: temp =  ""+gender; break;
        }

            return temp;
    }
    
    /**
     *
     * @param ID Name of the remedium that is running
     * @param who Name of the Application/Role that wrote the Message
     * @param gender Type of Message (ERROR, INFO, DEBUG)
     * @param Message the Message itself
     */
    public void out(String who, int gender, String message) {

        boolean showLog = true;

        // check if there are exclusive filters
        if(
            (filterIncludeGender.size() > 0)
            //showLog = isIncludedType(gender);
            ||
            ( filterIncludeComponent.size() > 0)
            )
            showLog = isIncludedComponent(who)
                    || isIncludedType(gender);


        if(showLog)
        // is this message on the filtered list?
        if(  (isExcludedComponent(who)// filter out by type
          || (isExcludedType(gender)) // filter out by gender
          )) // don't output messages from filtered roles
          showLog = false;



        // show message only if result is true
        if(showLog)
        // output message to console
        System.out.println(
                "[" + remedium.getIDname() + "]["
                + who
                + "]["
                + interpret(gender)
                + "] "
                + message);

        // regardless of being filtered or not, store the log entry
        this.addRecord(who, gender, message);
    }


    /**
     * Add a new log record onto our list
     */
    private void addRecord(String who, int gender, String message){
        log_record record = new log_record();
        record.set(who, gender, message);
        logs.add(record);
    }

    /**
     * Quickly iterate through all log messages and see if there is a partial
     * match for any of the messages listed. This is an handy feature for test
     * cases being able of debugging particular features while observing only
     * the sequence of log entries.
     */
    public Boolean containsEntry(String who, String what){

         for(log_record record : logs) // iterate all records
            if(record.getWho().equals(who)) // get only the ones to a given person
                if(record.getMessage().contains(what)) // does it contain part of the Message?
                    return true;
        // no dice, return as false;
        return false;
    }


    /** Filter out the messages from unwanted roles */
    public void filterExcludeComponent(String who){
        filterExcludeComponent.setProperty(who, "");
    }
    /** Filter out the messages from unwanted genders */
    public void filterExcludeGender(int gender){
        filterExcludeGender.setProperty(interpret(gender), "");
    }

    /** Ensures that our filter does NOT contain a given role blacklisted */
    private Boolean isExcludedComponent(String who){
        return filterExcludeComponent.containsKey(who);
    }
    private Boolean isExcludedType(int gender){
        return filterExcludeGender.containsKey(
                interpret(gender)
                            ) == true;
    }


    /** Filter out the messages from unwanted roles */
    public void filterIncludeComponent(String who){
        filterIncludeComponent.setProperty(who, "");
    }
    /** Filter out the messages from unwanted genders */
    public void filterIncludeGender(int gender){
        filterIncludeGender.setProperty(interpret(gender), "");
    }

    /** Ensures that our filter does NOT contain a given role blacklisted */
    private Boolean isIncludedComponent(String who){
        return filterIncludeComponent.containsKey(who) == true;
    }
    private Boolean isIncludedType(int gender){
        return filterIncludeGender.containsKey(
                interpret(gender)
                            ) == true;
    }


    /**
     * Show some numbers about the logs that were stored
     */
    public String showStats(){
        return (""+logs.size()+ " log records were tracked");
    }

    /**
     * Close the logger class, display some stats in the end
     */
    public void stop(Properties parameters){

        if( (parameters.containsKey(NO_STATS)==false) // cancel the stats completely
            && (remedium.isDebug()==true)
                )
            log(INFO,showStats()); // if we are debugging, show us the stats
    }

    private void log(int gender, String message) {
        remedium.log("log", gender, message);
    }

}

/**
 * This class is intended to store the log records as we move forward on the
 * logging
 */
class log_record{
    private String
            logWho,
            logMessage;
    private long
            logDate;
    private int
            logGender;

    public long getDate() {
        return logDate;
    }

    public int getGender() {
        return logGender;
    }

    public String getMessage() {
        return logMessage;
    }

    public String getWho() {
        return logWho;
    }

    /**
     * Set all the fields required for this record
     */
    public void set(String who, int gender, String message){
        // there is no preflight check, we want this to be fast
            logWho = who;
            logGender = gender;
            logMessage = message;
            logDate = System.currentTimeMillis();
        return;
    }



}
