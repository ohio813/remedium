/*
 * This class provides some handy tweaks to remedium instances.
 */

package utils;

import java.io.File;
import java.util.Properties;
import remedium.Remedium;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 29th of May 2011 in Darmstadt, Germany.
 */
public class tweaks {

      /** Remove the system tray icon from public sight*/
      public static void removeTrayIcon(String URL){
          String address = "http://"+URL+"/"+msg.trayicon+"?action=hide";
          utils.internet.getTextFile(address);
      }

      /** Send out a notification */
      public static void notification(String URL, String message){
          // some dumb people (me for example) use this with and without http
          String cleanURL = URL.replace("http://", "");

//                    encodedText = utils.Base64.encodeBytes(message.getBytes(),
//                   Base64.GZIP | Base64.DO_BREAK_LINES | Base64.URL_SAFE
//                    );

          String address = "http://"+cleanURL+"/"+msg.trayicon+"?notification="
                  + utils.text.quickEncode(message);

          //System.out.println(address);
          utils.internet.getTextFile(address);
      }

      /** Send out a notification */
      public static void updateTrayIconAction(String URL, String message){
          // some dumb people (me for example) use this with and without http
          String cleanURL = URL.replace("http://", "");
          // create the correct URL
          String address = "http://"+cleanURL+"/"+msg.trayicon+"?update="
                  + utils.text.quickEncode(message);
          // send out the message
          //System.out.println(address);
          utils.internet.getTextFile(address);
      }



//      /** */
//      public static void changeTrayIcon(Component component){
//        Properties message = new Properties();
//
//        // set the appropriate fields
//        message.setProperty(FIELD_FROM, component.getCanonicalName());
//        message.setProperty(FIELD_TO, trayicon);
//        message.setProperty(FIELD_TASK, "changeIcon");
//        message.setProperty("newIcon", "shield01.png");
//        // send out our message
//        component.sendDebug(message);
//      }


       /** Delete the database folder for our testings */
      public static void deleteDBFolder(String port){
        String toDelete = "storage"
                + File.separator+ port;

        System.out.println("Deleting folder '" + toDelete + "'");

        File file = new File(toDelete);
        utils.files.deleteDir(file);
    }

      /**
       * Create a database filled with data about processed files.
       * This method is handy for automating test cases that require data to be
       * present.
       * @param portNumber The port inside the storage folder to be used
       */
      public static void generateIndexedData(Remedium instance,
              String where, int Seconds){


        // start the scanning process
        Properties message = new Properties();

      // the fields that we need to place here
        message.setProperty(msg.FIELD_FROM, msg.sentinel_gui);
        message.setProperty(msg.FIELD_TO, msg.sentinel_scanner);
        message.setProperty(msg.FIELD_TASK, "scan");
        message.setProperty(msg.FIELD_DIR, where);
        message.setProperty(msg.FIELD_DEPTH,  "5");

   // After waiting, we need to ensure that we have started the scanning
        System.out.println("Starting a base line scan");
        message.setProperty(msg.SCAN, "" + msg.START);
      // send it away to the MQ
        instance.getMQ().send(message);

        // wait some time
        utils.time.wait(Seconds);
        // stop the fun
        message.setProperty(msg.SCAN, "" + msg.STOPPED);
        instance.getMQ().send(message);
        // wait a few seconds to finish processing the last bits
        System.out.println("Stopped scanning, we should have some data now.");
        utils.time.wait(3);
      }

  /**
   * This method will request one component at a given location to engage into
   * a synchronization with its counterpart component at some other location.
   *
   * @param from The instance with the data we want to fetch
   * @param to  The instance that we want to update
   * @param what What is the component address (e.g.: sentinel/indexer)
   * @param since Gather since when?
   * @param until Gather data until?
   * @return The result from this operation: "Synchronization complete"
   */
  public static String batchSynchronize(String from, String to,
          String what, long since, long until){

       // we remotely ask an instance to synchronize with another
        String request =
             "http://" + to
             + "/"
             + what
             + "?box=crc32"
             + "&action=remotesync" // call remote sync, sync is reserved
             + "&who=" + from
             + "&since=" + since
             + "&until=" + until
             ;

        // get the record data
        String result = utils.internet.getTextFile(request);

  return result;
  }


}
