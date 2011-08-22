/*
 * This component provides license management and acceptance to users.
 * 
 * The file server uses this class to verify that a given user will only
 * access a given resource or service after accepting the conditions of a given
 * license.
 * 
 * This component will also allow the administrator to add more licenses
 * 
 */

package app.files;

import app.user.User;
import java.io.IOException;
import java.util.ArrayList;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Form;
import system.core.Component;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import remedium.Remedium;
import system.container.Container;
import system.log.LogMessage;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 31th of July 2011 in Darmstadt, Germany.
 */
public class LicenseComponent extends Component{

    // our identification
    private static final String id = "license";
    
    private Container container; // where we store data

    private static final String // definitions of our fields
            uid = "uid",
            title = "title",
            description = "description",
            content = "content",
            tags = "tags",
            permissionModify = "permissionModify",
            parameters = "parameters"
            ;

    private final String[] fields = new String[] // fields for our database
        {
        uid, // unique identification
        title, // license name
        description, // short description
        content,  // the contents of this license
        tags, // tags that can read this license
        permissionModify, // tags that can modify this license
        parameters // password=123, expires=2011/10/10:10h12 (...)
    };
    
        public static final int // define the index of each field inside our database
            _uid = 0,
            _title = 1,
            _description = 2,
            _content = 3,
            _tags = 4,
            _permissionModify = 5,
            _parameters = 6
            ;
    
        // the reserved keyword for web services
     public final static String
             newLicense = "newLicense",
             submitLicense = "submitLicense",
             doAccept = "doAccept",
             action = "action",
             view = "view",
             edit = "edit",
             accept = "accept",
             delete = "delete";
        
        
    // public constructor
    public LicenseComponent(Remedium assignedInstance){
        // call the super component!
         super(assignedInstance);
     }

    @Override
    public void onStart() {
        log(msg.INFO,"Starting the license server");
        // the reply object
        LogMessage result = new LogMessage();
        // create the database container
        container = new Container(id, fields, this.getStorage(), result);
        // Output the result from this initialization if something went wrong
        if(result.getResult() == msg.REFUSED){
            this.log(msg.ERROR, result.getRecent());
        }
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
        // the end result holder
        String result = "";
        // interpret action
        String myAction = utils.internet.getHTMLparameter(request, action);
        // do we want to add a new license?
        if(myAction.equals(newLicense)){
            result = createNewLicense(request);
        }
            else
        if(myAction.equals(submitLicense)){
            result = submitNewLicense(request);
        }   else
        if(myAction.equals(doAccept)){
            result = submitAcceptLicense(request, response);
        }    
        
 // view       
        // interpret a view request
        result = this.viewLicense(request, result); 
        // avoid double requests
 // edit 
        if(result.isEmpty())
            // interpret an edit request
            result = this.editLicense(request, result); 
 // delete      
        if(result.isEmpty())
            // delete a license
            result = this.deleteLicense(request, result); 
 // accept      
        if(result.isEmpty())
            // accept a given set of licenses
            result = this.acceptLicense(request); 
// default reply        
        // add the default reply if none of the above replies
        if(result.isEmpty())
            // list all the licenses
            result = listLicenses(request);
        // set our text as end result
        this.html.setSection(html.SectionHome, result);
        // all done
        return result;
    }
    
    
    /** Interpret a request to view a license */
    private String viewLicense(Request request, String text){
        String result = text;
        // get the view parameter
        String requestedLicense = utils.internet.getHTMLparameter(request, view);
        // clean up our string
        requestedLicense = utils.text.safeString(requestedLicense);
        // if it is empty, exit this place
        if(utils.text.isEmpty(requestedLicense))
            return result;
        // get the record
        String[] record = container.read(requestedLicense);
        // did we got a good result?
        if(record == null)
            return result; // it is empty, exit here
        
        // where we will hold our contents
        String getDescription;
        String getContent;
        
        try {
            // get the decoded text of our licenses
            getDescription = new String(utils.Base64.decode(record[_description]));
            getContent = new String(utils.Base64.decode(record[_content]));
            // make the content web-friendly
            getContent = getContent.replace("\n", "<br>\n");
        } catch (IOException ex) {
            return result;
        }
        
        // display the results
        result =
            "<h2>"+ record[_title]+"</h2>"
            +"<table style=\"text-align: left; width: 625px; height: 88px;\""
            +" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">"
            +"  <tbody>"
            +"    <tr>"
            +"      <td align=\"undefined\" valign=\"undefined\">"
            +"      <address>"
                + getDescription
                +"</address>"
            + viewLicenseLinks(record, request) // add links if we are admin
            + "<hr>"
            +"      </td>"
            +"    </tr>"
            +"    <tr>"
            +"      <td style=\"height: 26px;\" align=\"undefined\""
            +" valign=\"undefined\">"+ getContent+"</td>"
            +"    </tr>"
            +"  </tbody>"
            +"</table>";
       
        // all done
        return result;
    }
    
    
    /** Deliver the action links when viewing licenses */
    private String viewLicenseLinks(String[] license, Request request){
        String result = "<br>";
        // get the current user    
        User user = this.getLoggedUser(request);
        // are we admin or similar?    
        if(user.hasPermission(User.admin)== false){
            return result; // not admin, exit here
        }
        // show the links
        result = result.concat(
                // add the edit link
        html.doLink("edit", this.getWebAddress() + "?"
                + edit + "=" + license[_uid] ) 
                + " | " +
                // add the delete link
        html.doLink("delete", this.getWebAddress() + "?"
                + delete + "=" + license[_uid] )
                
                );
         // all done
        return result;
    }
    
    
    /** Submit user agreement on specific licenses*/
     private String submitAcceptLicense(Request request, Response response){
         // where we hold the page results
         String result = "";
         // get the licenses that were accepted
         String myLicenses = utils.internet.getHTMLparameter(request, "licenses");
         // get the redirection page
         String redirect = utils.internet.getHTMLparameter(request, "redirect");
         // if either one of them is empty, exit here
         if( (utils.text.isEmpty(myLicenses))
          || (utils.text.isEmpty(redirect)) ){
             return "";
         }
         // all seems fine so far, let's do the license acceptance for this user
         for(String myLicense : myLicenses.split(",")){
             // read the license
             String[] record = container.read(myLicense);
             // if it doesn't exist, something fishy has happened and we quit
             if(record == null)
                 return "";
             // since the record is not null, we can write the license as cookie
             Cookie cookie = new Cookie("license_" + myLicense, "accepted");
             response.setCookie(cookie);
         }
         // all done, let's get back
         result = "<h2>License agreed</h2>"
                + "Redirecting to previous link..";
            // redirect to the intended link
            addAutoHTMLrefresh(3, redirect);
         
         
         return result;
     }
    /** Submit a new license for acceptance*/
     private String submitNewLicense(Request request){
        // initialize our variables
         String 
                 formId = "",
                 formTitle = "",
                 formDescription = "",
                 formContent = "",
                 formTags = "";
         
        try {
            // get the form
            Form form = request.getForm();
            
            // attempt to get the UID from a parameter
            formId = utils.internet.getHTMLparameter(request, uid);
            // if empty, try to find it inside the form
            if(utils.text.isEmpty(formId))
                formId = utils.text.safeString(form.get(id));
            // get the rest of the parameters
            formTitle = utils.text.safeHTML(form.get(title));
            // get this text in a safe format for storage (prevent SQL injections)
            formDescription = utils.Base64.encodeBytes(form.get(description).getBytes());
            formContent = utils.Base64.encodeBytes(form.get(content).getBytes());
            
        } catch (IOException ex) {
            String result = "<h2>Failed to save license</h2>"
                    + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return result;
        }
        
       // is our data acceptable?
      if(
           (utils.text.isEmpty(formTitle))
        || (utils.text.isEmpty(formDescription))
        || (utils.text.isEmpty(formContent))
        )  {
          String result =
                  "<h2>Failed to save license</h2>"
                  + "Redirecting back to main page..";
          // redirect to the main page
          addAutoHTMLrefresh(3, this.getWebAddress());
          return result;
      }
      
        // clean up the tags
        formTags = formTags.replace("\r", ""); 
        formTags = formTags.replace("\n", ","); 
        
        // create our record with all the values
        String[] record = new String[] // fields for our database
        {
        formId, // unique identification
        formTitle, // license name
        formDescription, // short description
        formContent,  // the contents of this license
        formTags, // tags that can read this license
        User.admin, // tags that can modify this license
        "" // password=123, expires=2011/10/10:10h12 (...)
    };
        // write the record back onto the container
        container.write(record);
        
        
         
        String result = "<h2>License saved</h2>"
                        + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
        return result;
     }
        
     /** Accept a given set of licenses */
    private String acceptLicense(Request request){
        // get the value for the accept parameter
        String deleteLicense = utils.internet.getHTMLparameter(request, accept);
        // if the request is empty, leave here
        if(utils.text.isEmpty(deleteLicense))
            return "";
        // hold the result of our processing
        String result = "";
        // get the list of licenses to accept
        String licenseList = utils.internet.getHTMLparameter(request, accept);
        String redirect = utils.internet.getHTMLparameter(request, "redirect");
        // none of these values is accepted as empty
        if((utils.text.isEmpty(licenseList))
                ||
                (utils.text.isEmpty(redirect))
                ){
            return "";
        }
        // all nice so far, let's display the license page
        result =  
            "<h2>License acceptance</h2>"
            +"<hr><br>"
            +"To proceed, you need to accept the terms and conditions of the"
            +" following license(s)<br>"
            +"<br>"
            +"<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\">"
            +"  <tbody>"
            +"    <tr>"
            +"      <td></td>"
            +"      <td><b>Title</b></td>"
            +"      <td><b>Description</b></td>"
            +"    </tr>";
                
        // list all our licenses
        for(String thisLicense : licenseList.split(",")){
            // read the license from our database
            String[] record = container.read(thisLicense);
            // if null, something is wrong and should quit here
            if(record == null)
                return "";
            // do the nice layout
            result = result.concat(
                    "<TR>"
                    + "<TD>"
                    + utils.MimeIndexer.getIconHTML("aa.txt") 
                    +"</TD>"
                    + "<TD>"
                    + "<a target=\"_blank\" href=\""
                    + this.getWebAddress() 
                    + "?" + view + "=" + record[_uid]
                    + "\">"
                    + record[_title] 
                    + "</a>"
                    + "</TD>"
                    + "<TD>"
                    + utils.text.decodeBase64(record[_description])
                    +"</TD>"
                    + "</TR>\n");
        }
                
                
     result += "  </tbody>"
            +"</table>"
            +"<br>"
            +"<form method=\"post\" action=\"?"+action+"="+doAccept
            + "&licenses=" + licenseList 
            + "&redirect=" + redirect 
            +"\""
            +" name=\"accept\"><input value=\"Accept\" type=\"submit\"></form>"
            +"<br>"
            +"<br>"
            +"</div>"
            +"</div>";
        // all done
        return result;
    }
    
     
     
     /** Delete a given license */
    private String deleteLicense(Request request, String text){
        // get the value for the edit parameter
        String deleteLicense = utils.internet.getHTMLparameter(request, delete);

        // if the request is empty, leave here
        if(utils.text.isEmpty(deleteLicense))
            return text;
        // output result
        String result = text;
        // get the license details
        String[] license = container.read(deleteLicense); 
        // test if it was found or not
        if(license == null){
                result = "<h2>License not found</h2>"
                        + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
        return result;
        }
        // delete the license
        container.delete(uid, deleteLicense);
        
        result = "<h2>License '"+license[_title]+"' deleted</h2>"
                        + "Redirecting back to main page..";
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
        
        
        // all done
        return result;
    }
    
     
    
    /** Edit a given license */
    private String editLicense(Request request, String text){
        // get the value for the edit parameter
        String editLicense = utils.internet.getHTMLparameter(request, edit);

        // if the request is empty, leave here
        if(utils.text.isEmpty(editLicense))
            return text;
        
        // preflight checks, do we have permission for this action?
        if(this.getLoggedUser(request).hasPermission(User.admin)==false)
            {
                String result = "<h2>Not allowed to edit</h2>"
                        + "Redirecting back to main page..";
                addAutoHTMLrefresh(3, this.getWebAddress());
                return result;
            }
        
        // get the record
        String[] license = container.read(editLicense);
        
        // is this license valid?
      if(license == null){
          String result = "<h2>License was not found</h2>"
                  + "Redirecting back to main page..";
          // redirect to the main page
          addAutoHTMLrefresh(3, this.getWebAddress());
          return result;
      }
      
      // holder of decoded data
      String getDescription;
      String getContent;
      
        try {
            // translate the encoded values
            getDescription = new String(utils.Base64.decode(license[_description]));
            getContent = new String(utils.Base64.decode(license[_content]));
            
        } catch (IOException ex) {
            String result = "<h2>Failed to edit license</h2>"
                        + "Redirecting back to main page..";
                addAutoHTMLrefresh(3, this.getWebAddress());
                return result;
        }
      
        
        // do the output
        String result =
            "<h2>Edit '"+license[_uid]+"' license</h2>"
            +"<hr>"
            +"<form method=\"post\" action=\"?"+action+"="+submitLicense
            + "&" + uid + "=" + license[_uid]
            +"\""
            +" name=\""+submitLicense+"\">"
            +"  <table style=\"text-align: left; width: 5px;\" border=\"0\""
            +" cellpadding=\"5\" cellspacing=\"2\">"
            +"    <tbody>"
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Title</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><input"
            +" value=\""+license[_title]+"\""
            +" size=\"50\" name=\""+title+"\"></td>"
            +"      </tr>"
                
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Description</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><textarea"
            +" cols=\"50\" rows=\"5\" name=\""+description+"\">"
                + getDescription
                +"</textarea></td>"
            +"      </tr>"
           
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Content</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><textarea"
            +"  wrap=\"nowrap\" cols=\"100\" rows=\"10\" name=\""+content+"\">"
                + getContent
            + "</textarea></td>"
            +"      </tr>"
                
            +"      </tr>"
            +"    </tbody>"
            +"  </table>"
            +"  <br>"
            +"  <input value=\"Save\" type=\"submit\"><br>"
            +"</form>"
            +"</div>"
            +"</div>";
        return result;
    } 
     
    /** Create a new license */
    private String createNewLicense(Request request){
        // preflight checks, do we have permission for this action?
        if(this.getLoggedUser(request).hasPermission(User.admin)==false)
            {
                String result = "<h2>Not allowed to create new licenses</h2>"
                        + "Redirecting back to main page..";
                addAutoHTMLrefresh(3, this.getWebAddress());
                return result;
            }
        // output the text for creating a new license
        String result =
            "<h2>Add a new license</h2>"
            +"<hr>"
            +"<form method=\"post\" action=\"?"+action+"="+submitLicense+"\""
            +" name=\""+submitLicense+"\">"
            +"  <table style=\"text-align: left; width: 5px;\" border=\"0\""
            +" cellpadding=\"5\" cellspacing=\"2\">"
            +"    <tbody>"
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Id tag</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><input"
            +" name=\""+id+"\">&nbsp;</td>"
            +"      </tr>"
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Title</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><input"
            +" size=\"50\" name=\""+title+"\"></td>"
            +"      </tr>"
                
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Description</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><textarea"
            +" cols=\"50\" rows=\"5\" name=\""+description+"\"></textarea></td>"
            +"      </tr>"
           
            +"      <tr>"
            +"        <td style=\"text-align: right;\" valign=\"undefined\">Content</td>"
            +"        <td align=\"undefined\" valign=\"undefined\"><textarea"
            +"  wrap=\"nowrap\" cols=\"50\" rows=\"10\" name=\""+content+"\">"
            + "</textarea></td>"
            +"      </tr>"
                
            +"    </tbody>"
            +"  </table>"
            +"  <br>"
            +"  <input value=\"Save\" type=\"submit\"><br>"
            +"</form>"
            +"</div>"
            +"</div>";
        
        // all done
        return result;
    }
    
    /** Return all the available licenses on this server */
    public ArrayList<String[]> getAll(){
        return container.readAll();
    }
    
    /** List all the available licenses */
    private String listLicenses(Request request){
        String result = "";
        // get a list of all the available licenses
        ArrayList<String[]> licenses = container.readAll();
        // is our user admin or not?
        boolean isAdmin = this.getLoggedUser(request).hasPermission(User.admin);
        
        // do the column header
        result = "<H2>Licenses</H2>\n"
                + "<HR>"
                + "<TABLE border=\"0\" cellpadding=\"5\" cellspacing=\"0\">"
                + "<TR>"
                + "<TD></TD>"
                + "<TD><B>Title</B></TD>"
                + "<TD><B>Description</B></TD>"
                + "</TR>\n"
                ;
        
        if(licenses != null)
        // iterate each license available
        for(String[] license : licenses){
            if(license.length != fields.length)
                continue;
            result = result.concat(
                    "<TR>"
                    + "<TD>"
                    +utils.MimeIndexer.getIconHTML("aa.txt") 
                    +"</TD>"
                    + "<TD>"+html.doLink( license[_title], 
                    this.getWebAddress() 
                    + "?" + view + "=" + license[_uid])
                    +"</TD>"
                    + "<TD>"+utils.text.decodeBase64(license[_description])
                    +"</TD>"
                    + "</TR>\n");
        }
        
          result += "</TBODY>\n"
                 +  "</TABLE>\n"
                 +  "</TABLE>\n"
                 +  "<HR>\n"
                  ;
        
        // Add link for the new user
        if(isAdmin == true)
            result = result.concat(this.html.doLink("Add new license", 
                this.getWebAddress() + "?action=" + newLicense));
        // all done
        return result;
    }
    
}