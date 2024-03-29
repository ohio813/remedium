/*
 * This component allows for this instance to serve files to any given
 * web requests.
 * In the future we might add features such as:
 *      - password protection
 *      - download tracking
 *      - upload files
 *      - and so forth
 */

package app.files;

import system.core.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 8th of April 2011 in Darmstadt, Germany
 */
public class FileServer extends Component{

    private String
            httpdocs = "./httpdocs";

    // interesting way to deal with concurrent HashMaps
    private ConcurrentHashMap<String, byte[]>
            fileCache = new ConcurrentHashMap<String, byte[]>();

    private String // common parameters used in URL addresses
            getFile = "name"; 

  public FileServer(Remedium assignedInstance){
       super(assignedInstance);
     }

    @Override
    public void onStart() {
        // create our httpdocs folder if it doesn't exist
        utils.files.mkdirs(httpdocs);

        // add this text to our "About" page
        html.setSection(html.SectionAbout, getAboutText());
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
        return "file";
    }

    @Override
    public String doWebResponse(Request request, Response response) {

        String download = "";

        download = utils.internet.getHTMLparameter(request, getFile);

        // if we mention a download, let's try to provide it
        if(utils.text.isEmpty(download)==false){
            log(INFO,"Delivering "+download+" to "
                    + request.getClientAddress().getHostName());

            try {
                // make the file available for download
                giveFile(request, response, download);

                response.close();

            } catch (IOException ex) {
                log(ERROR,"File '"+download+"' was not served");
            }

            // return a value as "ignore" to prevent changes to
            // signal that no further action is required.
            return "ignore";
        }

        // If nothing is selected, show a listing of files
        String result = giveListing(request, response, "");
        html.setSection(html.SectionHome, result);

        // do a normal listing of files in the folder
        return "";//giveListing(request, response, "");
    }


    /** Provide a listing of files and directories inside a given path */
    private String giveListing(Request request, Response response, String
            where){

        String
                target = "",
               display = "";

        if(utils.text.isEmpty(where)){
            target = httpdocs;
            display = "/";
        }
        else{
            target = httpdocs + File.separatorChar+where;
            display = File.separatorChar+where;
        }

        File folder = new File(target);
        File[] files = folder.listFiles();

        String result = 
                "<H3>Index of "+display+"</H3>\n" +
                "<HR><TABLE>" +
                "<TR><TD><B>Name</B></TD>"+
                "<TD><B>Size</B></TD>";


  if(files != null)
    for (File file : files) {
         boolean isDirectory = file.isDirectory();
         String name = file + (isDirectory ?"/":"");
         String size = isDirectory ? "-" : ""
                 + utils.files.humanReadableSize(file.length());

        result += "<TR><TD><TT>" +

                html.doLink(file.getName(),
                this.getCanonicalName() + "?"+getFile+"="
                + file.getName())

                + "</TT></TD>"+
            "<TD><TT>"+size+"</TT></TD></TR>\n";
      }

    result = result +"</TABLE>"
            //+ "<HR>"
            ;

        return result;
    }


    /** Serve this file to whoemever requests it */
    private void giveFile(Request request, Response response, String download)
            throws IOException {
        OutputStream out = null;
       

            response.setMinor(1);
            out = response.getOutputStream();

            String diskFile = httpdocs+File.separatorChar+download;

            //String type = "text/html; charset=utf-8";
            //byte[] content = null;


            // this adds a caching feature. We might want to disable this later.
            byte[] cached = fileCache.get(diskFile);

         File realFile = new File(diskFile);
         response.set("Content-Type", Indexer.getContentType(diskFile));
         //response.set("Content-Type", "application/octet-stream");
         response.set("Content-Length", (int)realFile.length());
         response.set("Connection", "close");

         InputStream file = null;

       String ignoreCache = utils.internet.getHTMLparameter(request, "nocache");
       Boolean doIgnore = ignoreCache.equalsIgnoreCase("true");

     if(  (cached == null)
        ||(doIgnore==true)
        )

        {
         try{
            file = new FileInputStream(realFile);
            byte[] chunk = new byte[(int)realFile.length()];
            int count = 0;
            int pos = 0;

            while((count = file.read(chunk, pos, chunk.length - pos)) > 0) {

               pos += count;
            }
            cached = chunk;
            fileCache.put(diskFile, cached);
         }
          finally{
            // close our input stream
            file.close();
         }
         }
         //content = cached;
         out.write(cached);
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

