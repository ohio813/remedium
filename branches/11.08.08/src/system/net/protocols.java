/*
 * This class hold the protocols used throughout the system to exchange
 * information between different instances and applications.
 */

package system.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Base64;

/**
 *
 * @author Nuno Brito
 */
public class protocols {

    // should we debug this class or not?
    private boolean debug = true;


     /**
     * This method allows to convert a given Properties object onto
     * a string that can be dispatched safely across the network wire.
     *
     * This method is documented at ProtocolSpecifications.rtf
     * under the section "Version 1, code name: Properties"
     */
    public static String propertiesToString(Properties message) {
        String result = "";

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {

            message.storeToXML(output, "");
            result = Base64.encodeBytes(output.toByteArray(), Base64.GZIP);

        } catch (IOException ex) {
            Logger.getLogger(network_version1.class.getName()).log
                    (Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * This method will pick on a given string that was received using the
     * version 1 network protocol and will provide back a Properties object
     * that contains the data that was transmitted origingally
     */
    public static Properties stringToProperties(String message) {
        Properties result = new Properties();

        if (message.equalsIgnoreCase("")) {
            return null;
        }

        byte[] temp = null;

        try {

            temp = Base64.decode(message);

        } catch (IOException ex) {
            log("error","Received message was not valid as a Base64 envelope");
            return null;
        }

        try {


            ByteArrayInputStream in = new ByteArrayInputStream(temp);
            // load the Properties data from the input stream

            // if it is null we can't proceed
            if (in == null) {
                log("error","The message returned a null value, aborting "
                        + "the send procedure");
                return null;
            }
            result.loadFromXML(in);

        } catch (IOException ex) {
            Logger.getLogger(protocols.class.getName()).log(Level.SEVERE,
                    null, ex);
            return null;
        }

        return result;
    }

    private static void log(String gender, String message){
     System.out.println("[protocol]["+gender+"] "+message);
    }

    private void debug(String message){
     if(debug)
         log("debug",message);
    }

}
