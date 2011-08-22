/*
 * The system class is the backbone of the services provided
 * by Remedium. It is a component that will hold as children other
 * componens.
 *
 * Provided components:
 *  - Tray Icon (TrayIconComponent.class)
 *
 *
 */

package remedium;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.core.Component;
import app.misc.TrayIconComponent;
import app.user.UserComponent;
import system.log.LogComponent;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 28th of May 2011 in Darmstadt, Germany.
 */
public class SystemRem extends Component {

    String remID = ""; // the identification of this remedium instance

   /** the public constructor */
    public SystemRem(Remedium assignedInstance){
        super(assignedInstance);
   }

   /** Start all components tht provide services to the remedium platform */
    private void kickstartComponents(){

 // tray icon
        TrayIconComponent tray =
                new TrayIconComponent(this.getInstance(), this);

        UserComponent user = new UserComponent(getInstance(), null);
        user.getCanonicalName();


// log service
        LogComponent logComp =
                new LogComponent(this.getInstance(), null);
        logComp.getCanonicalName();

    }


    @Override
    public void onStart() {

        // provide a storage to keep data
        kickstartConfiguration();

        // get all components running
        kickstartComponents();

        // output success message
        log(msg.ROUTINE, "Ready for action!");
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return "system";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return "";
    }



    /** Create an ID for this instance if one does not exists already */
    private String generateID(){
        String result =
            utils.math.RandomInteger(100, 999)
            +"."+
            utils.math.RandomInteger(100, 999)
            +"."+
            utils.math.RandomInteger(100, 999)
            ;
        return result;
    }


    /** Provide persistence access to this component */
    private synchronized void kickstartConfiguration(){
        String result =
                ini.read(msg.FIELD_ID_SERIAL);

                if(utils.text.isEmpty(result)){
                //if(result.isEmpty()){
                    remID = generateID();
                    log(msg.INFO,"Writing our individual ID as '" + remID+"'");
                    ini.write(msg.FIELD_ID_SERIAL, remID );
                }
                else{
                    remID = result;
                    log(msg.INFO,"This instance is known as '"+remID+"'");
                }
    }

    public String getRemID() {
        return remID;
    }


}
