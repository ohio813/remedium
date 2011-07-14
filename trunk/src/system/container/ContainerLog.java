/*
 * This class will track modifications applied on a given container.
 * It will track data that was added, deleted and modified with the intention
 * of later exporting these changes when requested.
 */

package system.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.log.LogMessage;
import system.mq.msg;
import system.core.Component;

/**
 *
 * @author Nuno Brito, 24th of May 2011, 11000 meters above the Atlantic Ocean
 */
public class ContainerLog implements msg{

    // the objects made available when the class is constructed
    Component component;

    String
            store_name = ""; // the name for the storage container

//    private long
//            exportMax = 10000; // max number of records to be exported per turn

        // definitions
    public boolean
            debug = true, // should we display debugging messages or not?
            isLog  = false,   // should this container be tracked or not?
            logStarted = false;  // has the log tracking started or not?

    private Container
            logDB; // where we keep track of changes


    
    /** This public constructor associates this class to a container */
    public ContainerLog(Component assignedComponent,
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

       // use the store_name from the original container
       store_name = assignedContainer.getName();

    }



        /** Write this entry onto our log */
    public void writeLog(String query){
        // preflight checks
        if(isLog)
            return; // no log activated? No reason to continue

        if(!logStarted) // start log container if not started already
            kickstartLog();

        // write this record onto our log container
        try {

            logDB.write(
                    //containerLock,
                    new String[]
            {"" + System.currentTimeMillis(),
             utils.Base64.encodeObject(query)
            });

            //log(DEBUG,"Wrote log");
        } catch (IOException ex) {
            Logger.getLogger(Container.class.getName()).log(Level.SEVERE,
                    null, ex);
        }

    }

    /** Kick start our log tracking if this wasn't available before */
    private void kickstartLog(){
        LogMessage result = new LogMessage();
        // part related to the log tracking, create our special container
        // inside this one
        logDB = new Container(store_name + "_log",
                    new String[]{FIELD_CREATED, FIELD_CONTENT},
                    this.component.getInstance().getStorage(), result);

        logStarted = true;
    }


    /** Export the contents of our container. The exported data allows
     * to replicate the construction of a given database. Each export will
     * provide a set of SQL scripts that should be executed by the target
     * container.
     *
     * @param Date Export data since a given date up to present
     */
    public ArrayList<Properties> export(Long date){
        // preflight checks
        if(this.isLog == false) // only export if the container is a log
            return null;



        return null;
    }



    /** Output a message using the component's log system. */
    public final void log(int gender, String message) {
        component.log(gender, "ContainerLog: " + message);
    }

}
