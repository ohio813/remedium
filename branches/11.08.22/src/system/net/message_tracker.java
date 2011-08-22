package system.net;

import java.util.Hashtable;
import java.util.Properties;
import remedium.Remedium;

/**
 * The purpose of this class is to provide the means to track external tickets.
 * It is used for another class to register all outgoing messages with the
 * intention of knowing when it is time for asking a status update at the
 * target server.
 *
 * @author Nuno Brito, 19th of May 2011 in Pittsburgh, USA.
 */
public class message_tracker extends ticketType{

    // should we debug this message or not?
     private Boolean debug = false;

    // each ticket number holds the location where it should be checked
     private Hashtable tickets = new Hashtable();
    // turn this variable as true if you want debugging messages visible
     
    // is this thread already running?, has it stopped? is there an error?
//     private int
//             status = STOPPED;

     private String
             className; // define this class name


    // the instance designated for this thread
     private Remedium remedium = null;


   /**
     * The construtor for this class
     */
    public message_tracker(Remedium remedium) {
        super();
        // set the instance that is designated to this thread
        this.remedium = remedium;
        // set out className (used by logs)
        className = setClassName();
        debug("Message tracker launched");
    }



/**
 * This method will assess if a given string is empty or not
 * In case it is empty, it will make visible an error Message and
 * output true. In case it is not empty, outputs false.
 * 
 * @param test The string that will be tested
 * @param ErrorMessage Customize the Message in case of being empty
 */
    private boolean isEmpty(String test, String ErrorMessage){
        if(   (test == null)
           || (test.length() == 0)
                ){
            log(ERROR,ErrorMessage);
            return true;
        }
    return false;
    }


    private String setClassName(){
       String result;

      Class cls = this.getClass();
        result = cls.getName();

        result = cls.getName();
        if (result.lastIndexOf('.') > 0) {
            result =
                    result.substring
                        (result.lastIndexOf('.')
                    +1);  // Map$Entry
            }
        // The $ can be converted to a .
        result = result.replace('$', '.');      // Map.Entry

        return result;
    }

  

  private void log(int gender, String message) {
        if(remedium!=null)
            remedium.log(
                //"tracker",
                className,
//                +","
//                +this.getName(),
                gender,
                message
        );
        else // there is no remedium initialized
           System.out.println("[message_tracker][" + gender + "] " + message);
    }

    // only output messages if we are in debug mode
    private void debug(String message) {
        if (debug) {
            log(DEBUG, message);
        }
    }

    public boolean addEntry(Properties parameters) {
        ticketType temp = new ticketType();
        // avoid null situations
        if (parameters == null) {
            log(ERROR, "No valid parameters were provided, exiting addEntry");
            return false;
        }
        if (!parameters.containsKey(FIELD_TICKET)) {
            log(ERROR, "No ticket value was found, exiting addEntry");
            return false;
        }
        if (!parameters.containsKey(FIELD_TO)) {
            log(ERROR, "No recipient for this message was found, exiting addEntry");
            debug(parameters.toString());
            return false;
        }


        String ticketLocal = parameters.getProperty(FIELD_TICKET);
        String fromLocal = parameters.getProperty(FIELD_FROM);
        String toLocal = parameters.getProperty(FIELD_TO);
        String addressLocal = parameters.getProperty(FIELD_ADDRESS);


        // we don't want duplicates on our list of running tickets.
        if (tickets.containsKey(ticketLocal)) {
            debug("We already have the ticket #" + ticketLocal + " on our list");
            return false;
        }
        // ticket value can't be empty
        if (isEmpty(ticketLocal, "The ticket field is empty")) {
            return false;
        }
        if (isEmpty(fromLocal, "The FROM field is empty")) {
            return false;
        }
        if (isEmpty(toLocal, "The TO field is empty")) {
            return false;
        }
        if (isEmpty(toLocal, "The ADDRESS field is empty")) {
            return false;
        }
        temp.ticket = ticketLocal; // assign the ticket number
        temp.from = fromLocal; // from who does this Message comes from?
        temp.to = toLocal; // to whom?
        temp.address = addressLocal; // to where?
        temp.sendDate = remedium.getTime(); // when was this Message added?
        /**
         * Now that we have the minimum values, lets set up the optional
         * parameters if they were made available.
         */
        // if there is a specific timeout, use it
        if (parameters.containsKey(FIELD_TIMEOUT)) {
            temp.timeout = parameters.getProperty(FIELD_TIMEOUT);
        }
        if (parameters.containsKey(FIELD_INTERVAL)) {
            temp.interval = parameters.getProperty(FIELD_INTERVAL);
        }
        tickets.put(ticketLocal, temp);
        // placed ticket, output Message
        debug("Added ticket #" + temp.ticket + " from " + temp.from + " to " + temp.to);
        return true;
    }

    public void deleteTicket(String ticket) {
        tickets.remove(ticket);
    }

    public Hashtable getEntries() {
        Hashtable result = tickets;
        return result;
    }

    public Remedium getRemedium() {
        return remedium;
    }

    public int getStatus() {
        return status;
    }

    public int getTicketStatus(String ticket) {
        ticketType entry = (ticketType) tickets.get(ticket);
        return entry.status;
    }

    /** From which remote instance did this ticket come from? */
    public String getTicketOrigin(String ticket) {
        ticketType entry = (ticketType) tickets.get(ticket);
        if(entry == null) return "";
        return entry.address;
    }



    public boolean isRunning() {
        return status == RUNNING;
    }

    // Status handling routines
    public void setStatus(int newStatus) {
        status = newStatus;
    }

    public void setTicketStatus(String ticket, int newStatus) {
        ticketType entry = (ticketType) tickets.get(ticket);
        entry.status = newStatus;
    }

}// end message_tracker


///**
// * This class is used to store all the details pertaining a network ticket
// * It also contains some helper methods to manage each ticket
// */
//class ticketType implements Message{
//    public String
//            ticket, // the ticket number
//            to, // to whom the Message recipient is located
//            from, // from who does the Message comes from?
//            address, // where the Message recipient is located
//            interval = "1",   // wait at least n seconds before next call
//            timeout = "600"; // how many seconds should we wait for our request
//                             // to be completed? (using 10 minutes as default)
//                             // 1 minute = 60 seconds, therefore 10 minutes = 10 * 60
//
//   public Long
//            sendDate, // date when it was first sent
//            previousTry; // when did we last sent an update status?
//
//   public int
//            status;  // current update status of this entry
//
//   /**
//    * Prepares our ticket as a proper Message object
//    */
//   public Properties prepareTicket(){
//        Properties output = new Properties();
//            output.setProperty(Message.FIELD_TO, to);
//            output.setProperty(Message.FIELD_FROM, from);
//            output.setProperty(Message.FIELD_TICKET, ticket);
//            output.setProperty(Message.FIELD_ADDRESS, address);
//            output.setProperty(Message.FIELD_PARAMETERS,
//                    protocols.propertiesToString(output));
//        return output;
//   }
//
//
//   /**
//    * Evaluates if this Message still hasn't expired
//    */
//   public boolean isValid(){
//        long timeNow = System.currentTimeMillis();
//        long timeExpired = Long.parseLong(timeout)*1000;
//        long expireDate = timeExpired + sendDate;
//        //System.out.println(timeExpired +" + "+sendDate+" ("+expireDate+") > " + timeNow);
//   return ( expireDate > timeNow) && (status != Message.COMPLETED);
//   }
//
//   /**
//    * Evaluates if we can request a status update for this ticket
//    */
//   public boolean canAsk(){
//        long timeNow = System.currentTimeMillis();
//        long inter = Long.parseLong(interval) * 1000;
//        long timeInterval = previousTry + inter;// + sendDate;
//        //System.out.println(previousTry +" + "+inter+" ("+timeInterval+") > " + timeNow);
//   return (timeNow >= timeInterval);
//   }
//}