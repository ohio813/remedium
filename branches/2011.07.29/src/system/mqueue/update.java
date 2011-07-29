package system.mqueue;

import utils.internet;

/**
 *
 * @author Nuno Brito
 */
public class update {

    static update action = new update();

    Boolean debug = true;
    String beacon_data; // hold the raw retrieved by the beacon page

    String
         ID = "remedium", // distinguish this app from others on the same page
         version = "latest"; // get a specific version if multiple are available

    // define where we can grab informations about the available updates
    String[] beacons = {
        "http://localhost/reboot/pad_remedium.xml",
        "http://remedium.me/latest",
    };


    // useful for extending the update functionality to other applications
    protected void setBeacon(String[] proposed_beacons){
        this.beacons = proposed_beacons;
    }
    // useful for extending the update functionality to other applications
    protected void setDebug(Boolean state){
        this.debug = state;
    }
    // useful for extending the update functionality to other applications
    protected void setID(String setting){
        this.ID = setting;
    }
    // useful for extending the update functionality to other applications
    protected void setVersion(String setting){
        this.version = setting;
    }


    
    /**
     * We will now pick on a row of data an attempt to extract the
     * data relevant to the latest update for our server.
     * @param rawdata
     * @return True if we consider this data as valid
     */
    Boolean processData(String rawdata){
        // first trial, remove any empty attempts
        if(rawdata.equalsIgnoreCase(""))
            return false;

/*  How do we like our data containers to look like?

 * Support spaces on application names
 * Support multiple versions
 * Support multiple applications on the same page
 * Support various checksums (MD5 and SHA1 to start)
 * Be simple to understand as plain text
 * Look presentable enough to place as clear text in html
 * Cannot be confused with other non-related texts
 * Allow mirrors to be specified (no restrictions about how many)
 * Text about log of changes
 * Text about the program introduction
 * Option to visit homepage
 * Multi language?
 *
 * I guess we'll adopt PAD XML format
 * We can use other tools to manage them and this a good thing
 * I can also code a bbcode handler to present them as intended.
 * We agree to only use PAD XML files with some optional extensions
 * such as
 *  -- Checksum support (MD5, SHA1 and others)
 *  -- Zip compression (.zip, 7zip, rar whatever
 *  -- 9
--[remedium]-- (identification tag)
version=latest


*/
        // check for tags related to our identifiers.

    return true;
    }

    /**
     * Our updates can be listed on several locations that we define
     * as "beacons".
     * @return
     */
    Boolean getBeacon(){

        if(!internet.isInternetReachable()){
            debug("Internet connectivity is not available");
            return false;
        };

         for (String beacon : beacons){
            debug("Fetching beacon data from "+beacon);
            String data = internet.getTextFile(beacon);
            // if we like the content of this data, we can stop here
            // otherwise iterate to the next beacon until we like one or
            // conclude this operation as failed if no data is retrieved
            if(processData(data))
                return true;
        }
        debug("No available beacons hold valid data");
        return false;
    }





 private void log(String gender, String message){
                  System.out.println
                          ("update ["+gender+"] "+message);
                    }
 static private void debug(String message){
                  if(action.debug)
                      action.log("debug",message);}
}// end show
