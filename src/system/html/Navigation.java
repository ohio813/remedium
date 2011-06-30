/*
 * This class handles all the management of the navigation links.
 */

package system.html;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Nuno Brito, 09th of April 2011 in Darmstadt, Germany.
 */
public class Navigation extends HtmlObject
{
 // list all our registered links
    private Hashtable<String, Link>
            links = new Hashtable();

    public Navigation(){

    }

    /** Adds a new www to our navigationContent list,
     uses the indexName as default indexer */
    public Boolean addLink(String indexName, String assignedTitle,
            String assignedLink,
            Boolean selected){
        // preflight checks
        if(  assignedTitle == null
          || indexName==null
          || assignedLink==null
          || links.containsKey(indexName))
            return false;

        
        Link link = new Link(assignedTitle, assignedLink);
        link.active = selected; // are we selected or not?
        // place link at our list
        links.put(indexName, link);
        // add this to our ordering list
        add(indexName, assignedTitle);
        // set the cache as dirty
        setDirty(true);
        // return true if the link is placed on the list
        return links.containsKey(indexName);
    }


    /** Outputs a navigationContent bar based on the registered links */
    public String getSubLinks(){

        if(notDirty()) // If there have been no changes, just provide the cache
            return cache;

        // there have been changes, let's compute everything again.
        String result = "";

        // now add the contents in the middle, convert tags to expected values
        String[] tags = tagOrdering.split(divider);

        // do the actual translation
        for(String tag : tags)
            if(tagList.containsKey(tag)){
             //   result = result.concat(links.get(tag).);
                Link link = (Link) links.get(tag);

                String linkText = "";

                   if(link.active) // active means highlighted
                    linkText = "<a href=\""+link.www+"\" class=\"active\">"
                    +link.title+"</a>";
                   else // do the link
                    linkText = "<a href='"+link.www+"'>"+link.title+"</a>";
                    // add this result to our breadcrumb
                    result = result.concat(
                    "<li>"+
                    linkText
                    +"</li>"
                    );
            }


        // update our cache with this result
        cache = result;
        this.setDirty(false);
        // output the result
        return result;
    }

/** Outputs a navigationContent bar based on the registered links */
    public String getNavLinks(){

       if(notDirty()) // If there have been no changes, just provide the cache
            return cache;

        String result = "";

        // now add the contents in the middle, convert tags to expected values
        String[] tags = tagOrdering.split(divider);

        // do the actual translation
        for(String tag : tags)
            if(tagList.containsKey(tag)){
             //   result = result.concat(links.get(tag).);
                Link link = (Link) links.get(tag);

                String linkText = "";

                   if(link.active) // active means highlighted
                    linkText = "<strong>"+link.title+"</strong>";
                   else // do the link
                    linkText = "<a href='"+link.www+"'>"+link.title+"</a>";
                    // add this result to our breadcrumb
                    result = result.concat(linkText+"&nbsp>&nbsp");
            }

        // remove the last > that is just redundant
        result += "##"; // not elegant, but it is functional
        result = result.replace("&nbsp>&nbsp"+"##", "");
        // update our cache with this result
        cache = result;
        this.setDirty(false);
        // return the output
        return result;
    }




        /** Outputs a navigationContent bar based on the registered links */
    public Boolean setSelected(String who){
        // preflight checks


        if(  who == null
          //|| links.containsKey(who)==false
          )
            return false;
        // start the action

        Enumeration em = links.elements();
        // iterate and update the active status
        while(em.hasMoreElements()){
            Link link = (Link) em.nextElement();
            Boolean originalState = link.active;
            link.active = tagList.getProperty(who).equalsIgnoreCase(link.title);

            // check if we are now dirty or not
            int changed = originalState.compareTo(link.active);
            if(changed != 0)
                setDirty(true);
        }
        return true;
    }

    @Override
    String id() { // how do we define this object?
        return "div id=\"header\"";
    }

    @Override
    Boolean needToCloseTag() {
        return true;
    }

    @Override
    public String getText() { // provides a text version of our object
        return "";
    }

}
