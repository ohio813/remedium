/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package system.html;

/**
 *
 * @author Nuno Brito, 10th of April 2011 in Darmstadt, Germany.
 */
public class Link {

    public String
            title,
            family,
            www;

    public Boolean
            active = false;


    public Link(String assignedTitle, String assignedLink){
        this.title = assignedTitle;
        this.www = assignedLink;
    }

    

}
