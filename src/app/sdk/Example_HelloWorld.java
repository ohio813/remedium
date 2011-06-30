/*
 * This is a simple Hello world example.
 *
 * An example of how a component works from the web. You will
 * need to call this class from a Remedium instance
 */

package app.sdk;

import system.core.Component;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;

/**
 *
 * @author The Hello World
 */
public class Example_HelloWorld extends Component{

    /**
     * Create an instance of our component
     * @param assignedInstance The instance from whom we'll use their services
     * @param assignedRemLock A security password (provided from the instance)
     */
    public Example_HelloWorld(Remedium assignedInstance){
       super(assignedInstance);
     }
    @Override
    public void onStart() {
    }

    @Override
    public void onRecover() {
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return "hello"; // this is the used for URL location
    }

    @Override // this is our reply when a web request comes in
    public String doWebResponse(Request request, Response response) {
        return "Hello world!";
    }

}
