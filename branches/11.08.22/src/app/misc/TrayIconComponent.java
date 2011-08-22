/*
 * This component implements the trayicon functionality. This is only
 * supported on machines running Java version 6
 */

package app.misc;

import system.core.Component;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.mqueue.msg;

/**
 * Handy commands to keep for future reference;

 *  trayIcon.setImage(updatedImage);

 *  trayIcon.setTooltip("I'm busy. Go away.");

 *  trayIcon.setImageAutoSize(true);

 *  trayIcon.displayMessage("Finished downloading",
           "Your Java application has finished downloading",
           TrayIcon.MessageType.INFO);
 *
TrayIcon.MessageType.ERROR	  An error message
TrayIcon.MessageType.INFO	  An information message
TrayIcon.MessageType.NONE	  A simple message
TrayIcon.MessageType.WARNING	  A warning message
 *
 */


/**
 *
 * @author Nuno Brito, 18th of April 2011 in Darmstadt, Germany.
 */
public class TrayIconComponent extends Component {


    // settings
    private String icon = "shield.png";


    // objects
    private TrayIcon trayIcon;
    private SystemTray tray;

   private String
           pageDefault = "",
           pageCurrent = ""
           //pagePrevious = ""
           ;


    /** the public constructor */
    public TrayIconComponent(Remedium assignedInstance,
            Component assignedFather){
        super(assignedInstance, assignedFather);
   }



    /** Show our tray icon */
    public void showTrayIcon(){


        // define the default landing page
        pageDefault = getInstance().getMyAddress();
        pageCurrent = pageDefault; // initial setting

        

if (SystemTray.isSupported()) {

    tray = SystemTray.getSystemTray();

    URL imgURL = this.getClass().getResource(icon);
    Image image = Toolkit.getDefaultToolkit().getImage(imgURL);


    //Image image = Toolkit.getDefaultToolkit().getImage(icon);

    MouseListener mouseListener = new MouseListener() {

        @Override
        public void mouseClicked(MouseEvent e) {
             if(e.getButton() == 1){ // means left click
                 String page = getClickPage();
                     log(msg.INFO,"Opening the browser window at " + page);
                     utils.internet.openURL(page);

            }
     }

                @Override
        public void mouseEntered(MouseEvent e) {
            //System.out.println("Tray Icon - Mouse entered!");
        }

                @Override
        public void mouseExited(MouseEvent e) {
            //System.out.println("Tray Icon - Mouse exited!");
        }

                @Override
        public void mousePressed(MouseEvent e) {
            //System.out.println("Tray Icon - Mouse pressed!");
        }

                @Override
        public void mouseReleased(MouseEvent e) {
           // System.out.println("Tray Icon - Mouse released!");
        }
    };

    ActionListener exitListener = new ActionListener() {
            @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Exiting...");
            getInstance().stop();
            //System.exit(0);
        }
    };


    ActionListener browserListener = new ActionListener() {
            @Override
        public void actionPerformed(ActionEvent e) {
            log(msg.INFO,"Opening the browser window");
            utils.internet.openURL(getInstance().getMyAddress());
        }
    };

    PopupMenu popup = new PopupMenu();
    MenuItem defaultItem = new MenuItem("Exit");
    defaultItem.addActionListener(exitListener);

  
    MenuItem controlItem = new MenuItem("Control panel");
    controlItem.addActionListener(browserListener);


    popup.add(controlItem);
    popup.add(defaultItem);

    trayIcon = new TrayIcon(image, "Remedium", popup);

   

    ActionListener actionListener = new ActionListener() {
                @Override
        public void actionPerformed(ActionEvent e) {
         log(msg.INFO,"Opening the browser window");
            utils.internet.openURL(getInstance().getMyAddress());
        }
    };

    trayIcon.setImageAutoSize(true);
    trayIcon.addActionListener(actionListener);
    trayIcon.addMouseListener(mouseListener);



    try {
        tray.add(trayIcon);
    } catch (AWTException e) {
        System.err.println("TrayIcon could not be added.");
    }

} else {

    //  System Tray is not supported

}

    }

    @Override
    public void onStart() {
        showTrayIcon();
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return "trayicon";
    }


    /** Output a notification on the System tray */
    private void doNotification(String message, Request request){

        // get the content to be displayed
        String decodedMessage = utils.text.quickDecode(message);

        // show the text
         trayIcon.displayMessage("",
                decodedMessage,
                TrayIcon.MessageType.INFO);
    }
    
       /** Update the web page that is open when someone clicks the icon */
    private void doUpdate(String message, Request request){

        // get the content to be displayed
        String decodedMessage = utils.text.quickDecode(message);
        // update the default page
        setClickPage(decodedMessage);

        
    }



    /** Hide the tray icon */
    private void doTrayAction(String action, Request request){

        // if it is empty then ignore this value
        if (action.equalsIgnoreCase("hide")){
            log(msg.INFO,"Hiding the tray icon");
            // hide the tray icon for good
            tray.remove(trayIcon);
        }

        // all done
        }


    @Override
    public String doWebResponse(Request request, Response response) {

        // handle action requests
        String action = utils.internet.getHTMLparameter(request, msg.ACTION);
        if(action.length() > 0)
            doTrayAction(action, request);

        // handle notification requests
        String notification = utils.internet.getHTMLparameter(request,
                "notification");
        if(notification.length() > 0)
            doNotification(notification, request);


        // handle update requests
        String update = utils.internet.getHTMLparameter(request,
                "update");
        if(update.length() > 0)
            doUpdate(update, request);




        return "";
    }

    /** Return the page that should be open when user clicks on tray icon */
    private String getClickPage(){
        return pageCurrent;
    }

    /** Update the page that is open when user clicks on icon*/
    private void setClickPage(String newPage){
        // if we define "default", then return back to our default page
        if(newPage.equalsIgnoreCase("default"))
            newPage = this.pageDefault;
        
        //pagePrevious = pageCurrent;
        pageCurrent = newPage;
    }


}
