/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package system.net;

import java.util.Properties;
import system.msg;

/**
 *
 * @author Nuno Brito
 */
public class ticketType implements msg {

    public String
            ticket, // the ticket number
            to, // to whom the msg recipient is located
            from, // from who does the msg comes from?
            address, // where the msg recipient is located
            interval = "1",   // wait at least n seconds before next call
            timeout = "600"; // how many seconds should we wait for our request
                             // to be completed? (using 10 minutes as default)
                             // 1 minute = 60 seconds, therefore 10 minutes = 10 * 60

   public Long
            sendDate, // date when it was first sent
            previousTry; // when did we last sent an update status?

   public int
            status;  // current update status of this entry

   /**
    * Prepares our ticket as a proper msg object
    */
   public Properties prepareTicket(){
        Properties output = new Properties();
            output.setProperty(msg.FIELD_TO, to);
            output.setProperty(msg.FIELD_FROM, from);
            output.setProperty(msg.FIELD_TICKET, ticket);
            output.setProperty(msg.FIELD_ADDRESS, address);
            output.setProperty(msg.FIELD_PARAMETERS,
                    protocols.propertiesToString(output));
        return output;
   }


   /**
    * Evaluates if this msg still hasn't expired
    */
   public boolean isValid(){
        long timeNow = System.currentTimeMillis();
        long timeExpired = Long.parseLong(timeout)*1000;
        long expireDate = timeExpired + sendDate;
        //System.out.println(timeExpired +" + "+sendDate+" ("+expireDate+") > " + timeNow);
   return ( expireDate > timeNow) && (status != msg.COMPLETED);
   }

   /**
    * Evaluates if we can request a status update for this ticket
    */
   public boolean canAsk(){
        long timeNow = System.currentTimeMillis();
        long inter = Long.parseLong(interval) * 1000;
        long timeInterval = previousTry + inter;// + sendDate;
        //System.out.println(previousTry +" + "+inter+" ("+timeInterval+") > " + timeNow);
   return (timeNow >= timeInterval);
   }
   
}
