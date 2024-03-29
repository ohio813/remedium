/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package app.example;

import system.core.Component;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 24th of April 2011 in Darmstadt, Germany.
 */
public class TestComponent extends Component{

    public TestComponent(Remedium assignedInstance, long assignedRemLock,
            Component assignedFather){
        // call the super component!
         super(assignedInstance, assignedFather);
     }

    @Override
    public void onStart() {
        log(INFO,"Starting fresh");
    }

    @Override
    public void onRecover() {
    }

    @Override
    public void onLoop() {
       // log(INFO,"boinc");
    }

    @Override
    public void onStop() {
        log(INFO,"Stopping");
    }

    @Override
    public String getTitle() {
        return "cheers";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return getTitle();
    }
}
