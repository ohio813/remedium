/*
 * This class provides data synchronization between remote containers.
 *
 * We place all the synchronization related method on this location to keep
 * the ContainerHSQL class readable, otherwise it would host a gigantic number of
 * lines that would become unbearable to manage.
 *
 * We also ease the task of using other synchronization providers in the future.
 */

package system.container;

//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import system.mqueue.msg;
import system.core.Component;

/**
 *
 * @author Nuno Brito, 3rd of May 2011 in Darmstadt, Germany.
 */
public class ContainerSync {


    // the objects made available when the class is constructed
    private Component component;
    private Container container;

    private Boolean
            allowSyncAuthorize = true;

    /** This public constructor associates this class to a container */
    public ContainerSync(Component assignedComponent,
                        Container assignedContainer){
        //preflight checks
        if(assignedContainer == null){
            System.out.println("ContainerLog error: Assigned container "
                    + "is null");
            return;
        }

        if(assignedComponent == null){
            System.out.println("ContainerLog error: Assigned component "
                    + "is null");
            return;
        }

        // map the provided objects to the objects of this class
        component = assignedComponent;
        container = assignedContainer;
    }



     /** The receiver part of the synchronize action */
    public String synchronizeRequester(String who,
            final String since, final String until){

         // we remotely ask instance A to synchronize with instance B
        String request =
             "http://" + who
             + "/"
             + component.getCanonicalName()
             + "?db=" + container.getName()
             + "&action=sync" // call remote sync, sync is reserved
             + "&who=" + component.getInstance().getNet().getAddress()
             + "&since=" + since
             + "&until=" + until
             ;

        log(msg.INFO, "Requesting: " + request);

        // get the record data
        String result = utils.internet.getTextFile(request);

        log(msg.DEBUG, "Downloaded fresh data, processing it now");

        // now do the updating part
        
        // clean up the new lines from the result
        result = result.replace("\n", "");

        // iterate all our found records
        int i = 0;
        for(String record : result.split(";</br>")){
            ++i;
            // split each record into fields
            String[] data = record.split(";");

            // write this data inside our container
            container.write(data);
        }

        result = "Update ok! Counted " + i + " records";

        log(msg.DEBUG, result);
        return result;
    }


    /** Synchronize this container with an externally located container. */
    public final String synchronizeProvider(final String who,
            final String since, final String until){
        /**
         * We like big data. Instead of filling up RAM memory, we are going
         * to use HSQL directly and retrieve one record at a time that will
         * be synchronized with a remote container.
         *
         * We call this technique: "little pieces". The idea is that using
         * little pieces (records), we are capable of synchronizing a whole
         * container and a whole set of containers with little worries, instead
         * of using big bulks of data.
         *
         * So, we are container A and want to synchronize with container B
         *
         *  Steps of action:
         *      - Ask container B if he has any records for a given date
         *      - If so, ask him to provide us those records
         *          - If less than 10 000, provide as a web page
         *          - If more than 10 000, provide them throught a message queue
         *          and give us an idea of how many messages we should expect
         *      - Add provided data on our own database
         */

        // preflight checks
        final long Since, Until;

         // pre flight checks
          try { // we need valid values to process dates
                Since = Long.parseLong(since);
                Until = Long.parseLong(until);
        } catch (Exception e){
            return "Error. Invalid date format";
        }
        // we need to be running
         if (!container.isRunning()) {
            String error = "Get operation failed. The storage instance is not "
                    + "running";
            log(msg.ERROR, error);
            return "Error. " + error;
        }


        // if are not authorized to provide any data, stop right here
        if( getSyncAuthorize() == false){
            return "Synchronization is not authorized";
        }

   // step 1 - Get all the data between the requested time interval

        // count all records that match the time interval
       long count = 0;

        try{
                //TODO if the count is zero, an exception occurs. I CAN'T SOLVE IT!
                count = container.countBetween(Since, Until);

        }catch (Exception e){}
        finally {}


       // No records? No need to continue
       if(count == 0){
           log(msg.ROUTINE, "No records to provide");
           return "No records to provide";
       }

       // if they are less than 10 000, show them as web page
       log(msg.DEBUG,"Dispatching " + count +" records to " +who);

       // (for the moment ignore the 10 000 records restriction)


   // step 2 - Get all records pertaining the mentioned time interval

        // variable where results will be kept
        String result = "";


        result = "Hello there!";

        // Well done Sir.
        return result;
    }



    public Boolean getSyncAuthorize() {
        return allowSyncAuthorize;
    }

    public void setSyncAuthorize(Boolean allowSyncAuthorize) {
        this.allowSyncAuthorize = allowSyncAuthorize;
    }




 public final void log(int gender, String message) {
        component.log(gender, message);
    }



}
//        // expression to get our records
//         String expression =
//                "SELECT * FROM " + container.getName() + " WHERE "
//                + msg.FIELD_DATE_CREATED
//                + " > '" + since + "'"
//                + " AND "
//                + msg.FIELD_DATE_CREATED
//                + " < '" + until + "'"
//                ;
//
//
//        // initialize our variables
//        Statement st = null;
//        ResultSet rs = null;
//
//          //run our query
//        try {
//            st = db.conn.createStatement(); // statement objects can be reused with
//            rs = st.executeQuery(expression); // run the query
//
//            for (; rs.next();) {
//                String record = "";
//                // read all our msg fields
//                for (int i = 1; i < container.getStoreFields().length + 1; i++) {
//                    // add each record, split it using a ";"
//                  record = record.concat(rs.getObject(i + 1).toString())+";";
//                }
//                // at the end of line add the following code:
//               result = result.concat(record)
//                       +"</br>"
//                       + "\n"
//                       ;
//
//            }
//            st.close(); // NOTE!! if you stop a statement the ResultSet is lost
//        } catch (SQLException ex) {
//            log(msg.ERROR,"Synchronization failed. An exception occured "
//                    + "when calling " + expression);
//            return "Error, exception occured";
//        } finally{
//            try {
//                st.close();
//                rs.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(ContainerHSQL.class.getName())
//                        .log(Level.SEVERE, null, ex);
//            }
//        }