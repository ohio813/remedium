/*
 * The Box class is intended to store and manage a group of containers.
 * It is associated with the Component class and allows components to create
 * and use them in a dynamic manner, without need of defining static variables.
 */

package system.container;

import java.util.HashMap;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.msg;
import system.core.Component;

/**
 *
 * @author Nuno Brito, 3rd of June 2011 in Darmstadt, Germany.
 */
public class Box {

    Component component;

    public Box(Component assignedComponent){
        component = assignedComponent;
    }

    private HashMap<String, Container> containerBox = new HashMap();


    /** Add a container to our box */
    public Container add(Container container){
        // preflight checks
        if(container == null)
            return null;

        String title = container.getName();

        containerBox.put(title, container);
        return container;
    }

    /** Return a container from our box */
    public Container get(String title){
        return this.containerBox.get(title);
    }

    /** Verify if a given container is registered or not inside our box */
    public Boolean exists(String title){
        return containerBox.containsKey(title);
    }

    /** List all containers registered inside the box */
    public String[] list(){
        // smart ass way of getting all the key names from an Hash Map
        String[] result = containerBox.keySet().toArray(new String[0]);
        return result;
    }

    /** Should registered containers accept update requests or not? */
    public void batchSyncAuthorize(Boolean newState){
        // get a list of containers
        String[] containers = this.list();
            // iterate each one
            for(String container : containers){
                // update the SyncAuthorize status
                this.get(container).setSyncAuthorize(newState);
            }
    }

    /** Synchronize all the containers in the box */
    public String batchSynchronize(Request request, Response response){
        // get a list of containers
        String[] containers = this.list();
          String result = "";
            // iterate each one
            for(String container : containers){
                log(msg.DEBUG,"Synchronizing '"+ container+"'");
                result = result.concat(
                        this.get(container).webRequest(request, response)
                        +"\n");
            }
                log(msg.DEBUG,"Synchronization complete");
            return result;
    }


    /** output messages to the outside world */
    public final void log(int gender, String message) {
        if(component == null)
            System.out.println(message);
        else
            component.log("box", gender, message);
    }

    /** Dump all records from our containers onto a folder */
    public boolean dump(String where){
     // We will dump all the records of each container on their own sub folders
        for(Container container : containerBox.values()){
         Boolean result = container.dump.toFolder(where);
         if(result == false)
             return false;
     }
        return true;
    }

    /** Import all records from a folder onto our containers */
    public boolean dumpImport(String where){
     // We grab all the records for each container from files on their folders
        for(Container container : containerBox.values()){
         Boolean result = container.dump.fromFolder(where);
         if(result == false)
             return false;
     }
        return true;
    }

}
