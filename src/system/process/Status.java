/*
 * The status class is intended to standardize the format of messages passed
 * as status on the process manager.
 * This class also allows to impose limits on the size of parameters that are
 * kept inside each instance, while allowing to abstract from the use of a
 * database or any other technology as desired in the future.
 */

package system.process;

import java.util.Properties;
import system.msg;

/**
 *
 * @author Nuno Brito, 3rd of April 2011 in Germany.3
 */
public class Status implements msg {

    private String
            name = "";

    private Properties
            parameters; // where we keep the parameters for this app

    private long
            updated, // when was this status modified?
            created; // when was this process created?

    private int
            status;  // what is the current status of this process?


    /** the constructor of this class */
    public Status(String assignedName){
        // preflight check
        if(assignedName==null){ // cannot allow null names
            return;
        }

        if(name.length()>0){ // cannot allow changes after the first start
            return;
        }
        if(assignedName.length() == 0){ // cannot use an empty name
            return; 
        }

        parameters = new Properties(); // where we hold all parameters

        long timestamp = System.currentTimeMillis(); // get the system time

        // set up the initial values
        updated = timestamp;
        created = timestamp;
        // we debut as inactive
        status = INACTIVE;
        // write the name of this instance
        name = assignedName;
        //System.out.println("--->--->---->"+name+"|||-->"+assignedName);
    }

    /** get the date when this process was registered */
    public long getCreated() {
        return created;
    }

    /** get the parameters defined for this process */
    public Properties getParameters() {
        return parameters;
    }

    /** get the operational status for this process (running, stopped, etc) */
    public synchronized int getStatus() {
        return status;
    }

    /** get the date when the process was last updated */
    public long getUpdated() {
        return updated;
    }

    /** return the name of this instance */
    public String getName() {
        return name;
    }

    /** Set the parameters shared inside this state */
    public void setParameters(Properties parameters) {
        this.parameters = parameters;
    }

    /** Set the current status of this process */
    public synchronized void setStatus(int status) {
        this.status = status;
    }

    

}
