/*
 * This is the Global app launcher and access point.
 *
 * Here we declare instances that allow other applications to share services
 * and launch other processes.
 * 
 */
package system.core;

import app.centrum.CentrumComponent;
import app.sentinel.SentinelComponent;
import app.files.FileServer;
import app.files.LicenseComponent;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.mqueue.msg;
import system.process.ManagerComponent;

/**
 *
 * @author Nuno Brito, 13th of April 2011 in Darmstadt, Germany.
 */
public class global extends Component {


  public global(Remedium assignedInstance, String AcceptedComponents){
       super(assignedInstance);

       // only process the list if it is not empty
       if(utils.text.isEmpty(AcceptedComponents) == false){
       // split a list of components onto distinct items
           for(String comp : AcceptedComponents.split(";")){
               // Record our accepted components, they are listed using ;
               this.settings.setProperty(comp, "");
               log(msg.DEBUG,"Allowing component '"+comp+"' to start");
           }
       // save the full list of allowed components
       this.settings.setProperty("components", AcceptedComponents);}
        else // if none is listed, accept all that we can find
       this.settings.setProperty("components", "");
     }

  
     /** Check if component is allowed to start. If not components are
      specified then it should allow all components to start */
     private boolean isAllowed(String who){
        Boolean result = false;
         try{
         if(settings.containsKey(who))
             result = true;
         if(settings.getProperty("components").contentEquals(""))
             result = true;
         }catch (Exception e){}
        return result;
     }


  /** Kickstart authorized components to start */
  private void startComponents(){

    if(isAllowed("manager")){
         ManagerComponent Manager = new ManagerComponent(getInstance());
         Manager.getCanonicalName();
         }
      
    if(isAllowed(msg.sentinel)){
         SentinelComponent sentinelComponent = new SentinelComponent(getInstance());
         sentinelComponent.getCanonicalName();
         }

    LicenseComponent license = null;
    if(isAllowed("license")){
         license = new LicenseComponent(getInstance());
         license.getCanonicalName();
         }


    if(isAllowed("file")){
         FileServer fileServer = new FileServer(getInstance(), license);
         fileServer.getCanonicalName();
         }

    
//    if(isAllowed("sdk")){
//        SDK sdk = new SDK(getInstance());
//        sdk.getCanonicalName();
//         }

    if(isAllowed("centrum")){
        CentrumComponent centrum1 = new CentrumComponent(getInstance(), null);
        centrum1.getCanonicalName();
         }


     }


    @Override
    public void onStart() {

        startComponents();

         }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return "main";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return "Hello world!";
    }
}
