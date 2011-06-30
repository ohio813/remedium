/*
 * This component provides conversion between different formats. For example,
 * it will pick on a text file and translate the file onto a format that can
 * be stored inside our source code as a string.
 */

package app.sdk;

import system.core.Component;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 11th of April 2011 in Darmstadt, Germany.
 */
public class SDK extends Component{

    public SDK(Remedium assignedInstance){
       super(assignedInstance);
     }
    @Override
    public void onStart() {

        // add the introduction text of our home page
        html.setSection(html.SectionAbout, ""
                + "The SDK (Standard Development Kit) component provides"
                + " a set of tools to ease the life of programmers that wish"
                + " to introduce changes on this system."
                + html.br
                + html.br
                + ""
                + ""
                + "");


                // add the introduction text of our home page
        html.setSection(html.SectionHome, ""
                + "The SDK (Standard Development Kit) component provides"
                + " a set of tools to ease the life of programmers that wish"
                + " to introduce changes on this system."
                + html.br
                + html.br
                + "To propose new tools added on this kit, please get in "
                + "contact with Nuno Brito (nuno@sys32.org)"
                + ""
                + ""
                + "");

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
        return "sdk";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return "Hello world!";
    }

}
