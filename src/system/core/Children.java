/*
 * Provides access to the children of a given component. The father component
 * uses this class to access services such as the database of the children.
 */

package system.core;

import java.util.Enumeration;
import java.util.Hashtable;
import system.mq.msg;

/**
 *
 * @author Nuno Brito, 19th of May 2011 in Pittsburgh, USA
 */
public class Children {

    Component instance;

    /** Public constructor */
    public Children(Component assignedComponent){
        this.instance = assignedComponent;
    }

    
    private Hashtable<String, Component>
            children = new Hashtable();


    /** Send a stop signal to all children */
    public void stop(){
    // get an enumeration of our children
        Enumeration em = children.elements();
        // iterate and add them to the arraylist
        while(em.hasMoreElements()){
            Component child = (Component)em.nextElement();
            child.doStop();
        }
    }

    /** Get access to a children if it exists */
    public Component get(String title){
        return children.get(title);
    }

         /** Add a given component as our child */
    public final Boolean add(Component child){
        // preflight checks
        if(child == null){
            instance.log(msg.ERROR, "Add child operation failed. Child component is null");
            return false;
        }
        if(children.containsKey(child.getTitle())){
           instance.log(msg.ERROR, "Add child operation failed. Component '"
                    +child.getTitle()
                    +"'+is already a child of mine");
            return false;
        }

        // all checks passed, add this component to the children list
        children.put(child.getTitle(), child);

        instance.log(msg.ROUTINE,"Added '"+child.getTitle()
                +"' to the list, we now have "
                +children.size()+" children."
                );
        // return as true since we had no troubles
        return true;
    }


}
