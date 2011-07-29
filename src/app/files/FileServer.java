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

import app.user.User;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import system.core.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
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

    // interesting way to deal with concurrent HashMaps
    private ConcurrentHashMap<String, byte[]>
            fileCache = new ConcurrentHashMap<String, byte[]>();
    
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
            _downloads = 6,
            // special cases
            _empty = 1 // represent an empty array
            ;
   

  public FileServer(Remedium assignedInstance){
       super(assignedInstance);
     }

    @Override
    public void onStart() {
        // create our httpdocs folder if it doesn't exist
        utils.files.mkdirs(httpdocs);
        // add our own text to the "About" page
        html.setSection(html.SectionAbout, getAboutText());
        // the reply object
        LogMessage result = new LogMessage();
        // create the database container
        container = new Container(id, fields, this.getStorage(), result);
        // Output the result from this initialization if something went wrong
        if(result.getResult() == msg.REFUSED){
            this.log(msg.ERROR, result.getRecent());
        }
        // scan all files inside our httpdocs folder
        scanFolder();
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
        if(read.length == 1){

         // define the initial permissions by default
         String permission = "";
         // define the parameter switch
         String parameter = ""; 
            if(file.isDirectory() == true){
                parameter = "type=dir,"; // set this object as a directory
                permission = "public";
            }
            else
                parameter = "type=file,"; // set this object as a file

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
    public void onRecover() {
    }

    @Override
    public void onLoop() {
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
            // redirect to the login page
            addAutoHTMLrefresh(3, this.getInstance().getMyAddress()+"/user");
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
           giveFileMain(request, response, download);
            // return "ignore" to signal that no further action is required.
            return "ignore";
        }

  // File Listing
        // do we have any preferences about the folder?
        String where = utils.internet.getHTMLparameter(request, getFolder);
        // do the file listing
        result = listingMain(where, request);
        // output our final result to the user
        html.setSection(html.SectionHome, result);
        return "";
    }

    
    /** Edit a specific file or folder */
    private String doSave(Request request, String what){
        // where the result will be held
        String result = "";
        // get the details from the intended file
        String[] record = container.read(what);
        // have we really found a file?
        if(record.length == _empty){
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
        
         result  = "<h2>Save operation completed</h2>"
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
        if(read.length == _empty){
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
                "<h2>Edit '"+read[_name]+"'</h2>"
                +"<form method=\"post\""
                +" action=\"?save="+what //+" name=\"edit=\"\"
                       + "\">"
                +"  <table style=\"text-align: left; width: 468px; height: "
                       + "63px;\""
                +" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">"
                +"    <tbody>"
                +"      <tr>"
                +"        <td"
                +" style=\"font-family: monospace; text-align: center; "
                + "vertical-align: top;\">Permissions (one per line)</td>"
                +"        <td style=\"width: 341px;\" align=\"undefined\""
                +" valign=\"undefined\"><textarea wrap=\"off\" rows=\"8\""
                +" cols=\"50\" name=\"permissions\">" + permission
                + "</textarea></td>"
                +"      </tr>"
                +"      <tr>"
                +"        <td style=\"font-family: monospace;\">Downloads</td>"
                +"        <td style=\"width: 341px;\" align=\"undefined\""
                +" valign=\"undefined\"><input style=\"font-family: monospace;\""
                +" name=\"downloads\" size=\"4\" value=\""+read[_downloads]
                       +"\"></td>"
                +"      </tr>"
                +"    </tbody>"
                +"  </table>"
                +"  <br>"
                +"  <input style=\"font-family: monospace;\" value=\"Save\""
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
            ArrayList<Properties> records = container.read(path, targetParent);
            // iterate each one of them if not empty
            if(records.size() > 0)
                for(Properties record : records){
                    // get the name of the folder
                    String recordName = record.getProperty(name);
                    // if it is not our target folder, don't process
                    if(recordName.equals(targetName)==false)
                        continue;
                    // get the permissions for this folder
                    String targetPermission = record.getProperty(permissions);
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
            // output an error message to the end user
            String text = "<h2>Restricted folder</h2>"
                        + "Redirecting you to the main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return text;
        }else
             log(msg.ACCEPTED, "Access authorized of '" + cleanDisplay +"'"
                     + " to '" + this.getLoggedUser(request).getMyName()+"'");


        // display the header of our page
        String result =
                "<H3>Index of "+cleanDisplay+"</H3>\n"
                + "<HR><TABLE>"
                + "<TR>"
                + "<TD><B>&nbsp&nbspName</B></TD>"
                + "<TD><B>&nbsp&nbspSize</B></TD>"
                + "<TD><B>&nbsp&nbspDownloads</B></TD>"
                + "<TD><B>&nbsp&nbspPermissions</B></TD>"
                ;

        // display folders in first position
        result += this.listingFolders(target);
        // display folders in first position
        //result += this.listingFiles(target, "dir");
        result += this.listingFiles(target);
        // close down our table
        result += "</TABLE>";
        // all done!
        return result;
    }

    /** Display the folders when requested by the user */
    private String listingFiles(String target){
        String result = "";
        // convert from Windows slashes to UNIX style
        target = target.replace("\\", "/");
        // get all files belonging to this folder
        ArrayList<Properties> files = container.read(path, target);
        // if not empty, iterate all our found files
        if(files.size() > 0)
            for(Properties record : files){
                String parameter = record.getProperty(parameters);
                if(parameter.contains("type=dir,"))
                    continue;
                // get the URL
               String textLink = this.getCanonicalName()
                    + "?" + getUID + "=" + record.getProperty(uid);
               // convert this text to an HTML link
               textLink = html.doLink(record.getProperty(name), textLink)
                       + "&nbsp&nbsp"; // add some space to make this prettier
               // size of this file
               String textSize = record.getProperty(size);
               textSize = utils.files.humanReadableSize
                       (Long.parseLong(textSize));
               // How many times it was downloaded
               String textDownloads = record.getProperty(downloads);
               // display this value if it isn't empty
               if(textDownloads.equalsIgnoreCase("0")==false)
                   textDownloads =
                       "<div style=\"text-align: center;\">"
                       + record.getProperty(downloads)
                       + "</div>";
               else // don't display anything for this value
                   textDownloads = "";
               // add up this item to our changes
               result = result.concat(
                 this.addItem(
                       textLink,
                       textSize,
                       textDownloads
                       ));
            }
        return result;
    }



    /** Display the folders when requested by the user */
    private String listingFolders(String target){
         String result = "";
        // convert from Windows slashes to UNIX style
        target = target.replace("\\", "/");
        // get all files belonging to this folder
        ArrayList<Properties> files = container.read(path, target);
        // if not empty, iterate all our found files
        if(files.size() > 0)
            for(Properties record : files){
                // get our parameters
                String parameter = record.getProperty(parameters);
                // ignore files since we don't like them here
                if(parameter.contains("type=file,"))
                    continue;

             // create the full path
             String cleanPath =
                     record.getProperty(path)
                     + "/"
                     + record.getProperty(name)
                     ;
             // remove the root folder part from this string
             cleanPath = cleanPath.replace(httpdocs + "/", "");

            // do the final URL link
               String textLink = this.getCanonicalName()
                    + "?" + getFolder + "=" + cleanPath;
               // convert this text to an HTML link
               textLink = "[DIR] " + html.doLink(record.getProperty(name),
                       textLink);
               // set up the permissions tab
               String permission = setupFolderPermissions(record);
               // add up this item to our changes
               //TODO add the total count and size of files inside each folder
               result = result.concat(this.addItem(textLink, "","",permission));
           }
        return result;
    }

    
    /** Either shows readonly permissions or allows to edit them if admin */
    private String setupFolderPermissions(Properties record){
        String result = "";
        // get the current user
        User user = this.getLoggedUser(currentRequest);
        // get our permissions and format them for user display
        String permissionText = 
                record.getProperty(permissions).replace(",", ", "); 
        // are we admin?
        if(user.hasPermission(User.admin)){
            result =
                     "&nbsp"
                    + "&nbsp"
                    + permissionText
                    + "</TT></TD><TD><TT>"
                       + "&nbsp&nbsp"
                       + html.doLink("edit", 
                    "?edit="+record.getProperty(uid));
        }
        else{
        result = "<div style=\"text-align: center;\">"
                       + permissionText
                       + "</div>";
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
    private void giveFileMain(Request request, Response response,
            String download){
        // does our file really exists?
        String[] record = container.read(download);

        // we didn't found this file
        if(record.length == 1){
            try { // close our stream of data
                response.close();
            } catch (IOException ex) {}
            // stop right here
            return;
        }

        // get the file parts
        String filename = record[1];
        String filepath = record[2];
        String fullname = filepath + "/" + filename;
        Long counter = Long.parseLong(record[6]);

        log(msg.INFO, "Delivering " + fullname + " to "
                    + request.getClientAddress().getHostName());
              
    // increase the counter
    counter++; // increment
    record[6] = "" + counter; // write on the respective field position
    // write back this value onto the database
    container.write(record);
    // provide the file to the end user
    File selectedFile = new File(fullname);
    this.giveFileDownload(request, response, selectedFile);
    // all done
    }

       /** Serve the requested file */
    private void giveFileDownload(Request request, Response response,
            File downloadfile
            //String download, String where
            ){

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
            response.set("Content-Type", Indexer.getContentType(diskFile));
            //response.set("Content-Type", "application/octet-stream");
            response.set("Content-Length", (int) realFile.length());
            // define the file name
            response.set("Content-Disposition", "filename=\""
                    + downloadfile.getName() + "\"");

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

