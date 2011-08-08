/*
 * The component is intended to control the User Interface, along with
 * receiving and dispatching messages from other processes.
 */

package system.process;

import app.user.User;
import system.core.Component;
import java.util.ArrayList;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.html.Button;
import system.mqueue.msg;

/**
 * @author Nuno Brito, 4th of April 2011 in Germany.
 */
public class ManagerComponent extends Component implements msg{



    private String
          // used to pad the columns of entries on our table
          pad = "&nbsp&nbsp",
          // set components to be hidden from public view
          hideComponents = "manager_main;trayicon;system",
          whitelist = "log";


       public ManagerComponent(Remedium assignedInstance){
       super(assignedInstance);
    }


    @Override
    public void onStart() {

    // write the new page just before dropping everything
        html.setSection(html.SectionAbout, getAboutText());
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return "manager";
    }

    /** Instruct our system to stop */
    private String stopSystem(){
            log(INFO,"Received a request to stop");
            String result =
              "The system has stopped and "
              + "this window will close itself within 3 seconds.."
              + html.br
              //+ html.javascript.closeWindow(3)
              ;
            // write the new page just before dropping everything
            html.setSection(html.SectionHome, result);

            return result;
    }

    @Override
    /** Do our expected replies to web requests */
    public String doWebResponse(Request request, Response response) {

        // get the action parameter (if available)
        String action = utils.internet.getHTMLparameter(request,"action");

        // if the action says "stop" then go for it.
        if(action.equalsIgnoreCase("stop")){
                // get the final message
                String result = stopSystem();
                // ensure that it gets printed
                this.finishWebResponse(request, response, result);
            // Stop remedium
            getInstance().stop();

            return result;
        }


        // create a button for the stop request
        Button stopButton = new Button("Stop system",
                this.getCanonicalName()+"?action=stop");

        String result =
              getListOfProcess();


        // add a stop button if we are the administrators of the system
        if(this.isLogged(request)){ // are we logged?
            User user = this.getLoggedUser(request); // who are we?
            if(user.getMyName().equals(msg.admin)) // are we admin?
              result = result.concat( // add the button
                html.br + html.br
              +  stopButton.getText()
                      );
        }
        // add this text to our home page
        html.setSection(html.SectionHome, result);

        return "";
    }

    /** create a list of active processes */
    private String getListOfProcess(){

        String result =
                "<H3>Available components</H3>\n" +
                "<HR><TABLE>\n" +
                "<TR><TD><B>Name</B></TD>\n"+
                "<TD><B>Status</B></TD>\n"
                //"<TD><B>Actions"+pad+"</B></TD>\n"
                ;

        // get all the running processes
            ArrayList<Status> results =
                    getInstance().getProcess().getList();

            

            for(Status process : results){
                if(whitelist.contains(process.getName())==true){
                }
                    else
                  if(process.getName().contains("/") // ignore sub components
                        // hide unwanted components from our list
                        || hideComponents.contains(process.getName())
                                )
                    continue; 
                //String name = process.getName();

                result = result.concat(
                        "<TR>"

                        + "<TD><TT>"
                        + printNameLink(process) + pad
                        + "</TT></TD>\n"
                        +"<TD><TT>"
                        + utils.text.translateStatus(process.getStatus())
                        +"</TT></TD>\n"

//                        + "<TD><TT>\n"
//                        + printStatus(process)
//                        + "</TT></TD>"


                        + "</TR>\n"
                        );
            }

            result = result +"</TABLE>\n";

        return result;
    }

    private String printNameLink(Status process){
        String result ="";
        if(process.getStatus() == STOPPED){
            result = "<span style=\"color: rgb(153, 153, 153);\">"
                    +process.getName()
                    +"</span>";
        }
        else
            result = html.doLink(process.getName(), process.getName());
        return result;
   }
    
    /** HTML friendly status of a process for our list */
    private String printStatus(Status process){

        String result ="";

        if(process.getStatus() == STOPPED)
            result = html.doLink("Start", process.getName()+"?action=start");

        if(process.getStatus() == RUNNING)
            result = html.doLink("Stop", process.getName()+"?action=stop");

        if(process.getStatus() == PAUSED)
            result = html.doLink("Resume", process.getName()+"?action=resume");

        if(process.getStatus() == ERROR)
            result = html.doLink("Start", process.getName()+"?action=start");

        return pad + result + pad;
    }


    /** provide a simple explanation about the purpose of this component */
    private String getAboutText(){
    return
    "<h2>What is this \"manager\" application?</h2>"
    +"This is an application that lists the other applications that are also "
    +"available inside the system. It provides links so that you can visit "
    +"each application. For the meanwhile, this application is used as "
    +"default whenever the system starts. In the future, this setting might "
    +"change as default to a more user friendly application.<br>"
    +"<br>"
    +"<h2>What are the plans for the future?</h2>"
    +"The intention of the manager is to be used as a process manager. Meaning "
    +"that it should allow users to start, pause, resume and stop any of the "
    +"registered applications. Ideally, it would also be nice to provide an "
    +"overview of the up time for each application along with information of "
    +"the current actions that each of them is performing. <br>"
    +"<br>"
    +"We would also like to introduce better security features, for example, "
    +"allow users to define which applications are made available to the "
    +"outside network connections and even define a password/account to access "
    +"them. <br>"
    +"<br>"
    +"These are some of our plans for the future. For the moment we are only "
    +"providing basic functionality.<br>"
    +"<br>";
    }

}
