/*
 * The log component allows users to view the system messages from a browser.
 *
 */

package system.log;

import system.core.Component;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 26th of July 2011 in Darmstadt, Germany.
 */
public class LogComponent extends Component{

    public LogComponent(Remedium assignedInstance, Component assignedFather){
        // call the super component!
         super(assignedInstance, assignedFather);
     }

    @Override
    public void onStart() {
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return "log";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        String result = getLogText();
        this.html.setSection(html.SectionHome, result);
        // redirect to the main page
        addAutoHTMLrefresh(3, this.getWebAddress());
        // all done
        return getTitle();
    }


    /** Get the text from our log system*/
    private String getLogText(){
        String result = this.getInstance().getLog().getHTMLLog(25);

        return result;
    }

}
