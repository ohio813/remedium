/*
 * This component manages users and respective authentications.
 *
 * The goal is to provide a service that allows users to register an account at
 * our system and then perform management actions such as changing the password
 * and managing other users if provided with administrative permissions.
 *
 * Other components should be able to ask this service if a given user is
 * registered or not. By default, we only allow one user logged per IP address
 */

package app.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
 * @author Nuno Brito, 23rd of July 2011 in Darmstadt, Germany.
 */
public class UserComponent extends Component{


    private Container
            container;  // where we store our user names and details
    private HashMap<String, Long>
            loggedUsers =  new HashMap(); // keep a record of username is logged or not


    private PageAdmin pageAdmin;  // the administration backend

    public UserComponent(Remedium assignedInstance, Component assignedFather){
        // call the super component!
         super(assignedInstance, assignedFather);
     }

            

    @Override
    public void onStart() {
        log(msg.INFO,"Starting the User component");
        // the reply object
        LogMessage result = new LogMessage();
      // create the database container
        container = new Container(User.id, User.fields,
                this.getStorage(), result);
        // Output the result from this initialization if something went wrong
        if(result.getResult() == msg.REFUSED){
            this.log(msg.ERROR, result.getRecent());
        }
        // create our page
        pageAdmin = new PageAdmin(this);

        // does our administrator exists?
        verifyAdminExists();

    }

    /** Ensure that an administrator exists on our system */
    private void verifyAdminExists(){
        // do we have an administrator?
        if(this.userExists(msg.admin)==true) // yes we do
            return; // no need to continue
        // create an administrator account
        this.authenticateRegistration(msg.admin, msg.admin, msg.admin
                +",public"); // password and user name are admin, permissions
        // are public and administration
    }

    @Override
    public void onRecover() {
    }

    @Override
    public void onLoop() {
    }

    @Override
    public void onStop() {
        log(msg.INFO,"Stopping");
    }

    @Override
    public String getTitle() {
        return User.id;
    }

    @Override
    public String doWebResponse(Request request, Response response) {


        // get the page that we want to display right now
        String doAction = utils.internet.getHTMLparameter(request, User.action);
        // holder of our page to display
        String result = "";
  // question login - question if a user is logged inside the site?
        if(doAction.equalsIgnoreCase(User.isLogged)){
            result = questionLogin(request, response);
            return "ignore";
        }

  //  handle log out - specific request to log out from the system
        if(doAction.equalsIgnoreCase(User.logout))
            result = this.processLogout(request, response);
        else
  // show logout - this only appears if we are already logged
        if(this.isLogged(request)){
            result = getTextLogout(request);
            // if we are admin, show the administration console
            result = this.pageAdmin.handleAdminUser(request, result);
            // set this text as the default
            this.html.setSection(html.SectionHome, result);
        }
        else
  // show register - are we requesting to see the registration page?
        if(doAction.equalsIgnoreCase(User.register))
            result = showRegister(request);
        else
  // handle registration - is a user trying to register?
        if(doAction.equalsIgnoreCase(User.doRegister))
            result = processRegistration(request);
        else
  // handle login - is a user trying to login?
        if(doAction.equalsIgnoreCase(User.doLogin))
            result = processLogin(request, response);
        else
  // show terms: are we requesting the terms and conditions page?
        if(doAction.equalsIgnoreCase(User.terms))
            this.html.setSection(html.SectionHome, getTextLicenseTerms());
        else{
  // show login: If we don't want anything else, just proceed with the login
            result = getTextLogin();
            result = pageAdmin.handleAdminUser(request, result);
            this.html.setSection(html.SectionHome, result);
        }

        // all done
        return result;
    }


    /** Return the details of an user based on the name */
    public User getUser(String who){
        // get the data for this specific user
        String[] record = container.read(who);
        // data verification, empty record means 'user not found'
        if(record.length == User._empty){
            return null;
        }
        // create a new user
        User user = new User();
        // get the details onto the user object
        user.setUser(record);
        // return the results
        return user;
    }

    /** Returns an array with all the users registered in our system */
    public User[] getUsers(){
        ArrayList<User> users = new ArrayList();
        // get all the records from the user database
        ArrayList<String[]> records = container.readAll();
        // iterate each record from our list
        for(String[] record : records){
            // create a new user
            User user = new User();
            // get its values from the record
            user.setUser(record);
            // add to our array
            users.add(user);
        }
        // return an array of all our users
    return users.toArray(new User[]{});
    }

     /** Process a login request */
    private String questionLogin(Request request, Response response){
        // where is this request coming from?
        String username = utils.internet.getHTMLparameter(request, User.uid);
        String result = "false";
        // is this UID empty?
        if(utils.text.isEmpty(username)){
            // try to get a memory from the used port
            String from = utils.internet.getAddress(request);
            if(this.getInstance().getLoggedAddress().containsKey(from))
                result = "true";
            }
        else
        // only output true if this user is listed on our logged menu
            if(this.loggedUsers.containsKey(username)){
                log(msg.INFO, "questionLogin: User '"
                        + username + "' is logged");
                result = "true";
        }
        // print out our reply to this request
        this.finishWebResponse(request, response, result);
        // all done
        return "ignore";
    }


    /** Process a log out request */
    private String processLogout(Request request, Response response){
        String result = "";

        // get the current user logged on our system
        User loggedUser = this.getLoggedUser(request);
        // iterate the list of current users, when one matches, delete it
        for(User user : this.getInstance().getLoggedAddress().values()){
            // check if we have a match
            if(user.getMyName().equals(loggedUser.getMyName())){
                // delete it for good
                this.getInstance().getLoggedAddress()
                        .remove(user.getMyAddress());
            }
        }

            this.html.setSection(html.SectionHome,"<h2>Logged out</h2>"
                    + "Redirecting you to the login page..");
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getWebAddress());
            result = "Logged out";
        
        return result;
    }


    /** Process a login request */
    private String processLogin(Request request, Response response){
        // the form holder
        Form rem = null;
        try {
            // get the form
          rem = request.getForm();
        } catch (IOException ex) {
            // an error occurred, exit this method
            log(msg.ERROR,"processLogin failed: Exception occured");
            this.html.setSection(html.SectionHome, "Invalid login credentials");
            return "Invalid login";
        }

        // get the user details from the form
         String remUsername = rem.get("remUsername");
         String remPassword = rem.get("remPassword");
         boolean remTerms = rem.containsKey("remTerms");

         // do the authentication of this user
         boolean hasLogged = this.authenticateUser
                 (remUsername,remPassword, remTerms, request);
         // are we successfully logged with these credentials?
         if(hasLogged){
            // we are now logged inside the system
            log(msg.COMPLETED,"processLogin: User '"+remUsername+"' is logged");
            this.html.setSection(html.SectionHome, "<h2>Valid login</h2>"
                    + "Redirecting you to the main page..");
            // redirect to the main page
            addAutoHTMLrefresh(3, this.getInstance().getMyAddress());
            return "Valid login";
         }
         // someting went wrong
         else{
            // an error occurred, exit this method
            log(msg.ERROR,"processLogin failed: Credentials are not valid");
            this.html.setSection(html.SectionHome, "<h2>Invalid login</h2>"
                    + "Redirecting you to the login page..");
            // redirect back to the component page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return "Invalid login";
         }

    // all done
    }

    /** Handle the authentication procedure for the login of a user */
    private boolean authenticateUser(String username, String password,
            Boolean terms, Request request){
        // TODO: We need to sanitize the username and password details here

        // get the data for this specific user
        String[] record = container.read(username);

        // data verification, empty record means 'user not found'
        if(record.length == User._empty){
            log(msg.ERROR,"authenticateUser failed: '"
                    + username
                    + "' does not exist");
            return false;
        }
        // has the user accepted the license terms?
        if(terms == false){
            log(msg.ERROR,"authenticateUser failed: "
                    + "License terms need to be explicitly accepted");
            return false;
        }
        // check the password
        if(password.equalsIgnoreCase(record[User._password])==false){
            log(msg.ERROR,"authenticateUser failed: "
                    + " Invalid password");
            return false;
        }

        // update the date of last login
        long since = System.currentTimeMillis();
        record[User._logged] = "" + since;
        // get the client's IP address
        String from = utils.internet.getAddress(request);
        record[User._address] = from;
        // write this value on our database
        container.write(record);
        // remember username is currently logged and from where
        loggedUsers.put(username, since);
        // create a new User
        User user = new User();
        // add all data from this user
        user.setUser(record);
        // put it in memory
        this.getInstance().getLoggedAddress().put(from, user);
        // all done!
        return true;
    }


    /** Process a register request */
    private String processRegistration(Request request){
        // the form holder
        Form rem = null;
        try {
            // get the form
          rem = request.getForm();
        } catch (IOException ex) {
            // an error occurred, exit this method
            log(msg.ERROR,"processRegister failed: Exception occured");
            this.html.setSection
                    (html.SectionHome, "<h2>Invalid registration</h2>"
                    + "Redirecting you to the registration page.."
                    );
            // redirect to the login page
            addAutoHTMLrefresh(3, this.getWebAddress()
                    +"?action=register");
            return "Invalid registration";
        }

        // get the user details from the form
         String remUsername = rem.get("remUsername");
         String remPassword = rem.get("remPassword");

         // proceed with the registration authentication
         boolean result = authenticateRegistration(remUsername, 
                 remPassword, "public");
         // react to the result
         if(result == true){
             // success in the registration
             log(msg.COMPLETED,"processRegistration: User '"+remUsername+"' is "
                     + "registered");
            this.html.setSection(html.SectionHome, "<h2>Valid registration</h2>"
                     + "Redirecting you to the main page.."
                    );


            // login this user
            this.loginUser(remUsername, request);

            // redirect to the login page
            addAutoHTMLrefresh(3, this.getWebAddress());
            return "Valid registration";
         }
         // this user was not registered
         else{
            log(msg.ERROR,"processRegistration failed");
            this.html.setSection
                    (html.SectionHome, "<h2>Invalid registration</h2>"
                    + "Redirecting you to the registration page.."
                    );
            // redirect to the login page
            addAutoHTMLrefresh(3, this.getWebAddress()
                    +"?action=register");
            return "Invalid registration";
         }
         // all done
    }


    private void loginUser(String who, Request request){
     // login a given user
            User user = this.getUser(who);
            // do the login procedure for this user
            loggedUsers.put(who, Long.parseLong(user.getMyLoggedSince()));
            // put it in memory
            this.getInstance().getLoggedAddress().put
                (utils.internet.getAddress(request), user);
    }

    /** Checks if a given user exists or not inside our system */
    private boolean userExists(String who){
         String[] newRecord = container.read(who);
         return newRecord.length != User._empty;
    }

    /** Change the records for a given user **/
    public boolean editUser(String[] record){
        // we don't verify permissions here, it is generic
        return container.write(record);
    }

    /** Delete a given user **/
    public boolean deleteUser(String who){
        // we don't verify permissions here, it is generic
        return container.delete(User.uid, who);
    }



    /** Handle the authentication procedure for registering a user */
    private boolean authenticateRegistration(String username, String password,
            String permissions){
        boolean result = false;
        // does this user already exists?
        String[] newRecord = container.read(username);
        // exit the registration if the record is not empty
        if(newRecord.length != User._empty){
            return false;
        }

        // Let's set this record with the new user details
        newRecord = new String[]{
        username, // unique identification
        username, // file name
        password, // password hash
        permissions, // admin,guest,EGOS_license
        "", // expires=2011/10/10:10h12 (...), registered at..
        ""+System.currentTimeMillis(), // since when is this user logged
        "" // where is it coming from
        };
        // write this value at our database
        result = container.write(newRecord);
        return result;
    }


    /** Return the registration */
    private String showRegister(Request request){
//        boolean loginStatus = this.isLogged(request);
//        User user = this.getLoggedUser(request);
//        System.out.println(loginStatus + "--->" + user.getMyName());
        // get our text for the page
        String result = getTextRegistration();
        // set this text as the default
        this.html.setSection(html.SectionHome, result);
        // acknowledge that we received this request
        log(msg.INFO,"Showing the user registration page");
        return result;
    }

    /** Produce the text allowing the user to log out if desired */
    private String getTextLogout(Request request){
        // get current user
        User user = this.getLoggedUser(request);
        // output our message
        String result = "<h2>Welcome back "+user.getMyName()+"!</h2>"
                  + "If you wish to log out from the system, follow this "
                  + html.doLink("link", this.getWebAddress() + "?action="
                  + User.logout)
                  ;
     return result;
}


    private String getTextLogin(){
    return
        "<h1>Login</h1>"
        +"Enter your account credentials. If you don't have an account, please"
        +" visit the <a href=\"?action=register\">registration"
        +" page</a>."
        +"<form name=\"login\" action=\"?action=doLogin\""
        +" method=\"post\"><br>"
        +"  <table style=\"text-align: left; height: 105px; width: 490px;\""
        +" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">"
        +"    <tbody>"
        +"      <tr>"
        +"        <td style=\"width: 63px;\" align=\"undefined\""
        +" valign=\"undefined\">Username</td>"
        +"        <td style=\"width: 408px;\" align=\"undefined\""
        +" valign=\"undefined\"><input maxlength=\"20\""
        +" name=\"remUsername\"></td>"
        +"      </tr>"
        +"      <tr>"
        +"        <td style=\"width: 63px;\" align=\"undefined\""
        +" valign=\"undefined\">Password</td>"
        +"        <td style=\"width: 408px;\" align=\"undefined\""
        +" valign=\"undefined\"><input maxlength=\"16\""
        +" name=\"remPassword\" type=\"password\"></td>"
        +"      </tr>"
        +"      <tr>"
        +"        <td style=\"text-align: right;\" valign=\"undefined\"><input"
        +" name=\"remTerms\""
        +" value=\"agree\""
        +" type=\"checkbox\"></td>"
        +"        <td style=\"width: 408px;\" align=\"undefined\""
        +" valign=\"undefined\">&nbsp;I agree with the <a"
        +" href=\"?action=terms\">terms and conditions</a> of usage"
        +" for this site.</td>"
        +"      </tr>"
        +"    </tbody>"
        +"  </table>"
        +"  <br>"
        +"  <input name=\"remLogin\" value=\"Login\" type=\"submit\">"
        +"</form>";
    }

    /** Returns the registration form*/
    private String getTextRegistration(){
    return
        "<h1>Registration</h1>"
        +"<form method=\"post\" action=\"?action=doRegister\""
        +" name=\"registration\"><br>"
        +"  <table style=\"text-align: left; height: 105px; width: 674px;\""
        +" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">"
        +"    <tbody>"
        +"      <tr>"
        +"        <td style=\"width: 63px;\" align=\"undefined\""
        +" valign=\"undefined\">Username</td>"
        +"        <td style=\"width: 163px;\" align=\"undefined\""
        +" valign=\"undefined\"><input maxlength=\"20\""
        +" name=\"remUsername\"></td>"
        +"        <td style=\"width: 425px;\" align=\"undefined\""
        +" valign=\"undefined\">Choose your personal username. Only "
        +"letters and numbers accepted from 4 characters up to a maximum of 20 "
        +"characters.</td>"
        +"      </tr>"
        +"      <tr>"
        +"        <td style=\"width: 63px;\" align=\"undefined\""
        +" valign=\"undefined\">Password</td>"
        +"        <td style=\"width: 163px;\" align=\"undefined\""
        +" valign=\"undefined\"><input maxlength=\"16\""
        +" name=\"remPassword\" type=\"password\"></td>"
        +"        <td style=\"width: 425px;\" align=\"undefined\""
        +" valign=\"undefined\">Choose a password between 4 and 16 "
        +"characters. Only letters and numbers are accepted.</td>"
        +"      </tr>"
        +"    </tbody>"
        +"  </table>"
        +"  <br>"
        +"  <input value=\"Register\" type=\"submit\"> </form>";
    }


    /** Get the license and terms of usage for this site */
    private String getTextLicenseTerms(){
    return
         "<h1>Terms and conditions</h1>"
        +"<br>"
        +"<table style=\"text-align: left; width: 537px; height: 64px;\""
        +" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">"
        +"  <tbody>"
        +"    <tr>"
        +"      <td align=\"undefined\" valign=\"undefined\">In "
        +"order to proceed with a successful login procedure inside the present"
        +" web site and making use of the provided services, you agree to "
        + "respect the following rules:<br>"
        +"      <ul>"
        +"        <li>Provided content and information shall not be"
        +" disclosed to other people without express consent from the"
        +" administration services of this web site</li>"
        +"        <li>Distribution of the provided software to other"
        +" machines is expressively forbidden unless authorized by the"
        +" administration services of this web site</li>"
        +"      </ul>"
        +"      </td>"
        +"    </tr>"
        +"  </tbody>"
        +"</table>";
    }
    
}
