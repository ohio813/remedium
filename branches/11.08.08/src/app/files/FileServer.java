/*
 * This component allows for this instance to serve files to any given
 * web requests.
 *
 * It is divided in three major sections with the following characteristics:
 *  - File listing
 *      - Update breadcrumb
 *      - Ensure that folders appear before files
 *      - List statistics
 *  - File downloading
 *      - Track downloads
 *          - by whom, when and which IP address
 *      - Set permission tags
 *      - Set active or inactive permission to download
 *      - Set password access to folders or files
 *      - Allow only n downloads per minute
 *  - File upload
 *      - Compress files
 *
 *
 * In the future we might add features such as:
 *      - password protection
 *      - download tracking
 *      - upload files
 *      - and so forth
 */

package app.files;

import utils.MimeIndexer;
import app.user.User;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.core.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Form;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.container.Container;
import system.log.LogMessage;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 8th of April 2011 in Darmstadt, Germany
 */
public class FileServer extends Component{

  // settings

    private boolean
            requestLogin = false; // ask for login or not?
    
    private String
            httpdocs = "./httpdocs";  // where our files are hosted

  // objects
    private File // the file pointer
            http = new File(httpdocs);
    
    private LicenseComponent
            license;
    
    // cache to our files
    private HashMap<String, byte[]>
            fileCache = new HashMap<String, byte[]>();
    
    private Request currentRequest;

    private static final String // common parameters used in URL addresses
            getUID = "uid",
            getFolder = "dir",
            doSave = "save",
            doEdit = "edit";

    private Container container; // where we store data

    private static final String // definitions of our fields
            id = "file",
            uid = "uid",
            name = "name",
            path = "path",
            size = "size",
            permissions = "permissions",
            parameters = "parameters",
            downloads = "downloads";

    private final String[] fields = new String[] // fields for our database
        {
        uid, // unique identification
        name, // file name
        path, // file path
        size,  // file size
        permissions, // admin,guest,EGOS_license
        parameters, // password=123, expires=2011/10/10:10h12 (...)
        downloads // how many times a given file was downloaded
    };
    
        public static final int // define the index of each field inside our database
            _uid = 0,
            _name = 1,
            _path = 2,
            _size = 3,
            _permissions = 4,
            _parameters = 5,
            _downloads = 6
            ;
   

  public FileServer(Remedium assignedInstance, 
          LicenseComponent assignedLicense){
       super(assignedInstance);
      // get our assigned license server
       license = assignedLicense;
     }

    @Override
    public void onStart() {
        // create our httpdocs folder if it doesn't exist
        utils.files.mkdirs(httpdocs);
        // the reply object
        LogMessage result = new LogMessage();
        // create the database container
        container = new Container(id, fields, this.getStorage(), result);
        // Output the result from this initialization if something went wrong
        if(result.getResult() == msg.REFUSED){
            this.log(msg.ERROR, result.getRecent());
        }
        // set time between loops to update the files found on our HTTPDOCS
        this.setTime(120);
        
        // scan all files inside our httpdocs folder
        scanFolder();
        // clean database entries that are no longer found on our disk
        cleanDB();
    }

    
    /** Cleans up our database from redundant entries*/
    private void cleanDB(){
        // read all entries inside our database
        ArrayList<String[]> entries = container.readAll();
        // iterate each one of them
        for(String[] record : entries){
            File file = new File(record[_path] + "/" + record[_name]);
            // remove the entry if we failed to open it as a file
            if((file == null)||(file.exists() == false)){
                container.delete(uid, record[_uid]);
            }
        }
        
    }
    
    /**
     * Scan the HTTPdocs folder and index changes on the structure.
     */
    private void scanFolder(){
        // Get all files and folders up to the 25th level of subfolders
        ArrayList<File> result = utils.files.findAll(http, 25);
        // add our root folder as well
        result.add(http);
        // Iterate each found file and folder, add them up to our database
        for(File file : result){
            // If not added already, get it inside our database
            processFile(file);
        }
    }

    /**
     * Process each file.
     * Add the file to our database if it is not listed already
     */
    private void processFile(File file){
        // get the relative path
        String output = file.getPath();
        // convert the path to UNIX style (necessary under Windows)
        output = output.replace("\\", "/");
        // calculate our unique identification for this file
        String newUID = app.sentinel.ScannerChecksum.generateStringSHA256(output);
        // check if we have already indexed this value or not
        String[] read = container.read(newUID);
        // if we don't have this file on the database (result = 1), index it
        if(read == null){

         // define the initial permissions by default
         String permission = "";
         // define the parameter switch
         String parameter = ""; 
            if(file.isDirectory() == true){
                parameter = "type=dir,"; // set this object as a directory
            }
            else
                parameter = "type=file,"; // set this object as a file
          
            // default permissions
            permission = "public";
          

            // create our new record to be added inside the database
            String[] newRecord = new String[]{
                newUID, // newUID
                file.getName(), // name
                file.getParent().replace("\\", "/"), // folder where is placed
                ""+file.length(), // file size
                permission, // permissions: admin,guest,EGOS_license
                parameter, // password=123, expires=2011/10/10:10h12 (...)
                "0" // number of downloads
            };
            try{
            // write this record onto our database
            boolean result = container.write(newRecord);
            // something went wrong
            if(result==false)
                log(msg.ERROR,container.getLog().getRecent());
            }catch (Exception e){
                // output the error message when something went wrong
                this.log(msg.ERROR, "processFile failed while processing "
                        + "'" + file.getPath() + "'"
                        + " with error " + e.toString()
                        );
            }
        }
    }

    @Override
    public void onLoop() {
        // clean database entries that are no longer found on our disk
        cleanDB();
    }

    @Override
    public void onStop() {
    }

    @Override
    public String getTitle() {
        return id;
    }


    @Override
    public String doWebResponse(Request request, Response response) {

                
   // preflight checks - are we logged?
      if(requestLogin)
        if(this.isLogged(request)==false){
            html.setSection(html.SectionHome, "<h2>Login required</h2>"
                    + "Redirecting you to the login page.."
                    );
            // go to the login page and then back to us when logged
            addAutoHTMLrefresh(3, "/user?redirect="+this.getWebAddress());
            return "";
        }
      
      // set the current request instance to be used by other methods
      this.currentRequest = request;
      // output page container
      String result = "";

      
   // File / folder edit
      // do we want to edit anything?
        String edit = utils.internet.getHTMLparameter(request, doEdit);
        // edit a file or folder if requested
        if(utils.text.isEmpty(edit)==false){
           // make the file available for download
           result = doEdit(edit);
           // output our final result to the user
           html.setSection(html.SectionHome, result);
           return "";
        }
      // do we want to save something?
        String save = utils.internet.getHTMLparameter(request, doSave);
        // edit a file or folder if requested
        if(utils.text.isEmpty(save)==false){
           // make the file available for download
           result = doSave(request, save);
           // output our final result to the user
           html.setSection(html.SectionHome, result);
           return "";
        }
        
      
      
   // File Download
        // are we requesting a file?
        String download = utils.internet.getHTMLparameter(request, getUID);
        // yes, we have a request, process it
        if(utils.text.isEmpty(download)==false){
           // make the file available for download
           result = giveFileMain(request, response, download);
            // return "ignore" to signal that no further action is required.
            return result;
        }

  // File Listing
        // do we have any preferences about the folder?
        String where = utils.internet.getHTMLparameter(request, getFolder);
        // do the file listing
        result = listingMain(where, request);
        // output our final result to the user
        return "";
    }

    
    /** Edit a specific file or folder */
    private String doSave(Request request, String what){
        // where the result will be held
        String result = "";
        // get the details from the intended file
        String[] record = container.read(what);
        // have we really found a file?
        if(record == null){
            log(msg.ERROR,"Save failed, ID not found: " + what);
            result = "<h2>Invalid ID</h2>"
                    + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return result;
        }
        // get the data from the form
        
        // the form holder
        Form rem = null;
        try {
            // get the form
          rem = request.getForm();
        } catch (IOException ex) {
            // an error occurred, exit this method
            log(msg.ERROR,"doSave failed: Exception occured");
            result  = "<h2>Save failed</h2>"
                    + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return result;
        }

        // get the user details from the form
         String remPermissions = rem.get(permissions);
         String remDownloads = rem.get(downloads);
        
         // Clean up remPermissions
         remPermissions = remPermissions.replace(",", "");
         remPermissions = remPermissions.replace(" ", "");
         remPermissions = remPermissions.replace("\r", "");
         // convert from lines onto items
         remPermissions = remPermissions.replace("\n", ",");
         
         // change the record values
         record[_permissions] = remPermissions;
         record[_downloads] = remDownloads;
        
         // save these changes back on disk
         container.write(record);
        
         result  = "<h2>Edit completed</h2>"
                    + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
         
        // all done.
        return result;
    }
    
    
    /** Edit a specific file or folder */
    private String doEdit(String what){
        // where the result will be held
        String result = "";
        // get the details from the intended file
        String[] read = container.read(what);
        // have we really found a file?
        if(read == null){
            log(msg.ERROR,"Edit failed, ID not found: " + what);
            result = "<h2>Invalid ID</h2>"
                    + "Redirecting back to main page..";
            //this.html.setSection(html.SectionHome, result);
            // redirect to the login page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return result;
        }
        // read the permissions and convert it to HTML readable format
        String permission = read[_permissions];
        permission = permission.replace(",", "\n"); 

        // output the user interface            
               result =
                "<h2>Editing '"+read[_name]+"'</h2>"
                +"<form method=\"post\""
                +" action=\"?save="+what //+" name=\"edit=\"\"
                       + "\">"
                +"  <table style=\"text-align: left; width: 368px; height: "
                       + "63px;\""
                +" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">"
                +"    <tbody>"
                +"      <tr>"
                +"        <td"
                +" style=\"vertical-align: top;  width: 80px;\">"
                       + "Permissions<br>(one per line)</td>"
                +"        <td style=\"width: 268px;\" align=\"undefined\""
                +" valign=\"undefined\"><textarea wrap=\"off\" rows=\"8\""
                +" cols=\"30\" name=\"permissions\">" + permission
                + "</textarea></td>"
                +"      </tr>"
                +"      <tr>"
                +"        <td style=\"width: 80px;\">"
                       + "Downloads</td>"
                +"        <td style=\"width: 268px;\" align=\"undefined\""
                +" valign=\"undefined\"><input"
                +" name=\"downloads\" size=\"4\" value=\""+read[_downloads]
                       +"\"></td>"
                +"      </tr>"
                +"    </tbody>"
                +"  </table>"
                +"  <br>"
                +"  <input"
                       + " value=\"Save\""
                +" type=\"submit\"></form>";
        
        return result;
    }
    

    /** Verifies if the current user is allowed to view a folder or not */
    private boolean isAllowedToSeeFolder(String where, Request request){
        boolean result = false;
        // properly edit the path components
      try{
        String targetName = where.substring(where.lastIndexOf("/")+1);
        String targetParent = httpdocs +
                where.substring(0, where.lastIndexOf("/"));

        // handle the special case of the root folder
        if(where.equals("/")){
            targetParent = ".";
            targetName = httpdocs.substring(httpdocs.indexOf("/")+1);
        }
        
            // read from our container all the matching entries
            ArrayList<String[]> records = container.readNew(path, targetParent);
            // iterate each one of them if not empty
            if(records.size() > 0)
                for(String[] record : records){
                    // get the name of the folder
                    String recordName = record[_name];
                    // if it is not our target folder, don't process
                    if(recordName.equals(targetName)==false)
                        continue;
                    // get the permissions for this folder
                    String targetPermission = record[_permissions];
                    // get the user details
                    User user = this.getLoggedUser(request);
                    // is our user authorized to access this folder?
                    result = user.hasPermission(targetPermission);
                    // break here
                    return result;
                }
        // handle exception cases
        }catch (Exception e){
            log(msg.ERROR,"Operation isAllowedToSeeFolder failed to process '"
                    + where +"' with reason: " + e.toString() );
        }
        
        // is the user currently logged authorized to see this folder?
        return result;
    }

    /** Provide a listing of files and directories inside a given path */
    private String listingMain(String where, Request request){
        // initialization
        String
            target = "",
            display = "";
        // if nothing is specified, use the root folder as default
        if(utils.text.isEmpty(where)){
            target = httpdocs; // path to the folder on our disk
            display = "/"; // text displayed to user
        }
        else{ // a folder was specified, handle it properly
            if(where.contains(".."))
                where = ""; // security measures, avoid ".."
            target = httpdocs + File.separatorChar + where;
            display = File.separatorChar + where;
        }

        // present a UNIX style display
        String cleanDisplay = display.replace("\\", "/");

        // are we allowed to see this folder or not?
        if(isAllowedToSeeFolder(cleanDisplay, request)==false){
            log(msg.REFUSED, "Access not authorized of '" + cleanDisplay +"'"
                     + " to '" + this.getLoggedUser(request).getMyName()+"'");
            return "";
        }else
             log(msg.ACCEPTED, "Access authorized of '" + cleanDisplay +"'"
                     + " to '" + this.getLoggedUser(request).getMyName()+"'");

        // prepare filtered text
        String displayDownloads = "";
        String displayPermissions = "";
        // only display the download and permissions tab if we are admins
        if(this.getLoggedUser(request).hasPermission(User.admin)){
            // change the values to become visible for the user
            displayDownloads = "&nbsp&nbspDownloads";
            displayPermissions = "&nbsp&nbspPermissions";
        }        

        // display the header of our page
        String result =
                "<H2>Index of "+cleanDisplay+"</H2>\n"
                + "<HR>"
                + "<TABLE border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"
                + "<TBODY>\n"
                + "<TR>\n"
                + "<TD></TD>"
                + "<TD><B>&nbsp&nbspName</B></TD>"
                + "<TD></TD>"
                + "<TD><B>&nbsp&nbspSize</B></TD>"
                + "<TD><B>"+displayDownloads+"</B></TD>"
                + "<TD><B>"+displayPermissions+"</B></TD>"
                + "</TR>"
                ;

        // display folders in first position
        result += this.listingFolders(target, request);
        // display folders in first position
        result += this.listingFiles(target, request);
        // close down our table
        result += "</TBODY>"
                + "</TABLE>"
                + "</TABLE>";
        // set the output
        this.html.setSection(html.SectionHome, result);
        // all done!
        return result;
    }

    /** Display the folders when requested by the user */
    private String listingFiles(String target, Request request){
        String result = "";
        // convert from Windows slashes to UNIX style
        target = target.replace("\\", "/");
        // get all files belonging to this folder
        ArrayList<String[]> files = container.readNew(path, target);
        // if not empty, iterate all our found files
        if(files.size() > 0)
            for(String[] record : files){
                String parameter = record[_parameters];
                if(parameter.contains("type=dir,"))
                    continue;
                // get the URL
               String textLink = this.getCanonicalName()
                    + "?" + getUID + "=" + record[_uid];
               // convert this text to an HTML link
               textLink = 
                         html.doLink(record[_name], textLink);
               // size of this file
               String textSize = record[_size];
               textSize = "&nbsp" + 
                       utils.files.humanReadableSize
                       (Long.parseLong(textSize)) + "&nbsp";
               // How many times it was downloaded
               String textDownloads = 
                       setupDownloadsText(record, request);
               // what are our file permissions?
               String textPermissions = 
                       setupPermissionsText(record, request);
               // add up this item to our changes
               result = result.concat(
                 this.addItem(
                       utils.MimeIndexer.getIconHTML(record[_name]),
                       textLink,
                       showEditLink(record),
                       textSize,
                       textDownloads,
                       textPermissions
                       ));
            }
        return result;
    }
    
    

    /** If we are admin, show the edit link */
    private String showEditLink(String[] record){
        String result = "";
        // get the current user
        User user = this.getLoggedUser(currentRequest);
        // are we admin?
        if(user.hasPermission(User.admin)){
            result =
                    "&nbsp&nbsp"
                    + html.doLink("edit", 
                    "?"
                    + "edit="+record[_uid])
                    + "&nbsp";
        }
        // output result, add some blank spaces to make it look better
        return result;
    }
    
    
    /** Either shows readonly permissions or allows to edit them if admin */
    private String setupDownloadsText(String[] record, Request request){
        String result = "";
 
        result = record[_downloads];
               // display this value if it isn't empty
               if( (result.equalsIgnoreCase("0")==false)
                       // also, we need to be admin to see it
                       && (this.getLoggedUser(request)
                            .hasPermission(User.admin)))
                   result =
                       "<div style=\"text-align: center;\">"
                       + record[_downloads]
                       + "</div>";
               else // don't display anything for this value
                   result = "";
               // all done
               return result;
    }
    
    /** Either shows readonly permissions or allows to edit them if admin */
    private String setupPermissionsText(String[] record, Request request){
        String result = "";
        // only show details if we are logged with admin permissions
        if(this.getLoggedUser(request).hasPermission(User.admin)){
            // get our permissions and format them for user display
            String permissionText = 
                    record[_permissions].replace(",", ", "); 
            result = permissionText; 
            }
        // output result, add some blank spaces to make it look better
        return "&nbsp" + "&nbsp" + result;
    }
    

    /** Display the folders when requested by the user */
    private String listingFolders(String target, Request request){
         String result = "";
        // convert from Windows slashes to UNIX style
        target = target.replace("\\", "/");
        // get all files belonging to this folder
        ArrayList<String[]> files = container.readNew(path, target);
        // if not empty, iterate all our found files
        if(files.size() > 0)
            for(String[] record : files){
                // get our parameters
                String parameter = record[_parameters];
                // ignore files since we don't like them here
                if(parameter.contains("type=file,"))
                    continue;

             // create the full path
             String cleanPath =
                     record[_path]
                     + "/"
                     + record[_name]
                     ;
             // remove the root folder part from this string
             cleanPath = cleanPath.replace(httpdocs + "/", "");
             
            // do the final URL link
               String textLink = this.getCanonicalName()
                    + "?" + getFolder + "=" + cleanPath;
               // convert this text to an HTML link
               textLink = html.doLink(record[_name],
                       textLink);
               // get the folder size
                  long folderSize = utils.files.getFolderSize
                          (new File(
                                  record[_path]
                                  + "/"
                                  + record[_name]), 25);
               // convert to human readable format     
               String textSize = 
                       "&nbsp"
                       + utils.files.humanReadableSize(folderSize)
                       + "&nbsp";
               
               // set up the permissions tab
               String permission = setupPermissionsText(record, request);
               // add up this item to our changes
               result = result.concat(
                       this.addItem(
                       utils.MimeIndexer.getIconHTML("aaa.folder"),
                       textLink, 
                       showEditLink(record),
                       textSize,
                       "",
                       permission));
           }
        
            // add the previous folder link
            if(target.length() > httpdocs.length()){
                // clean our path
                String dir = target.replace(httpdocs + "/"
                        , "");
                // get the parent
                if(dir.contains("/"))
                   dir = this.getWebAddress() + "?dir=" 
                           + dir.substring(0, dir.lastIndexOf("/"));
                else
                    dir = this.getWebAddress();
                // do the item displayed to the end user
                result = this.addItem(
                       utils.MimeIndexer.getIconHTML("aaa.dirup"),
                       html.doLink("..",dir), 
                       "",
                       "",
                       "",
                       "")
                        + result;
            }
            
        
        return result;
    }

    
 
    
    /** Add a new item to our table */
    private String addItem(String... items){
        String result = "<TR>";
        // iterate each item
        for(String item : items){
            // add each item as a part of the table
            result = result.concat("<TD><TT>");
            result = result.concat(item);
            result = result.concat("</TT></TD>");
        }
        // close down our table
        result = result.concat("</TR>");
        result = result.concat("\n");
        // all done
        return result;
    }



    /** Serve the requested file */
    private String giveFileMain(Request request, Response response,
            String download){
        // does our file really exists?
        String[] record = container.read(download);

        // we didn't found this file
        if(record == null){
            try { // close our stream of data
                response.close();
            } 
            // something wrong happened, stop right here
            catch (IOException ex) {}
            // output an error message to the end user
            String text = "<h2>Not found</h2>"
                        + "Redirecting back to main page..";
            // set our error message text as output
            this.html.setSection(html.SectionHome, text);
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return text;
        }

        // get the file parts
        String filename = record[_name];
        String filepath = record[_path];
        String fullname = filepath + "/" + filename;
        Long counter = Long.parseLong(record[_downloads]);
        String permission = record[_permissions];
        
        // Has this user permission to download this file?
        if(this.getLoggedUser(request).hasPermission(permission)==false){
            log(msg.REFUSED, "Not delivering " + fullname + " to "
                    + request.getClientAddress().getHostName());
            return "";
        }
        // Do we need to make the user sign any licenses?
        String result = "";
        // check the license value of this file
        boolean accepted = licensesAccepted(request, permission);
        // has the user accepted these licenses yet?
        if(accepted == false){
            // licenses were not accepted, let's exit
            return result;
        }
        // all ok so far, proceed with the download
        log(msg.INFO, "Delivering " + fullname + " to "
                    + request.getClientAddress().getHostName());
              
    // increase the counter
    counter++;
    // change the record
    record[_downloads] = "" + counter; 
    // write our modified record onto the database
    container.write(record);
    // provide the requested file to the end user
    File selectedFile = new File(fullname);
    this.giveFileDownload(request, response, selectedFile);
    // all done
    return "ignore";
    }

    /** Ask for licenses when serving a file */
    private boolean licensesAccepted(Request request, String permissions){
        ArrayList<String[]> licenses = license.getAll(); // get all licenses
        String askLicenses = "";
        // iterate each one of the available licenses
        for(String[] thisLicense : licenses){
            // if one of the permissions matches a license Id, add to the list
            if(permissions.contains(thisLicense[_uid])){
                // add the license
                askLicenses = askLicenses.concat(thisLicense[_uid] + ",");
                // clean up our mess
                thisLicense = null;
            }
            // clean up the mess
            thisLicense = null;
        }
        // should we check the licensing of this file?
        if(utils.text.isEmpty(askLicenses)){
            // file has no licenses to check, exit here
            return true;
        }
            // clean up our string (there is an extra comma sign at the end)
            askLicenses = askLicenses.substring(0, askLicenses.length() - 1);
            // output the questions to be asked
            log(msg.INFO,"Confirming acceptance of licenses: " + askLicenses);
            
            // check if the user has accepted each of these licenses
            for(String thisLicense : askLicenses.split(",")){
            // get the cookies
            Cookie cookie = request.getCookie("license_" + thisLicense);
            // were we here before?
            if(cookie==null){ // this license was neither accepted or refused before
                // ask for the license acceptance
            String text = "<h2>This file requires license acceptance</h2>"
                        + "Redirecting to the license page..";
            // set our error message text as output
            this.html.setSection(html.SectionHome, text);
            // redirect to the main page
            addAutoHTMLrefresh(3, "/license" + "?" // where the componen is placed 
                    + LicenseComponent.accept // action  (acceptance of licenses)
                    + "=" + askLicenses // licenses to be asked
                    + "&redirect="  // request to redirect after action
                    + this.getWebAddress() + "?" // indicated the redirection page 
                    + request.getAddress().getQuery()); // get our parameters
            // don't provide the file just yet
            return false;
            }
            }
            
        // all good, licenses accepted
        return true;
    }
    
    
    
    
    
    
     /** Serve the requested file */
    private void giveFileDownload(Request request, Response response,
            File downloadfile ){

        try{
            // byte stream
            OutputStream out = null;
            // set the type of download
            response.setMinor(1);
            out = response.getOutputStream();

//            // where we write our full path to the requested file
            String diskFile = downloadfile.getPath();

            //String type = "text/html; charset=utf-8";
            //byte[] content = null;
            // this adds a caching feature. We might want to disable this later.
            byte[] cached = fileCache.get(diskFile);
            File realFile = new File(diskFile);
            // get the MIME header when available
            response.set("Content-Type", MimeIndexer.getContentType(diskFile));
            // help web browsers know about the size of this file in advance
            response.set("Content-Length", (int) realFile.length());
            // define the file name
            response.set("Content-Disposition", "filename=\""
                    + downloadfile.getName() + "\"");
            // close our connection after serving the file
            response.set("Connection", "close");

            InputStream file = null;
            String ignoreCache = utils.internet.getHTMLparameter(request, "nocache");
            Boolean doIgnore = ignoreCache.equalsIgnoreCase("true");
            if ((cached == null) || (doIgnore == true)) {
                try {
                    file = new FileInputStream(realFile);
                    byte[] chunk = new byte[(int) realFile.length()];
                    int count = 0;
                    int pos = 0;
                    while ((count = file.read(chunk, pos, chunk.length - pos)) > 0) {
                        pos += count;
                    }
                    cached = chunk;
                    fileCache.put(diskFile, cached);
                } finally {
                    // close our input stream
                    file.close();
                }
            }
            out.write(cached);
            // close our response
            response.close();
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /** Get the about text box  */
    private String getAboutText(){
        return "<h2>What is this \"file\" application?</h2>"
    +"This application&nbsp;turns the system into a simple web server. It "
    +"is mostly intended to serve files such as images to the web pages or "
    +"other files that the user decides to share in a public manner.<br>"
    +"<br>"
    +"<h2>Where are these public files located?</h2>"
    +"Under the same folder from where the system is launched, a folder called "
    +"\"httpdocs\" is made available. All files inside this folder will be "
    +"publicly available.<br>"
    +"<br>"
    +"<h2>Are you planning new features?</h2>"
    +"Yes. Improve security, support for sub folders, tracking of downloads "
    +"are some of the features aligned for the future. If you have more "
    + "suggestions then we'd like to hear them. "
    ;
    }

}

