/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package system.net;

import java.util.Properties;
import system.Message;

/**
 *
 * @author Nuno Brito
 */
public class ticketType implements Message {

    public String
            ticket, // the ticket number
            to, // to whom the Message recipient is located
            from, // from who does the Message comes from?
            address, // where the Message recipient is located
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
    * Prepares our ticket as a proper Message object
    */
   public Properties prepareTicket(){
        Properties output = new Properties();
            output.setProperty(Message.FIELD_TO, to);
            output.setProperty(Message.FIELD_FROM, from);
            output.setProperty(Message.FIELD_TICKET, ticket);
            output.setProperty(Message.FIELD_ADDRESS, address);
            output.setProperty(Message.FIELD_PARAMETERS,
                    protocols.propertiesToString(output));
        return output;
   }


   /**
    * Evaluates if this Message still hasn't expired
    */
   public boolean isValid(){
        long timeNow = System.currentTimeMillis();
        long timeExpired = Long.parseLong(timeout)*1000;
        long expireDate = timeExpired + sendDate;
        //System.out.println(timeExpired +" + "+sendDate+" ("+expireDate+") > " + timeNow);
   return ( expireDate > timeNow) && (status != Message.COMPLETED);
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
