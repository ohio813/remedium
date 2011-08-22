/*
 * This class provides the administration backend to manage users and permissions.
 *
 */

package app.user;

import java.io.IOException;
import org.simpleframework.http.Form;
import org.simpleframework.http.Request;

/**
 *
 * @author Nuno Brito, 26th of July 2011 in Darmstadt, Germany
 */
public class PageAdmin {
    // our host component
    private UserComponent component; 

    public PageAdmin(UserComponent assignedComponent){
        this.component = assignedComponent;
    }

     /** Show specific text if we are logged inside the system as admin */
    public String showAdminPage(Request request, String result){
        User loggedUser = component.getLoggedUser(request);
        // are we administrators?
        if(loggedUser.hasPermission(User.admin) == false)
            // no we are not, exit here
            return result;

        // handle the possible actions for an adminstrator

      // edit a user
        String edit = utils.internet.getHTMLparameter(request, User.edit);
        if(utils.text.isEmpty(edit)==false){
            return showPageEditUser(edit) + logOutText();
        }

      // handle the edit user data if being submitted
        String doEdit = utils.internet.getHTMLparameter(request, User.doEdit);
        if(utils.text.isEmpty(doEdit)==false){
            return doEdit(doEdit, request);
        }

      // handle a delete user request
        String doDelete = utils.internet.getHTMLparameter(request, User.doDelete);
        if(utils.text.isEmpty(doDelete)==false){
            return doDelete(doDelete);
        }

      // handle a request for user details
        String doDetails = utils.internet.getHTMLparameter(request, User.doDetails);
        if(utils.text.isEmpty(doDetails)==false){
            return showPageUserDetail(doDetails);
        }


     // no special requests? Show our user list
        String output = "<h2>Manage users</h2>\n"
            + "<table"
                + ">\n"
            + "<tbody style=\"vertical-align: top;\">\n"

            + "<tr>"
            +"      <td style=\"text-align: left; width: 86px;\">"
            +"      <h3 style=\"width: 60px;\">User</h3>"
            +"      </td>"
            +"      <td style=\"width: 301px;\" align=\"undefined\" valign=\"undefined\">"
            +"      <h3 style=\"margin-left: 0px; width: 375px;\">Permissions</h3>"
            +"      </td>"
            +"      <td style=\"width: 229px;\" align=\"undefined\" valign=\"undefined\">"
            +"      <h3>Since</h3>"
            +"      </td>"
            +"    </tr>";

        // list all the users
        for(User user : component.getUsers()){
            // create the entry for this user
             String converted =
                "<tr>"
                +"      <td style=\"width: 60px;\"><tt>"
                     + "<a href=\""+component.getWebAddress()
                     +"?edit="+user.getMyName()+"\">"+user.getMyName()+"</a>"
                     + "</tt>"
                     + "</td>"
                +"      <td style=\"width: 301px; "
                +" align=\"undefined\" valign=\"undefined\">"
                     +user.getMyPermissions().replace(",", ", ")
                     +"</td>"
                +"      <td style=\"width: 229px;\" align=\"undefined\""
                +" valign=\"undefined\"><span>"
                + utils.time.getTimeFromLong(Long.parseLong(user.getMyLoggedSince()))
                +"</span></td>"
                +"    </tr>";

            output = output.concat(converted);
        }
        // close the table
        output += "</tbody></table>";
        // do the footer links for other actions
        output += "<br><h3>Actions</h3>"
                + "<a href=\"?action=register\">Register user</a>"
                + " | "
                + component.html.doLink("log out", component.getWebAddress()
                  + "?action="
                  + User.logout);
    // set the text for the about page
       return output;// + logOutText();
    }

    /** Returns an error message back to control */
    private String exitMessage(String title){
    String out = "<h2>"+title+"</h2>"
               + "Returning to main page..";
            component.html.setSection(component.html.SectionHome,out);
            // redirect to the main page
            component.addAutoHTMLrefresh(3, component.getWebAddress());
            // return our results
            return out;
    }

    /** Show the page of details about a given user */
    private String doDelete(String who){
       // get the user that they relate to
        User user = component.getUser(who);
        // does this user exists?
        if(user == null){
            // a serious error occured, let's get back to where we were
            return exitMessage("User '"+who+"' was <b>not</b> deleted");
        }
        // ok, it does exist. Let's delete the user
        component.deleteUser(who);
        // all done
        return exitMessage("User '"+who+"' was deleted");
    }

    /** Show the page of details about a given user */
    private String showPageUserDetail(String who){
        // get the user that they relate to
        User user = component.getUser(who);
        // does this user exists?
        if(user == null){
            // a serious error occured, let's get back to where we were
            return exitMessage("User '"+who+"' was <b>not</b> found");
        }

        String permission = user.getMyPermissions();
        permission = permission.replace(",", ", ");

        String output = "<h2>Details for '"+who+"'</h2>"
            +"<table style=\"text-align: left; width: 303px;\" border=\"0\""
            +" cellpadding=\"2\" cellspacing=\"2\">"
            +"  <tbody>"
            +"    <tr>"
            +"      <td style=\"font-family: monospace; width: 123px;\""
            +" align=\"undefined\" valign=\"undefined\"><span"
            +" style=\"font-weight: bold;\">Username</span></td>"
            +"      <td style=\"font-family: monospace; width: 160px;\""
            +" align=\"undefined\" valign=\"undefined\">"+who+"</td>"
            +"    </tr>"
            +"    <tr>"
            +"      <td style=\"font-family: monospace; width: 123px;\""
            +" align=\"undefined\" valign=\"undefined\"><span"
            +" style=\"font-weight: bold;\">Registration</span></td>"
            +"      <td style=\"font-family: monospace; width: 160px;\""
            +" align=\"undefined\" valign=\"undefined\">"
            + utils.time.getTimeFromLong(Long.parseLong(user.getMyLoggedSince()))
            +"</td>"
            +"    </tr>"
            +"    <tr>"
            +"      <td style=\"font-family: monospace; width: 123px;\""
            +" align=\"undefined\" valign=\"undefined\"><span"
            +" style=\"font-weight: bold;\">Permissions</span></td>"
            +"      <td style=\"font-family: monospace; width: 160px;\""
            +" align=\"undefined\" valign=\"undefined\">"+permission+"</td>"
            +"    </tr>"
//            +"    <tr>"
//            +"      <td style=\"font-family: monospace; width: 123px;\""
//            +" align=\"undefined\" valign=\"undefined\"><span"
//            +" style=\"font-weight: bold;\">Downloads</span></td>"
//            +"      <td style=\"font-family: monospace; width: 160px;\""
//            +" align=\"undefined\" valign=\"undefined\"></td>"
//            +"    </tr>"
//            +"    <tr>"
//            +"      <td"
//            +" style=\"font-family: monospace; font-weight: bold; width: 123px;\""
//            +" align=\"undefined\" valign=\"undefined\">Top downloaded"
//            +" files</td>"
//            +"      <td style=\"font-family: monospace; width: 160px;\""
//            +" align=\"undefined\" valign=\"undefined\"></td>"
//            +"    </tr>"
//            +"    <tr>"
//            +"      <td"
//            +" style=\"font-family: monospace; font-weight: bold; width: 123px;\""
//            +" align=\"undefined\" valign=\"undefined\">Known IP"
//            +" addresses</td>"
//            +"      <td style=\"font-family: monospace; width: 160px;\""
//            +" align=\"undefined\" valign=\"undefined\"></td>"
//            +"    </tr>"
//            +"    <tr>"
//            +"      <td"
//            +" style=\"font-family: monospace; font-weight: bold; width: 123px;\""
//            +" align=\"undefined\" valign=\"undefined\"></td>"
//            +"      <td style=\"font-family: monospace; width: 160px;\""
//            +" align=\"undefined\" valign=\"undefined\"></td>"
//            +"    </tr>"
            +"  </tbody>"
            +"</table>";

        return output;
    }



    /** Edit a given user */
    private String doEdit(String who, Request request){
        
        // define the form object
        Form rem;
        try {
            // we need to get our form with details
            rem = request.getForm();
        } catch (IOException ex) {
            // a serious error occured, let's get back to where we were
            return exitMessage("User '"+who+"' was <b>not</b> edited");
        }

        // get the values from the form
//        String getName = rem.get(User.name);
        //String getStatus = rem.get(User.status);
        String getPermissions = rem.get(User.permissions);

        getPermissions = getPermissions.replace("\r", "");
        getPermissions = getPermissions.replace(" ", "");
        getPermissions = getPermissions.replace("\n", ",");

        int lastIndex = getPermissions.lastIndexOf(",");
        int length = getPermissions.length() - 1;

        if(lastIndex == length) // do we have a comma at the end of string?
            getPermissions = // remove it from our list
                    getPermissions.substring(0, getPermissions.length() - 1);


        // get the user that they relate to
        User user = component.getUser(who);

        if(user == null){
            // a serious error occured, let's get back to where we were
            return exitMessage("User '"+who+"' was <b>not</b> edited");
        }

        // write them back
//        user.setMyName(getName);
//        user.setMyUid(getName);
        user.setMyPermissions(getPermissions);
        // delete any old user
//        component.deleteUser(who);
        // edit our user
        component.editUser(user.getRecord());

        // do the final part
         String out = "<h2>User '"+who+"' was edited</h2>"
                    + "Returning to main page..";
            component.html.setSection(component.html.SectionHome,out);
            // redirect to the main page
            component.addAutoHTMLrefresh(3, component.getWebAddress());
            // return our results
            return out;
    }


    /** Show the page of details about a given user */
    private String showPageEditUser(String who){
        // get the user that was mentioned
        User user = component.getUser(who);
        // have we found a user?
        if(user == null){ // means that no user was found
            String out = "<h2>User not found</h2>"
                    + "Redirecting you to the login page..";
            component.html.setSection(component.html.SectionHome,out);
            // redirect to the main page
            component.addAutoHTMLrefresh(3, component.getWebAddress());
            return out;
        }
        
        String formattedPermissions = user.getMyPermissions();
        formattedPermissions = formattedPermissions.replace(",", "\n");

        // output our edit page
        String result =        
            "<h2>Editing '"+user.getMyName()+"'</h2>"
            +"<form method=\"post\" action=\"?doEdit="+user.getMyName()+"\">"
            +"  <table style=\"width: 330px; height: 170px;\" cellpadding=\"3\""
            +" cellspacing=\"3\">"
            +"    <tbody style=\"vertical-align: top;\">"
//            +"      <tr>"
//            +"        <td style=\"text-align: left; width: 114px;\">"
//            +"        <h3 style=\"width: 114px;\">Property</h3>"
//            +"        </td>"
//            +"        <td style=\"width: 176px;\">"
//            +"        <h3 style=\"margin-left: 0px; width: 193px;\">Value</h3>"
//            +"        </td>"
//            +"        <td style=\"width: 383px;\" align=\"undefined\""
//            +" valign=\"undefined\"></td>"
//            +"      </tr>"


//            +"      <tr>"
//            +"        <td style=\"width: 114px;\"><tt>Name</tt></td>"
//            +"        <td style=\"font-family: monospace; width: 176px;\""
//            +" align=\"undefined\" valign=\"undefined\"><input"
//            +" name=\"name\" value=\""+user.getMyName()+"\"><br>"
//            +"        </td>"
//            +"        <td style=\"font-family: monospace; width: 383px;\""
//            +" align=\"undefined\" valign=\"undefined\">Please use only"
//            +" letters and numbers. No spaces or other characters should be used</td>"
//            +"      </tr>"


            +"      <tr>"
            +"        <td style=\"width: 118px;\"><span"
            +" style=\"font-family: monospace;\">Permissions<br>(one per line)"
                + "</span></td>"
            +"        <td style=\"font-family: monospace; width: 187px;\""
            +" align=\"undefined\" valign=\"undefined\"><textarea"
            +" class=\"fullpage colmid\" style=\"font-family: monospace;\""
            +" cols=\"1\" rows=\"10\" name=\"permissions\">"
                +formattedPermissions
            + "</textarea><br>"
            +"        </td>"
//            +"        <td style=\"font-family: monospace; width: 383px;\""
//            +" align=\"undefined\" valign=\"undefined\">Define the"
//            +" permissions for this user. Please add one permission tag per line</td>"
            +"      </tr>"

//            +"      <tr>"
//            +"        <td style=\"width: 114px;\"><span"
//            +" style=\"font-family: monospace;\">Password</span></td>"
//            +"        <td style=\"width: 176px; font-family: monospace;\""
//            +" align=\"undefined\" valign=\"undefined\"><input"
//            +" name=\"date\" value=\""+user.getMyPassword()+"\"></td>"
//            +"        <td style=\"font-family: monospace; width: 383px;\""
//            +" align=\"undefined\" valign=\"undefined\">Set the date of"
//            +" registration for this user</td>"
//            +"      </tr>"

//            +"      <tr>"
//            +"        <td style=\"width: 114px;\"><span"
//            +" style=\"font-family: monospace;\">Status</span></td>"
//            +"        <td style=\"font-family: monospace; width: 176px;\""
//            +" align=\"undefined\" valign=\"undefined\">"
//            +"        <select name=\"status\">"
//            +"        <option value=\"active\" selected=\"selected\">active</option>"
//            +"        <option>inactive</option>"
//            +"        </select>"
//            +"        </td>"
//            +"        <td style=\"font-family: monospace; width: 383px;\""
//            +" align=\"undefined\" valign=\"undefined\">"
////            User status. If"
////            +" the user is set as inactive, he will not be allowed to login
//            +"</td>"
//            +"      </tr>"
                
                
            +"    </tbody>"
            +"  </table>"
//            +"  <br>"
            +"  <input value=\"Save\" type=\"submit\">"
                + "&nbsp&nbsp"
                + component.html.doLink("delete user", 
                    component.getWebAddress()
                    +"?"+User.doDelete
                    +"="+user.getMyName())
                + "</form>"
            
                ;
        
        return result;
    }

    /** Display the log out text */
    public String logOutText(){
        return  "<br><br>If you wish to log out from the system, follow this "
                  + component.html.doLink("link", component.getWebAddress()
                  + "?action="
                  + User.logout);
    }


}
