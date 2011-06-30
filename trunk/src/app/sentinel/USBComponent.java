/*
 * This class will detect whenever a USB drive is inserted on the workstation.
 * It is intended to work across Windows, Linux and MacOS.
 *
 */

package app.sentinel;

import utils.USBautorun;
import system.core.Component;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import utils.USBtracker;

/**
 *
 * @author Nuno Brito, 8th of June 2011 in Darmstadt, Germany.
 */
public class USBComponent extends Component{

    // settings
    private int
            timer = 1; // how many seconds between loops?

    // objects
    private boolean 
            hasBeenGrabbed = false;

    private USBtracker // track whenever a USB device is inserted on the machine
            tracker = new USBtracker();

    private USBautorun // USB immunization of autorun.inf file
            autorun = new USBautorun();

    /** Public constructor */
    public USBComponent(Remedium assignedInstance, Component assignedFather){
         super(assignedInstance, assignedFather);
     }

    /** ensure that we only take over of the main icon when it is necessary */
    private void iconGrab(){
        // preflight, only repeat once
        if(hasBeenGrabbed)
            return;
            // send the message
            utils.tweaks.updateTrayIconAction(this.getInstance().getMyAddress(),
               this.getInstance().getMyAddress() +"/"+ sentinel_usb);
            hasBeenGrabbed = true;
    }
    /** We don't need the system tray icon, let it go..*/
    private void iconRelease(){
        // preflight, only repeat once
        if(hasBeenGrabbed == false)
            return;
            // send the message
            utils.tweaks.updateTrayIconAction(this.getInstance().getMyAddress(),
               "default");
            hasBeenGrabbed = false;
    }

    @Override
    public void onStart() {
         // set our loop for two seconds
        this.setTime(timer);
        log(ROUTINE,"Prepared to process new USB devices when inserted");
    }


    @Override
    public void onRecover() {
    }

    @Override
    public void onLoop() {
        // get the status of removable drives
        int result = tracker.noChanges;

       try{
        result = tracker.updateDriveStatus();
        }catch(Exception e){}
        
        // if we detect a new USB drive, react immediately
        if(result == tracker.hasNewDrive){
            // on some bizarre cases we might have more than one drive to handle
            for(String newDrive : tracker.getRemovableDrives())
                // process the newly found drive
                processDrive(newDrive);
            // grab the attention of the system tray icon
            iconGrab();
        }
        // a removable drive was removed
        if(result == tracker.driveRemoved){
            // if there are no more removable drives on the machine..
            if(tracker.count() == 0)
                // ..release the tray icon
                 this.iconRelease();
        }
    }


    /** Process a newly inserted drive on this machine */
    private void processDrive(String drive){
        Boolean result = autorun.immunize(drive);

        if(result == true)
            log(INFO, "Drive '" + drive +"' is immunized");
        else
            log(INFO, "Failed to immunize drive '" + drive +"'");
    }

    @Override
    public void onStop() {
        log(INFO,"Stopping");
    }

    @Override
    public String getTitle() {
        return "usb";
    }

    @Override
    public String doWebResponse(Request request, Response response) {
        return getTitle();
    }
}
