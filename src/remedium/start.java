package remedium;
/*
 * This is the main class that is launched from command line. The purpose
 * of this class is to initiate the main instance and also launch a particular
 * application if necessary
 */

import java.util.Properties;
import system.msg;

public class start implements msg{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        /** start all systems
         */
        Remedium main = new Remedium();
        Properties parameters = new Properties();

//        parameters.setProperty(LOCK, ""+unlock);
        parameters.setProperty(LISTEN, ""); // ask to LISTEN requests
        parameters.setProperty(DELETE, "");
        parameters.setProperty(FORCE_FINISH, "");
       // parameters.setProperty(FIELD_PORT, PORT_A);

        // which apps do we want to launch?
        //parameters.setProperty("apps", centrum+";"+triumvir+";"+sentinel);

      //  System.out.println("----->"+utils.files.getRootFolders());

        // start the instance with our parameters
        main.start(parameters);

        // wait for the indexer to be started (heaviest component)
        while(main.logContains(sentinel_indexer, "Ready to start")==false)
        utils.time.wait(1);

        // start the process Manager as default application
        String defaultURL = "http://localhost:10101/sentinel";
        utils.internet.openURL(defaultURL);

        // while remedium is intended to be running, wait a few seconds
        while(main.isRunning()){
            utils.time.wait(2);
        }

        // call the stop of our running system
        main.stop();
    }

}
