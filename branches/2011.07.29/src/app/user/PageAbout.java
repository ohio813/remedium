/*
 * This class defines the text of the About page for the user component.
 */

package app.user;

/**
 *
 * @author Nuno Brito, 26th of July 2011 in Darmstadt, Germany
 */
public class PageAbout {

    public String getText(){
    return
        "<h1>User component</h1>"
        +"This component provides other components with a service of user"
        +" authentication. It is used to ensure that other components can decide"
        +" the features that should be provided to users of this system.<br>"
        +"<br>"
        +"<br>"
        +"<h2>Tags</h2>"
        +"To define user permissions, we use tags that describe the different"
        +" permissions that a given user holds. By default, users&nbsp;not"
        +" logged inside the system are provided with a tag of \"public\". For"
        +" example, if a given component makes available a service only for users"
        +" with tag \"private\", then we ensure that only users holding this tag on"
        +" the properties of their account are allowed to use the service.<br>"
        +"<br>"
        +"Tags are not hierarchical nor recurrent. If you are assigning a tag to"
        +" a folder inside a file server, these permissions do no propagate"
        +" automatically to all files and sub folders. <br>"
        +"<br>"
        +"<h2>Support</h2>"
        +"If you encounter any flaw or defect that should be corrected, please"
        +" send a message to <a href=\"mailto:mail@nunobrito.eu\">mail@nunobrito.eu</a><br>";
    }

}
