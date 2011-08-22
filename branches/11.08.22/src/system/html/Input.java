/*
 * This class provides a programatic way of creating and managing
 * submit buttons inside our system
 */

package system.html;

/**
 *
 * @author Nuno Brito, 10th of April 2011 in Darmstadt, Germany
 */
public class Input extends HtmlObject{

    protected String
            //title,
            // what type of button is this
            TYPE = "type",/* TYPE = TEXT | CHECKBOX | RADIO | PASSWORD | HIDDEN | SUBMIT|
                  * RESET | BUTTON | FILE | IMAGE */

            onClick = "", // script to run when the user clicks here
            NAME = "", // name of this button element
            VALUE = "", // the value sent with the form (ignored on IE)
            DISABLED = "", // disable this button
            ACESSKEY = "", //  shortcut key for this button
            TABINDEX = "", // tab order, integer value
            // not specific to button
            CLASS = "";// specify a class to which the button belongs



    public final String
            type_Button = "BUTTON",
            type_Submit = "SUBMIT",
            type_Reset = "RESET",
            type_Text = "TEXT",
            type_Checkbox = "CHECKBOX",
            type_Radio = "RADIO",
            type_Password = "PASSWORD",
            type_Hidden = "HIDDEN",
            type_File = "SUBMIT",
            type_Image = "IMAGE"
            ;

    protected Form // our parent form
            form = null; // to which form does this input belong to?

    /** our constructor */
    public Input(){
    }

    /** Get the text caption of the button */
    public String getName() {
        return NAME;
    }
    /** Set the text caption of the button */
    public void setName(String title) {
        NAME = title;
        setDirty(true);
    }

  
 
    @Override
    public String getText(){
        // get the text pertaining this button

        if(notDirty()) // we're not dirty, no need to do all the computing again
            return cache;

        String result = "";

        // now let's create a form, start with the head
        result += "<" + id();

        // add all our known attributes
        result += addAttribute(attValue, getVALUE());
        result += addAttribute(attType, getTYPE());
        result += addAttribute(attDisabled, getDISABLED());


        result += ">"+breakLine; // close the header;

            // update the cache
            cache = result;
            setDirty(false);
        return result;
        }


    public String getACESSKEY() {
        return ACESSKEY;
    }

    public void setACESSKEY(String ACESSKEY) {
        this.ACESSKEY = ACESSKEY;
        setDirty(true);
    }

    public String getCLASS() {
        return CLASS;
    }

    public void setCLASS(String CLASS) {
        this.CLASS = CLASS;
        setDirty(true);
    }

    public String getDISABLED() {
        return DISABLED;
    }

    /** Enable/disable the DISABLED property on inputs */
    public void setDISABLED(Boolean newState) {

        // currentState is true if the DISABLED property is not empty
        //Boolean currentState = this.DISABLED.isEmpty();

        // We do an XOR here to ensure setDirty is only called on state changes
//        setDirty((
//                ((currentState = false)
//                &&(newState == true))
//                ||
//                ((currentState = true)
//                &&(newState == false))
//                ));
        setDirty(true);

        if(newState==true)
            this.DISABLED = "disabled";
        else
            this.DISABLED = "";
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
        setDirty(true);
    }

    public String getTABINDEX() {
        return TABINDEX;
    }

    public void setTABINDEX(String TABINDEX) {
        this.TABINDEX = TABINDEX;
        setDirty(true);
    }

    public String getTYPE() {
        return TYPE;
    }

    public void setTYPE(String TYPE) {
        this.TYPE = TYPE;
        setDirty(true);
    }

    public String getVALUE() {
        return VALUE;
    }

    public void setVALUE(String VALUE) {
        this.VALUE = VALUE;
        setDirty(true);
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
        setDirty(true);
    }

    public String getOnClick() {
        return onClick;
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
        setDirty(true);
    }

    @Override
    String id() {
        return "input";
    }

    @Override
    Boolean needToCloseTag() {
        return false;
    }
   
}
