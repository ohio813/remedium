/*
 * The user class defines the characteristics for a user of our system.
 * It will define values such as the used IP address, the permissions inside
 * the system, amongst other features.
 *
 * This class intends to provide methods that ease the handling of tasks related
 * to users such as questioning if a given user has permission or not to access
 * a resource and also to ease editing the features for each user.
 * 
 */

package app.user;

/**
 *
 * @author Nuno Brito, 24th of July 2011 in Darmstadt, Germany
 */
public class User {

        public static final String
            // settings
            id = "user", // id of this component
            // fields
            uid = "uid",
            name = "name",
            password = "password",
            permissions = "permissions",
            parameters = "parameters",
            address = "address",
            status = "status",
            date = "date",
            // action keywords:
            action = "action",
            show = "show",
            logged = "logged",
            logout = "logout",
            doLogin = "doLogin",
            isLogged = "isLogged",
            terms = "terms",
            register = "register",
            doRegister = "doRegister",
            edit = "edit",
            doEdit = "doEdit",
            doDelete = "delete",
            doDetails = "details",
            admin = "admin";

    public static final String[] fields = new String[]{ // fields that we use
        uid, // unique identification
        name, // file name
        password, // password hash
        permissions, // admin,guest,EGOS_license
        parameters, // expires=2011/10/10:10h12 (...), registered at..
        logged, // since when is this user logged
        address // where it is coming from
    };

    public static final int // define the index of each field inside our database
            _uid = 0,
            _name = 1,
            _password = 2,
            _permissions = 3,
            _parameters = 4,
            _logged = 5,
            _address = 6,
            // special cases
            _empty = 1 // represent an empty array
            ;

        
    private String // specific to each instantiation of this class
        myUid = "", // unique identification
        myName = "guest", // file name
        myPassword = "", // password hash
        myPermissions = "public", // admin,guest,EGOS_license
        myParameters = "", // expires=2011/10/10:10h12 (...), registered at..
        myLoggedSince = "", // since when is this user logged
        myAddress = "" // where it is coming from
    ;

    /** pick a given record field with user data and adopt it */
    public void setUser(String[] record){
        setMyAddress(record[User._address]);
        setMyLoggedSince(record[User._logged]);
        setMyName(record[User._name]);
        setMyParameters(record[User._parameters]);
        setMyPassword(record[User._password]);
        setMyPermissions(record[User._permissions]);
        setMyUid(record[User._uid]);
    }

    /* Get all data from an user in the form of a string record*/
    public String[] getRecord(){
        String[] record = new String[]{
        this.getMyUid(),
        this.getMyName(),
        this.getMyPassword(),
        this.getMyPermissions(),
        this.getMyParameters(),
        this.getMyLoggedSince(),
        this.getMyAddress()
        };
        return record;
    }


    /** Verifies if this user has a given type of permission */
    public boolean hasPermission(String permission){
        // get all permissions inside an array
        String[] givenPermissions = permission.split(",");
        // default value if none is found
        boolean result = false;
        for(String givenPermission : givenPermissions)
            if(myPermissions.contains(givenPermission)){
                System.out.println("-userPermission->"+givenPermission+"-->"+permission);
                return true;
        }       
        // do we have this permission?
        //myPermissions.contains(permission);
        // output the result
        return result;
    }

    public String getMyAddress() {
        return myAddress;
    }

    public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
    }

    public String getMyLoggedSince() {
        return myLoggedSince;
    }

    public void setMyLoggedSince(String myLoggedSince) {
        this.myLoggedSince = myLoggedSince;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public String getMyParameters() {
        return myParameters;
    }

    public void setMyParameters(String myParameters) {
        this.myParameters = myParameters;
    }

    public String getMyPassword() {
        return myPassword;
    }

    public void setMyPassword(String myPassword) {
        this.myPassword = myPassword;
    }

    public String getMyPermissions() {
        return myPermissions;
    }

    public void setMyPermissions(String myPermissions) {
        this.myPermissions = myPermissions;
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid) {
        this.myUid = myUid;
    }


}
