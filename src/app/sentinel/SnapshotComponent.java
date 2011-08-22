/*
 * The snapshot component is used to track changes on specific folders.
 * It will index all files inside all sub directories of a given target
 * and repeat this action as time moves on.
 *
 * At any given point in time we should have a clear knowledge about the
 * files contained inside folders.
 *  - If a file is changing often, we should see these changes reported
 *  - If desired, we can ignore reports for files specified by the user
 *  - We should also start using our voting system working in the snapshots
 *  - This class is not the same as the base line scan made by the Indexer, we
 *  can actually see some action going on around here..
 *
 */

package app.sentinel;

import system.core.Component;
import java.io.File;
import java.util.HashMap;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 12th of June 2011 in Darmstadt, Germany.
 */
public class SnapshotComponent extends Component{


    // objects

    HashMap<String, SnapshotTracker>
            snaps = new HashMap(); // store our ongoing snapshots



    public SnapshotComponent(Remedium assignedInstance,
            Component assignedFather){
        // call the super component!
         super(assignedInstance, assignedFather);
     }

    
    /** Add a new snapshot object to our group */
    public void add(String folder){
        File root = new File(folder);
    }
    

    @Override
    public void onStart() {
        log(msg.INFO,"Snapshot service is ready");
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
        log(msg.INFO,"Stopping");
    }

    @Override
    public String getTitle() {
        return "snapshot";
    }

    public void doSnapshot(){
    }


    @Override
    public String doWebResponse(Request request, Response response) {
        return getTitle();
    }
}
