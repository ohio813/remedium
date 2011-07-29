package system.net;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import remedium.Remedium;
import system.mqueue.msg;

/**
 *
 * The container is the specification from the simple web framework where we
 * define what happens at each web request that arrives at our system
 *
 * On this case, we only ensure that received messages meet the security
 * and integrity requirements and then we place them on the system msg queue
 *
 * We also provide a ticket that requesting applications can use to keep track
 * of their requests on subsequent calls.
 *
 * @author Nuno Brito, 18th of May 2011 in Pittsburgh, USA.
 */
public class network_simple_server implements Container, msg {

    // should we debug this class or not?
    private boolean debug = false;
    private String ticket;
    private Remedium instance = null;

    // the text that identifies our web server
    private String id = "remedium/1.0";

    public network_simple_server(Remedium assignedInstance) {
        super();
        this.instance = assignedInstance;
    }



    /** Redirects the reply to a given component of our system */
    private void askComponent(Request request, Response response){
            // get the component name if any
            String compName
                    = request.getPath().toString().substring(1);

            // If the name is null or empty
            if((compName==null)
                    || (compName.length()==0)){
                // no name found, get the deafault application running
                   compName = this.getRemedium().getDefaultApp();
            }

            // call the web page of the requested component
            instance.getManager().getWeb(compName, request, response);
       }

            // request.getPath().toString()
            // /manager/child

            // request.getPath().toString()
            // /manager/child?action=start&dir=test

            //request.getClientAddress().toString()
            // /127.0.0.1:60717

            // request.getClientAddress().getHostName()
            // 127.0.0.1


    /** Prepare the initial header of our reply */
    private Response prepareResponse(Response response){
            // get current system time
            long time = System.currentTimeMillis();
             // prepare our response
            response.set("Content-Type", "text/plain");
            response.set("Server", id);
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            // output the reply
            return response;
    }




    @Override
    public void handle(Request request, Response response) {
        PrintStream body = null;

        if (!getRemedium().getNet().isListening()) {
            log(ERROR,"We are not accepting new requests, ignoring the request");
            return;
        }

        if(debug)
        log(DEBUG,"New request arrived at port "
                + getRemedium().getNet().getPort());

        try {

            body = response.getPrintStream();

        } catch (IOException ex) {
            log(ERROR,"Handle operation failed. Error while parsing the text "
                    + "body");
        }


        ///////////////////////////////////////////////////////////////////


        // initialize our reply
            response = prepareResponse(response);


        // is this a data request or component request?
        // we identify the component requests if they have a "/"
        if
           (
           (request.getPath().toString().length()>0)
          &&
           // we need to avoid asychronous messages
           (!request.getAddress().toString().contains("?msg="))
                    )
        {
            // pass this argument as a web page/service
            this.askComponent(request, response);
            return;
        }

       ///////////////////////////////////////////////////////////////////



        // get current system time
        long time = System.currentTimeMillis();

        // create the raw data holder (for version 1)
        String data = "";

        try {
            // decode the received input (version 1 expected)
            data = request.getParameter("msg");

            // we've got our msg but it can't be empty
            if (utils.text.isEmpty(data)) {
                log(ERROR, "Received a message but it was empty");
                return;
            }

            // when getting our request, some characters like "+" are replaced by " ",
            // so, we'll add them back.
            data = data.replace(" ", "+");

            if(debug)
                log(ROUTINE,"Content of the message is " + data);

        } catch (IOException ex) {
            Logger.getLogger(network_simple_server.class.getName()).log(Level.SEVERE, null, ex);
            body.close();
            return;
        }



        // get the container from the provided string
        Properties container = protocols.stringToProperties(data);

        // all received messages encode the parameters field as the real object
        // that we want to exchange between instances. Therefore we need to
        // convert the text from PARAMETERS in order to get the full msg
        // along with any other extra properties that are included

        if (!container.containsKey(msg.FIELD_PARAMETERS)) {
            log(ERROR, "We received a message but did not found the "
                    + "message container");
            return;
        }

        // get the msg that will be placed on the msg queue
        Properties message = protocols.stringToProperties
                (container.getProperty(msg.FIELD_PARAMETERS));

        // verify if the received msg is valid
        if ((message == null)
         || (message.size()==0)
                )
        {
            log(ERROR, "We received a message but it is empty");
            return;
        }


        // before outputting the msg, remove the STATUS field to ease readability
        Properties out = message;
        out.remove(FIELD_STATUS);

        debug("Received message: "
                + out.toString());

        /**
         * On this part we should interpret the different
         * versions. Since we only have one type of communication
         * available for the moment, we will temporarily skip
         * this code section
         */
        //insert code to handle versions here.
////////////// is this a request or a ticket update?  //////////////////////////
        if ( // ensure that we have a real ticket request
                (message.containsKey(Network.FIELD_TICKET))
                && (message.getProperty(Network.FIELD_TICKET).length() > 0)) {

            // set our default ticket number
            ticket = message.getProperty(Network.FIELD_TICKET);

            log(ROUTINE,"STATUS request was made for ticket "
                    + ticket);


            // get the current status of our ticket
            ArrayList<Properties> ticketResult =
                    getRemedium().getMQ().getTicket(ticket);

            if (ticketResult.isEmpty()) {
                log(ROUTINE,"No ticket results found for #" + ticket);
                body.close();
                return;
            }

            // convert our reply to a plain string
            String convert =
                    protocols.propertiesToString(ticketResult.get(0));

            debug("Message queue returns: "
                    + ticketResult.get(0));


            // prepare our response
            response.set("Content-Type", "text/plain");
            response.set("Server", "remedium/1.0");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            // output the reply
            debug("Message received at time: " + time);
            body.println(convert);
            body.close();

        } else {
            // pre-flight check. Is our msg minimally valid?
            if ((message == null)
                    || !message.containsKey(Network.FIELD_TO)
                    || !message.containsKey(Network.FIELD_FROM)
                    //|| !message.containsKey(Network.FIELD_MESSAGE)
                    ) {
                log(ERROR,"Received a message, but it was incomplete: "
                        + message.toString());

                response.set("Content-Type", "text/plain");
                response.set("Server", "remedium/1.0");
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);

                body.close();
                return;
            }

            // set our ticket number for this messsage
            ticket = Long.toString(time);
                    //Integer.toString(utils.math.RandomInteger(100000, 99999));
            message.setProperty(msg.FIELD_TICKET, ticket);
            // set the status to PENDING

            message.setProperty(msg.FIELD_STATUS,
                    Integer.toString(Network.PENDING));

            // place msg on queue
            Boolean placeMQ = getRemedium().getMQ().send(message);

            // in case we fail to place this msg on the queue, stop here
            if (!placeMQ) {
                log(ERROR,"Failed to place message on the queue: "
                        + message.toString());
                body.close();
                return;
            }


            // convert our reply to a plain string
            String convert = protocols.propertiesToString(message);

            // prepare our response
            response.set("Content-Type", "text/plain");
            response.set("Server", "remedium/1.0");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            // output the reply
            debug("Message received at time: " + time);
            debug("Replying as: " + convert);

            body.println(convert);
            body.close();

        }
    }

    private void log(int gender, String message) {
        instance.log("network_server", gender, message);
    }

    private void debug(String message) {
        if (debug) {
            log(DEBUG, message);
        }
    }

    public void setRemedium(Remedium remedium) {
        this.instance = remedium;
    }

    public Remedium getRemedium() {
        return instance;
    }
}
