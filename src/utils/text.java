/*
 * This class provides some handy routines for filtering text and providing
 * a safe input for our applications.
 */

package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import system.mq.msg;

/**
 *
 * @author Nuno Brito
 */
public class text {


     /**
        * Gets a string value from laptop characteristics based on a given pattern.
        * A Matcher object is used internally.
        *
        * @param source string containing the text to be parsed
        * @param reg regular expression pattern to use
        * @param group index of one of the groups found by the pattern
        * @return String containing the found pattern, or null otherwise
        */
        public static String findRegEx(String source, String reg, int group) {
        String out = null;

        Pattern p = Pattern.compile(reg); // Prepare the search pattern.
        Matcher matcher = p.matcher(source); // Retrieve our items.

        if (matcher.find()) {
        try {
        out = matcher.group(group);
        } catch (Exception e) {}
        }

        return out;
        }


     /**
      * Get safe string
      * Picks a given string and cleans out any invalid characters, replacing
      * spaces with "_"
      */
       public static String safeString(String input){

           input = input.replace(" ", "_");

           String output =
                utils.text.findRegEx( // only accept a-Z, 0-9 and -, _ chars
                        input,"[a-zA-Z0-9-_]+$", 0);

       return output;
       }

     /** Convert an array of strings to one string.
      *  Put the 'separator' string between each element.
      */
    public static String arrayToString(String[] a, String separator) {
        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1; i<a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        return result.toString();
    }

    
    /** Provides the index number of a given string inside an array.
     Returns -1 if the string was not found, 0 equals to the first item and
     so forth.*/
    public static int arrayIndex(String find, final String[] fields){
        int result = -1;
        int count = 0;
            for(String field : fields){
                if(field.equalsIgnoreCase(find))
                    return count;
                count++;
            }
        return result;
    }

    
    /**
     * Provide a textual representation of our system status value
     */
    public static String translateStatus(String status){
        return translateStatus(Integer.parseInt(status));
    }
    public static String translateStatus(int status){

        String s = "";

        switch (status) {
            case msg.COMPLETED:
                s = "COMPLETED";
                break;
            case msg.PENDING:
                s = "PENDING";
                break;
            case msg.EXPIRED:
                s = "EXPIRED";
                break;
            case msg.TIMEOUT:
                s = "TIMEOUT";
                break;
            case msg.REFUSED:
                s = "REFUSED";
                break;
            case msg.ERROR:
                s = "ERROR";
                break;
            case msg.STOPPED:
                s = "STOPPED";
                break;
            case msg.RUNNING:
                s = "RUNNING";
                break;
            case msg.PAUSED:
                s = "PAUSED";
                break;
            case msg.RESUME:
                s = "RESUME";
                break;

            default:
                s = Integer.toString(status);
                break;
        }
        return s;
    }

    /**
     * This method tests if a given string is empty or null
     * It is required to ensure that we can compile this application using
     * Java 1.5
     */
    public static Boolean isEmpty(String input){
    Boolean result = (input == null) || (input.length() == 0);
    return result;
    }




    /** get the string ready for output as debug */
     public static String doFormat(String title, String value){
         return title + " = " +value+ "; ";
     }

    /** convert an int value to hex */
     public static String getHex(final String title, final int value){
         String result = java.lang.Integer.toHexString (value);
         return doFormat(title, "0x"+result.toUpperCase());
     }

    /** convert a Long value to hex */
    public static String getHex(final String title, final long value){
         String result = java.lang.Long.toHexString (value);
          return doFormat(title, "0x"+result.toUpperCase());
     }

    /** Add a new element to a static String array */
     public static String[] stringArrayAdd(final String[] input,
             final String newText){
         String result = "";
         // iterate all elements
         for(String current : input)
             if(current.length() > 0)
             result = result.concat(current + ";");
         result = result.concat(newText + ";");
         // write back our list
        return result.split(";");
     }

   /** Remove an element from a static String array */
     public static String[] stringArrayRemove(final String[] input,
             final String removeText){
        String result = "";
        for(String current : input){
            if (current.equalsIgnoreCase(removeText))
                continue;
            else
                result = result.concat(current + ";");
        }
       // write back our list
        return result.split(";");
     }


   /** Remove an element from a static String array */
     public static String[] stringPrune(final String[] input){
        String result = "";
        for(String current : input){
            if (current.isEmpty())
                continue;
            else
                result = result.concat(current + ";");
        }
       // write back our list
        return result.split(";");
     }




    /** Picks a string and makes it URL safe */
    public static String quickEncode(String input){
        String
                result = input.replace(" ", "%20");
                result = result.replace("&", ".!.AND");
                result = result.replace("=", ".!.EQUAL");
                result = result.replace("/", ".!.fslash");
                result = result.replace("\\", ".!.bslash");
                result = result.replace("?", ".!.question");
        return result;
    }
    /** Decodes a URL safe string  */
    public static String quickDecode(String input){
        String
                result = input.replace("%20", " ");
                result = result.replace(".!.AND", "&");
                result = result.replace(".!.EQUAL", "=");
                result = result.replace(".!.fslash", "/");
                result = result.replace(".!.bslash", "\\");
                result = result.replace(".!.question", "?");
        return result;
    }

}
