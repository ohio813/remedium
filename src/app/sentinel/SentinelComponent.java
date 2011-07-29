/*
 * This is the Sentinel application. From here we can launch the other
 * components required to make this service function
 */

package app.sentinel;

import system.core.Component;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.html.Button;
import system.html.Meta;
import system.html.Section;
import system.html.Table;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 8th of April 2011 in Darmstadt, Germany
 */
public class SentinelComponent extends Component implements msg{

    // definitions
    private int
            folder_depth = 5, // how many subfolders should be crawled?
            currentAction = STOPPED;

    private Section
             archive = new Section();

    private String
            //targetFolder = "c:\\Program Files (x86)\\DivX\\DivX Plus Player", // where should we look?
            //targetFolder = utils.files.getRootFolder(), // where should we look?
            //targetFolder = "d:\\java;d:\\test", // where should we look?
            targetFolder = utils.files.getRootFolders(), // where should we look?
            SectionSettings = "Settings",
            SectionSettingsTag = "settings";

    private String
           // define the location of our Triumvir
            whoIsOurTriumvir = "";

    // declare our children
    IndexerComponent indexer;
    ScannerComponent scanner;
    TriumvirComponent triumvirComponent;
    USBComponent usb;


    public SentinelComponent(Remedium assignedInstance){
       super(assignedInstance);
     }


    

    @Override
    public void onStart() {
        // call the components dependent of Sentinel
        setupComponents();
        // make all page sections available
        setupSections();
    }

    /** Ensure that our page runs automatically when doing some operation */
    private void addAutoHTMLrefresh() {
        // set this page to update automatically
        Meta loopMeta = new Meta();
        loopMeta.add("http-equiv", "refresh");
        loopMeta.add("content", "4;url="
                +
                this.getCanonicalName()
                );
        //loopMeta.add("url", getWebAddress());
        html.setMeta(loopMeta.getText());
    }

    /** start up the components associated with Sentinel */
    private void setupComponents() {
        // find files on disk and propose them to the Indexer
        scanner = new ScannerComponent(getInstance(), this);
        children.add(scanner);
        // verifies if a file should be added to the archive
        indexer = new IndexerComponent(getInstance(), this);
        children.add(indexer);


        // initializes our triumvir
        triumvirComponent = new TriumvirComponent(getInstance(),
                this, indexer);

        children.add(triumvirComponent);

        // USB tracking
        usb = new USBComponent(getInstance(), this);
        children.add(usb);
        
    }

    /** Make available the main web page sections */
    private void setupSections() {
        // write the "about" page
        html.setSection(
                html.SectionAbout,
                Sentinel_About.getAboutText()
               );

        // make available the Archive web tab
        archive.setTitleLink(SectionSettingsTag);
        archive.setTitlePretty(this.SectionSettings);
        archive.setVisibleNav(Boolean.TRUE);
        archive.setContent(Sentinel_About.getAboutText());
        // add the archive section to our web page
        html.addSection(archive);
    }

    @Override
    public void onRecover() {
    }

    @Override
    public void onLoop() {
          getUpdatesFromTriumvir();
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return sentinel;
    }


    /** output a message to start the scanner */
    private void startScanner(){
            log(INFO, "Starting the Sentinel scanner");
            Properties message = new Properties();
            // the fields that we need to place here
            message.setProperty(FIELD_FROM, sentinel);
            message.setProperty(FIELD_TO, sentinel_scanner);
            message.setProperty(FIELD_TASK, "scan");
            message.setProperty(FIELD_DIR, targetFolder);
            //message.setProperty(FIELD_DIR, ".");
            message.setProperty(FIELD_DEPTH,  ""+folder_depth);
            message.setProperty(SCAN, ""+ START);
            // dispatch the message
            send(message);
    }


    /** Display the files that were archived by our system */
    private String displayArchivedFiles(){
        // get the status from the Indexer
        String result = indexer.getStatus();
        // output the text
        return result;
    }


    @Override
    public String doWebResponse(Request request, Response response) {
        // get the page that we want to display right now
        String show = utils.internet.getHTMLparameter(request, "show");

         // only add the HTML refresh to the main page
//            if(utils.text.isEmpty(show)
//            || show.equalsIgnoreCase("home")
//            )
//                addAutoHTMLrefresh();
//            else
//                html.setMeta("");
            


        if(show.equalsIgnoreCase(SectionSettings)){
            html.setMeta("");
            this.html.setSection(SectionSettingsTag, "No settings yet available"
                    + " at this point, sorry.");
            log(INFO,"Showing the settings section");
            return "";
        }


        // get the action parameter (if available)
        String action = utils.internet.getHTMLparameter(request,"action");

        // if the action says "start" then proceed with a disk scan
        if(action.equalsIgnoreCase("start")){
            requestChangeStatus(sentinel_scanner,RUNNING);
            requestChangeStatus(sentinel_indexer,RUNNING);
            // we are now running
            currentAction = RUNNING;
            startScanner();
        }

        // if the action says "stop" then go for it.
        if(action.equalsIgnoreCase("stop")){
            requestChangeStatus(sentinel_scanner,STOPPED);
            requestChangeStatus(sentinel_indexer,STOPPED);
            currentAction = STOPPED;
        }

        // create a button for the stop request (scan, pause, resume)
        Button actionButton = new Button("Start scan",
                this.getCanonicalName()+"?action=start");

        // stop button
        Button stopButton = new Button("Stop scan",
                this.getCanonicalName()+"?action=stop");

        if(currentAction == STOPPED){
            stopButton.setDISABLED(true);
            actionButton.setDISABLED(false);
            // stop the auto refresh tag
            html.setMeta("");
        }
        else {
            addAutoHTMLrefresh();
            stopButton.setDISABLED(false);
            actionButton.setDISABLED(true);
        }

        //System.out.println(loopMeta.getText());

        Table tableControls = new Table();
        tableControls.addLine(actionButton.getText(), stopButton.getText());

        // output our result
        String result =
                 "This tool will scan all files inside this machine and index"
                + " them inside the knowledge containers."
                + html.br
                + "To know more about Sentinel, please visit the "
                + html.doLink("About", "?show=about") + " page."
                + html.br
                + html.br
                + tableControls.getText()
                + html.br
                + displayArchivedFiles()
                ;

        // add this text to our home page
        html.setSection(html.SectionHome, result);

        return result;
    }

    /** Get knowledge update from our triumvir.*/
    private void getUpdatesFromTriumvir(){
        
    }

   

    /** Update the information about our triumvir */
    public void digest_updateTriumvirLocation(Properties message) {
        whoIsOurTriumvir = message.getProperty(FIELD_MESSAGE);
        log(DEBUG,"Using as triumvir '" + whoIsOurTriumvir +"'");
    }



    /** Signal this component that the indexing operation has finished */
    public void digest_stop(Properties message) {
            currentAction = STOPPED;
    }


    }


