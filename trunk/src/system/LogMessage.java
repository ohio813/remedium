/*
 * The log message allows creating an object that will hold a log message.
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

package system;

/**
 *
 * @author Nuno Brito, 30th of June 2011 in Darmstadt, Germany.
 */
public class LogMessage {

    private String
            message;  // text of the message
    private int
            gender,   // is this an INFO, DEBUG, ERROR message?
            code;     // what is the code number of this message?
            //result;   // what was the result received?
    private String[]
            args;     // arguments used for translating the message text

    /** Set the content for this message */
    public void set(final int gender, final int code,
            final String message, final String... args){

        this.message = message;
        this.gender = gender;
        this.code = code;
        this.args = args;

    }


    /** Set the content for this message */
    public void set(final int gender, final String message){
        this.message = message;
        this.gender = gender;
    }

    /** Set the content for this message */
    public void set(final int gender, final int code, final String message){
        this.message = message;
        this.gender = gender;
        this.code = code;
    }

    public String[] getArgs() {
        // output a copy of the arryay to avoid direct exposure
        String out = utils.text.arrayToString(args, ";");
        return out.split(";");
    }

    public int getCode() {
        return code;
    }

    public int getGender() {
        return gender;
    }

    public String getMessage() {
        return message;
    }


}
