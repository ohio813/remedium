/*
 * This class allows to getStatus a hold of processes that are currently active
 * on our system. It also allows to kill a process, order pausing and many
 * other features that you would also expect at an Operative System.
 *
 * One process can only hold a single identification and entry on our list.
 *
 * Any given process that is registered will share information to any others
 * using his entry at the process manager. This way, the process is not linked
 * to any other process in particular.
 *
 * This design allows that an unspecified number of processes can access the
 * current status of any given process.
 *
 *
 * For example, if we have the Sentinel role performing a scan on the computer
 * disk, this role will setStatus the current status of the scanning on his
 * entry at the process manager.
 *
 * This way, we can implement a Graphical User Interface that will pool the
 * status of the Sentinel role and extract the data from the scanning that is used
 * to inform the user about what is going on.
 *
 * This same data can also be used to implement a command line interface where
 * the user can view the progress on a plain text format.
 *
 * Last but not least, we can even use this concept to later introduce a web
 * interface that will use the same data as all the interfaces mentioned before.
 *
 *
 * This type of functioning adds an effective abstraction between the logical
 * layer of our application (the Sentinel scanning) and the presentation layer
 * (the way how data is displayed to users).
 *
 * On this file we don't mention the third layer: persistent storage, but
 * rest assured that it is also planned in a manner that allows us enough
 * abstraction to guarantee flexibility on the long term.
 *
 *
 */

package system.process;

import system.core.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 3rd of April 2011 in Germany.
 */
public abstract class ProcessManagerAbstract implements msg{

    // definitions
    private Boolean debug = false;

    // objects
    private Hashtable
            list = new Hashtable();

    // list all our registered components
    private HashMap<String, Component>
            componentList = new HashMap<String, Component>();

    private Remedium
            instance;

    /** the constructor for our class */
    public ProcessManagerAbstract(Remedium assignedInstance){
        // preflight check
        if(assignedInstance == null){
            System.out.println("Process Manager failed to start. Cannot"
                    + " use a null instance as support");
        }

        // use this instance as support for integration with the system
        instance = assignedInstance;

        log(ROUTINE,"Process Manager is running");
    }

    
    /** Adds a component to our list, allowing it to be later retrieved */
    public Boolean addComponent(Component who){
        // preflight checks
        if(who == null){
            log(ERROR, "Add component failed. Provided component is null");
            return false;
        }
        if(componentList.containsKey(who.getCanonicalName())){
            log(ERROR, "Add component failed. Component '"
                    + who.getCanonicalName()
                    +"'+is already listed");
            return false;
        }
        // all checks passed, add this process to the list
        componentList.put(who.getCanonicalName(), who);
        log(ROUTINE,"Added '"+who.getCanonicalName()+"' to the list"
                //+componentList.size()+" processes are listed."
                );

        if(debug)
         log(DEBUG,"Added '"+who.getCanonicalName()+"' to the list, ");

        //TODO this should not be necessary in the future
        this.add(who.getProcess());

        // return as true since we had no troubles
        return true;
    }


    /**
     * Adds a new process on our list
     */
    public Boolean add(Status who){
        // preflight checks
        if(who == null){
            log(ERROR, "Add operation failed. Provided process is null");
            return false;
        }
        if(list.containsKey(who.getName())){
            log(ERROR, "Add operation failed. Process '"+who.getName()
                    +"'+is already listed");
            return false;
        }
        // all checks passed, add this process to the list
        list.put(who.getName(), who);
        log(ROUTINE,"Added '"+who.getName()+"' to the list, "
                +list.size()+" processes are listed."
                );
        // return as true since we had no troubles
        return true;
    }


    /** Set the status of a given process */
    public synchronized Boolean setStatus(Status newStatus){
        // preflight checks
        if(newStatus == null){
            log(ERROR, "Set status operation failed. Provided process is null");
            return false;
        }
        if(exists(newStatus.getName())==false){
            log(ERROR, "Set status operation failed. Process is not listed");
            return false;
        }

        // all checks passed, update this process on the list
        list.put(newStatus.getName(), newStatus);
        // return as true since we had no troubles
        return true;
    }

    // check if a given process exists or not
    public Boolean exists(String who){
        return list.containsKey(who);
    }

    /** getStatus the details from only a given process.
     @returns null if the process was not found*/
    public synchronized Status getStatus(String who){

        Status process = (Status) list.get(who);

        if(process == null){
//            log(ERROR, "getStatus operation failed. Process '"+who+"' was"
//                    + " not found");
            return null;
        }
        // return what we have for this process
        return process;
    }


    /** getStatus the details from only a given process.
     @returns null if the process was not found*/
    public synchronized String getWeb(String who, Request request,
            Response response){

        Component component = (Component) componentList.get(who);

        if(component == null){
            if(debug)
                log(DEBUG,"getWeb operation failed. Did not found '"+who+"'");
            return null;
        }

        // return what we have for this process
        return component.getWebResponse(request, response);
    }


    /** Get a list of all registered processes */
    public synchronized ArrayList getList(){
        // we use to this pr
        ArrayList<Status> result = new ArrayList();
        // get an enumeration
        Enumeration em = list.elements();
        // iterate and add them to the arraylist
        while(em.hasMoreElements())
        {
            Status process = (Status)em.nextElement();
            result.add(process);
        }
        // send everything out
        return result;
    }


    /** kills a process that is running */
    public Boolean kill(String who){
        //TODO, we need to kill the associated process from our list

        // remove this process from our list
        list.remove(who);
        return true;
    }

    /** log messages to standard output */
    private void log(int gender, String message) {
        instance.log("manager", gender, message);
    }

}
