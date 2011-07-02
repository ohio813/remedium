/*
 * The log record allows creating an object that will hold a log message.
 * This class is used by LogMessage to hold a list of log records.
 *
 * A neat advantage of this class is that we can expand it one day to provide
 * translated log messages when available.
 */

package system.log;

/**
 *
 * @author Nuno Brito, 1st of July 2011 in Darmstadt, Germany.
 */
public class LogRecord{

    private String
            message;  // text of the message
    private int
            gender,   // is this an INFO, DEBUG, ERROR message?
            code;     // what is the code number of this message?
            //result;   // what was the result received?
    private String[]
            args;     // arguments used for translating the message text
    private boolean
            hasArgs = false; // defines if this record has arguments or not

    /** Set the content for this message */
    public void set(final int gender, final String message,
            final String... args){

        this.message = message;
        this.gender = gender;
        this.args = args;
        this.hasArgs = true;
    }

    /** Set the content for this message */
    public void set(final int gender, final String message){
        this.message = message;
        this.gender = gender;
    }

    public String[] getArgs() {
        // output a copy of the array to avoid direct exposure
        String out = utils.text.arrayToString(args, ";");
        return out.split(";");
    }

    public int getCode() {
        return code;
    }

    public int getGender() {
        return gender;
    }

    /** Convert all arguments onto the nicely formatted text */
    private String convert(){
        String result = message.replace("%", "%!%");
        int count = 1;
        while(result.contains("%!%")){
            result = result.replace("%!%"+count, args[count-1]);
            count++;
        }
        return result;
    }

    /** Gets our message. If it has arguments, they are converted.*/
    public String getMessage() {
        String result = message;
        if(hasArgs)
            result = "["+utils.text.translateStatus(gender)+"] " +convert();
        // provide our result
        return result;
    }

}


