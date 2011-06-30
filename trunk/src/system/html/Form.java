/*
 * Implements the Form class from the HTML protocol
 */

package system.html;

/**
 *
 * @author Nuno Brito, 11th of April 2011 in Darmstadt, Germany.
 */
public final class Form extends HtmlObject{

    private String 
       // attributes supported by Form
            ACTION = "", // URL of the CGI program

       // how to transfer the data. GET|POST http://goo.gl/BK9a1
            METHOD = "",

            NAME = "", // name of this form
       //  what type of form is this
            ENCTYPE = "",
            // "multipart/form-data"  (for file uploads)
            // "application/x-www-form-urlencoded" (for all the rest)
            // "text/plain" Simple text. More details at http://goo.gl/VthdN

            TARGET = "", // what frames to put the results in
            // TARGET = "_blank" | "_parent" | "_self" | "_top" | frame name
            // more details at http://goo.gl/vN3KX

            onSubmit = "", // script to run before the form is submitted
            onReset = "" // script to run before the form is reset
            ;

    /** The public constructor of this class */
    public Form(){
        // create default settings
        setMETHOD(method_POST);
        setDirty(true);
    }


         /** gets an HTML version of this form, ready to be published online */
    @Override
    public String getText(){

        if(notDirty()) // we're not dirty, no need to do all the computing again
            return cache;

        String result = "";

        // now let's create a form, start with the head
        result += "<" + id();

        // add all our known attributes

        result += addAttribute(attMethod, getMETHOD());
        result += addAttribute(attAction, getACTION());
        

        result += ">"+breakLine; // close the header;

        // now add the contents in the middle, convert tags to expected values
        String[] tags = tagOrdering.split(divider);

        // do the actual translation
        for(String tag : tags){
            if(tagList.containsKey(tag))
                result = result.concat(tagList.getProperty(tag));
        }
        // finish things up
        if(needToCloseTag())
            result += "</" + id() +">"+breakLine;


            // update the cache
            cache = result;
            setDirty(false);
        return result;
    }



    public String getACTION() {
        return ACTION;
    }

    public void setACTION(String newACTION) {
        this.ACTION = newACTION;
        setDirty(true);
    }

    public String getENCTYPE() {
        return ENCTYPE;
    }

    public void setENCTYPE(String ENCTYPE) {
        this.ENCTYPE = ENCTYPE;
        setDirty(true);
    }

    public String getMETHOD() {
        return METHOD;
    }

    public void setMETHOD(String METHOD) {
        this.METHOD = METHOD;
        setDirty(true);
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
        setDirty(true);
    }

    public String getTARGET() {
        return TARGET;
    }

    public void setTARGET(String TARGET) {
        this.TARGET = TARGET;
        setDirty(true);
    }

    public String getOnReset() {
        return onReset;
    }

    public void setOnReset(String onReset) {
        this.onReset = onReset;
        setDirty(true);
    }

    public String getOnSubmit() {
        return onSubmit;
    }

    public void setOnSubmit(String onSubmit) {
        this.onSubmit = onSubmit;
        setDirty(true);
    }

    @Override
    String id() {
        return "form";
    }

    @Override
    Boolean needToCloseTag() {
       return true;
    }
    
}
