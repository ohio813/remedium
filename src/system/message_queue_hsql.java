/*
 * The Message queue is fundamental to ensure that our each application
 * inside our system is capable of communicating with others.
 */
package system;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import system.net.protocols;

/**
 * We are basing this Message queue on the HSQL database. In the future
 * we might indeed use a dedicated MQ but for the meanwhile we will be using
 * this one instead.
 *
 *
 * @author Nuno Brito, 18th of May 2011 in Pittsburgh, USA
 */
public class message_queue_hsql implements message_queue_interface, Message {

    // are we debugging this class?
    private boolean debug = false;

    int // status messages
            // -1 = ERROR, 0 = STOPPED, 1 = PAUSED, 2 = RUNNING
            status = STOPPED;
    private Remedium remedium = null;

    @Override
    public boolean start() {
        boolean result = true;

        // If we are running, no restart should be allowed.
        if (getStatus() != STOPPED) {
            log(ERROR, "Queue needs to at STOPPED state before starting");
            return false;
        }

        // do we need to start up the database system by ourselves?
        if (!getRemedium().getDB().hasStarted()) {
            result = getRemedium().getDB().start();
        }
        if (!result) {
            log(ERROR, "Failed to start database system");
            return result;
        }
        // set our status as "RUNNING"
        setStatus(RUNNING);
        // Flush down all messages from this queue
        // during the flush, recreate the Message table
        flush();

        if (result) {
            log(ROUTINE,"Message queue is running");
            setStatus(RUNNING);
        }

        return result;
    }

    @Override
    public boolean stop() {
        setStatus(STOPPED);
        return getStatus() == STOPPED;
    }

    @Override
    public boolean pause() {
        setStatus(SUSPENDED);
        return getStatus() == SUSPENDED;
    }

    @Override
    public boolean resume() {
        setStatus(RUNNING);
        return getStatus() == RUNNING;
    }

    boolean flush() {
        // we need to simulate the lack of persistence, therefore
        // if any older table exists, we drop it here to start all over again

        // drop table if it exists
        Boolean table_reset = getRemedium().getDB().update(
                "DROP TABLE IF EXISTS " + TABLE_MESSAGES).equalsIgnoreCase(TRUE);

        // create a table with our required fields
        Boolean table_create = getRemedium().getDB().update(
                "CREATE TABLE " + TABLE_MESSAGES + " ( "
                + FIELD_ID + " INTEGER IDENTITY, "
                + FIELD_TO + " VARCHAR(256), "
                + FIELD_FROM + " VARCHAR(256), "
                + FIELD_CREATED + " VARCHAR(256), "
                + FIELD_TICKET + " VARCHAR(256), "
                + FIELD_STATUS + " VARCHAR(256), "
                + FIELD_ADDRESS + " VARCHAR(256), "
                + FIELD_PARAMETERS + " LONGVARCHAR )").equalsIgnoreCase(TRUE);

        // if all went fine, output a true value
        return (table_reset && table_create);
    }

    /**
     * We use this method to send new messages on the queue. We leave the
     * object data properties to be generic on purpose. The overall idea is
     * to ensure that we can add more features as progress moves while
     * retaining backward compatibility.
     *
     * @param data Set of the INI-style parameters, defined on the header of
     * this class
     * @return True if the new Message is placed on the queue
     */
    @Override
    public boolean send(Properties data) {
        // if the MQ is not running, then start it up
        if (getStatus() == STOPPED) {
            start();
        }

        // pre-flight check, we need to ensure that this Message is valid
        if (  !data.containsKey(FIELD_TO)
           || !data.containsKey(FIELD_FROM)
             //   || !data.containsKey(FIELD_PARAMETERS)
           || (getStatus() != RUNNING) // we need to be running
                ) {
            log(ERROR, "Message not placed on the queue");
            return false;
        }

        // add our own timestamp to the data record
        long timestamp = remedium.getTime();
        data.put(FIELD_CREATED, Long.toString(timestamp));

        // include all other properties of this object inside the PARAMETERS field
        // we convert the Properties object onto a simple string that is later reconstructed
        data.setProperty(FIELD_PARAMETERS,
                protocols.propertiesToString(data));

        // place the data record on the queue
        Boolean result = getRemedium().getDB().update(
                "INSERT INTO " + TABLE_MESSAGES + "("
                + FIELD_TO + ","
                + FIELD_FROM + ","
                + FIELD_CREATED + ","
                + FIELD_TICKET + ","
                + FIELD_STATUS + ","
                + FIELD_ADDRESS + ","
                + FIELD_PARAMETERS
                + ") VALUES("
                + "'" + data.getProperty(FIELD_TO) + "', "
                + "'" + data.getProperty(FIELD_FROM) + "', "
                + "'" + data.getProperty(FIELD_CREATED) + "', "
                + "'" + data.getProperty(FIELD_TICKET, "") + "', "
                // if not status is specified then set status to PENDING
                + "'" + data.getProperty(FIELD_STATUS, utils.text.translateStatus(PENDING)) + "', "
                + "'" + data.getProperty(FIELD_ADDRESS, "") + "', "
                + "'" + data.getProperty(FIELD_PARAMETERS,"") + "') ")
                .equalsIgnoreCase(TRUE);

        // output the result to get some idea of what is happening here
        if (debug) {

            if (data.containsKey(FIELD_TICKET)) 
                log(DEBUG,"Message with ticket #" + data.getProperty(FIELD_TICKET)
                        +" was placed on queue: " + data.toString());
            else
                log(DEBUG,"Message placed on queue: " + data.toString());
        }
        // all done
        return result;
    }

    // Status handling routines
    public int getStatus() {
        return status;
    }

    void setStatus(int newStatus) {
        status = newStatus;
    }

    public boolean isRunning() {
        return (status == RUNNING);
    }

    private void log(int gender, String message) {
        if(remedium!=null)
            remedium.log(
                "message_queue",
                gender,
                message
        );
        else // there is no remedium initialized
           System.out.println("[message_queue][" + gender + "] " + message);
    }

    private void debug(String message) {
        if (debug) {
            log(DEBUG, message);
        }
    }

    // returns an array of messages from the queue for a given recipient
    @Override
    public ArrayList<Properties> get(String messageRecipient) {
        return getField(messageRecipient, FIELD_TO);
    }

    // returns an array of messages from the queue for a given recipient
    @Override
    public ArrayList<Properties> getTicket(String ticketID) {
        return getField(ticketID, FIELD_TICKET);
    }

    // returns an array of messages from the queue for a given recipient
    @Override
    public ArrayList<Properties> getExternal() {
        String query = FIELD_ADDRESS +" IS NOT NULL "
                +"AND "+FIELD_ADDRESS+" <> ''"
                +"AND "+FIELD_STATUS +" = '"
          + utils.text.translateStatus(PENDING) +"' ";
        //System.out.println("---------> "+query);
        return getExpression(query);
    }

    private ArrayList<Properties> getField(String value, String field) {
        return getExpression(field + " = '" + value + "'");
    }

    // returns an array of messages from the queue for a given recipient
    //private ArrayList<Properties> getField(String value, String field) {
    private ArrayList<Properties> getExpression(String SQLexpression) {
        if (!isRunning()) { // are we running?
            //log("error","Queue is not running");
            return null; }
        if (SQLexpression.equalsIgnoreCase("")) { // is the expression available?
            log(ERROR,"Invalid query");
            return null;
        }


        // initialize our variable.
        ArrayList<Properties> answer = new ArrayList<Properties>();

        // prepare our query expression
        String expression = "SELECT * FROM " + TABLE_MESSAGES + " WHERE "
                + SQLexpression;

        //run our query
        try {
            Statement st = null;
            ResultSet rs = null;
            st = getRemedium().getDB().conn.createStatement();
            rs = st.executeQuery(expression); // run the query

            // prepare to output our answer using the provided results
            Properties p = null;

            for (; rs.next();) {

                // clear the variable holder
                p = new Properties();
                // get all our Message fields
                p.setProperty(FIELD_ID, rs.getObject(1).toString());
                p.setProperty(FIELD_TO, rs.getObject(2).toString());
                p.setProperty(FIELD_FROM, rs.getObject(3).toString());
                p.setProperty(FIELD_CREATED, rs.getObject(4).toString());
                p.setProperty(FIELD_TICKET, rs.getObject(5).toString());
                p.setProperty(FIELD_STATUS, rs.getObject(6).toString());
                p.setProperty(FIELD_ADDRESS, rs.getObject(7).toString());
                p.setProperty(FIELD_PARAMETERS, rs.getObject(8).toString());

                // add this record to our answer
                answer.add(p);
            }

            st.close(); // NOTE!! if you stop a statement the associated ResultSet is lost
        } catch (SQLException ex) {
            return null;
        }
        // return our results
        if(answer.size() > 0)
            debug("Got message: "+answer.toString());
        
        return answer;
    }

    // after reading a Message, it can be disposed using the Message ID
    @Override
    public Boolean delete(String messageID) {
        String expression = "DELETE FROM " + TABLE_MESSAGES + " WHERE "
                + FIELD_ID + " = '" + messageID + "'";
        Boolean result = getRemedium().getDB().update(expression)
                .equalsIgnoreCase(TRUE);
        return result;
    }

    // after reading a Message, it can be disposed using the ticket ID
    public Boolean deleteTicket(String ticketID) {
        String expression = "DELETE FROM " + TABLE_MESSAGES + " WHERE "
                + FIELD_TICKET + " = '" + ticketID + "'";
        Boolean result = getRemedium().getDB().update(expression)
                .equalsIgnoreCase(TRUE);
        return result;
    }

    @Override
    public boolean hasStarted() {
        return (getStatus() != STOPPED);
    }

    @Override
    public void setRemedium(Remedium remedium) {
        this.remedium = remedium;
    }

    @Override
    public Remedium getRemedium() {
        return remedium;
    }
}
