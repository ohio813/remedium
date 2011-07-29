/*
 * This class adds support for the meta tags of the HTML protocol
 */

package system.html;

/**
 *
 * @author Nuno Brito, 12th of April 2011 in Darmstadt, Germany.
 */
public class Meta extends HtmlObject{

    @Override
    String id() {
        return "meta";
    }

    @Override
    Boolean needToCloseTag() {
        return false;
    }

    @Override
    public String getText() {
        if(notDirty()) // we're not dirty, no need to do all the computing again
            return cache;

        String result = "";

        // now let's create a form, start with the head
        result += "<" + id();

        // now add the contents in the middle, convert tags to expected values
        String[] tags = tagOrdering.split(divider);
        // do the actual translation
        for(String tag : tags){
            if(tagList.containsKey(tag))
                result = result.concat(
                        " "
                        + tag
                        +"=\""
                        + tagList.getProperty(tag)
                        +"\"");
        }
        // finish things up
        result += " />"+breakLine;

            // update the cache
            cache = result;
            setDirty(false);
        return result;
    }

}
