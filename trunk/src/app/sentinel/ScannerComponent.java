/*
 * The scanner is the role that will find all files placed on the disk.
 * We only find files around here and store their locations.  We then
 * provide this information onto other parts of the application that will
 * process and request more information as needed.
 */

package app.sentinel;

import system.core.Component;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.Message;

public class ScannerComponent extends Component implements Message{

    // definitions
    private static String
            roleId = "scanner"; // the identification of this role
    private int
            //status = STOPPED, // current status
            depth = 35,  // the max depth here is 35 to prevent overflows
            throttle = 0; // if necessary, slow down processing to save CPU cycles
    private String
            where = "./"; // where should we look?

    // objects
    private Scanner_ThreadType
            scannerThread;

    @Override
    public void onStart() {
        // all done
        log(INFO,"Ready to start");
    }

    @Override
    public String getTitle() {
        return roleId;
    }


       public ScannerComponent(Remedium assignedInstance,
            Component assignedFather){
       super(assignedInstance,  assignedFather);
     }

    /**
     * Handle the requests for:
     *  - start
     *  - stop
     *  - pause
     *  - resume
     *  - change the scanning throttle
     * @param Message
     */
    public synchronized void digest_scan(Properties message){
        log(ROUTINE, "Scan request was made");

        if(message.containsKey(THROTTLE))
                updateThrottle(message);

        if(message.containsKey(FIELD_DIR))
                setWhere(message.getProperty(FIELD_DIR));

        if(message.containsKey(FIELD_DEPTH))
                depth = Integer.parseInt(message.getProperty(FIELD_DEPTH));

        if(message.containsKey(SCAN))
                updateScanner(message);
    }

    /** change the throttle value to save cpu cycles, force scanning
     * to go slower. This is particularly relevant on laptop machines
     */
    private synchronized void updateThrottle(Properties msg){
        int newStatus = Integer.parseInt(msg.getProperty(THROTTLE));
        this.throttle = newStatus;
        log(ROUTINE, "Updating the throttle value to "+newStatus);
    }

    /**
     * Launch the operations related to each scanner status change, accepted
     * values for the SCAN parameter are:
     * - START
     * - STOPPED
     * - PAUSED
     * - RESUME
     */
    private void updateScanner(Properties msg){
        
        int newStatus = Integer.parseInt(msg.getProperty(SCAN));

        log(ROUTINE, "Updating the scanner status");

        switch (newStatus) {
            case START: doScan(msg);  break;
            case STOPPED: setOperationalStatus(STOPPED); break;
            case PAUSED:  setOperationalStatus(PAUSED);  break;
            case RESUME:  setOperationalStatus(RUNNING); break;
        }
    }


    /** public version of setStatus with a roleLock key */
    public void setOperationalStatus(long lock, int newStatus) {
        if(lock != this.compLock) return;
        setOperationalStatus(newStatus);
    }
    /** get the status of our role, protected with a key */
    public synchronized int getStatus(long lock) {
        if(lock == this.compLock) return getOperationalStatus();
        else return -1;
    }

    /** set the folder and respective subfolders that will be scanned */
    private synchronized Boolean setWhere(String newWhere) {
        // safety filtering
        //String temp = utils.text.safeString(where);
        if(utils.text.isEmpty(newWhere)) return false;
        log(INFO,"Setting the scan folder to '"+newWhere+"'");
        where = newWhere;
        return true;
    }
    /** get which folder we want to scan */
    public synchronized String getWhere(long lock) {
        if(lock == this.compLock) return where;
        else return "";
    }
    /** throttle is intentional delay added on scanning process to save CPU */
    public synchronized int getThrottle(long lock) {
        if(lock == this.compLock) return throttle;
        else return -1;
    }
    /** How deep in subfolder levels are we allowed to go?  */
    public synchronized int getDepth(long lock) {
        if(lock == this.compLock) return depth;
        else return -1;
    }

    /** This method ensures that we only start the scan when the operational
     * conditions allow to do so*/
    private void doScan(Properties msg){
        // preflight checks
//        if(getOperationalStatus() != STOPPED){
//            log(ERROR, "Can't start the scanner thread while it is still active");
//            return;
//        }

        setOperationalStatus(RUNNING);

        scannerThread = new Scanner_ThreadType(compLock, this);
        scannerThread.start();
    }

    @Override
    public void onRecover() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String doWebResponse(Request request, Response response) {
       return getTitle();
    }


}