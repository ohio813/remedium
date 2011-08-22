/*
 * Basic characteristics that are shared across HTML objects
 */

package system.html;

import java.util.Properties;

/**
 *
 * @author Nuno Brito, 12th of April 2011 in Darmstadt, Germany
 */
public abstract class HtmlObject {

    public final String
            // attribute titles
            attType = "type",
            attOnClick = "onClick",
            attName = "name",
            attValue = "value",
            attDisabled = "disabled",
            attAcesskey = "acesskey",
            attTabIndex = "tabindex",
            attClass = "class",
            attMethod = "method",
            attEncType = "enctype",
            attAction = "action",
            attTarget = "target",
            attOnSubmit = "onSubmit",
            attOnReset = "OnReset",
            // define the expected keywords for each attribute (if applicable)
            method_GET = "GET",
            method_POST = "POST",

            enc_Multipart = "multipart/form-data",
            enc_Application = "application/x-www-form-urlencoded",
            enc_Text = "text/plain",

            target_Blank = "_blank",
            target_Parent = "_parent",
            target_Self = "_self",
            target_Top = "_top",





            divider = "<%!%>", // the symbol for dividing tags
            breakLine = "\n"; // make the text readable to humans

    private Boolean // dirty means that changes were made and need recalculation
            dirty;  // otherwise serve the cached version in a faster manner

    protected Properties // for each tag on the tag ordering, this objects keep the value
            tagList = new Properties();

    protected String
            tagOrdering = "", // the object ordering inside the form
            cache; // used to cache the last outputted HTML text



    abstract String id(); // how is this object identified
    abstract Boolean needToCloseTag(); // is it single tag or double one?
     /** gets an HTML version of this form, ready to be published online */
    abstract public String getText();


    /**
     * Add a new portion of code onto our page tagOrdering.
     * @param tag The unique tag that allows us to identify this object
     * @param tagOrdering The tagOrdering that will be introduced at runtime
     * @return True if the tag was added to the form
     */
    public boolean add(String tag, String newContent){
        // Add this tag to our properties object
        tagList.setProperty(tag, newContent);
        // Add this tag to our ordering if the tag doesn't exist already
        if(tagOrdering.contains(tag)==false)
               tagOrdering += divider+tag;
        // set the need to recalculate our HTML object
        setDirty(true);
        return true;
    }


    /** mention that this component needs to recalculate itself */
    protected void setDirty(Boolean state){
        dirty = state;
    }

    /** are we dirty or not? */
    protected Boolean notDirty(){
        return dirty == false;
    }

    /** Add attributes to the outputted HTML text */
    protected String addAttribute(String title, String value){
        String result = "";
        if(value.length()>0)
            result += " "+title+"=\""+value+"\"";
        return result;
    }

}
