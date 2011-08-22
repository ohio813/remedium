/*
 * This class provides a programatic way of creating and managing
 * submit buttons inside our system
 */

package system.html;

/**
 *
 * @author Nuno Brito, 10th of April 2011 in Darmstadt, Germany
 */
public final class Button extends Input{

    // optional values
     private String
             www;  // we might desire a single button to visit this value

    /** our constructor */
    public Button(String title, String address){
        super();
        setTYPE(this.type_Submit);
        setVALUE(title);
        setTarget(address);
        setDirty(true);
    }

    /** set the target web address */
    public void setTarget(String newwww) {
        this.www = newwww;
        setDirty(true);
    }

    /** Do a single action button. This is possible when 
        creating a dummy form just for our button*/
    @Override
    public String getText(){
         if(notDirty()) // we're not dirty, no need to do all the computing again
            return cache;
        // Create an empty form that only contains our button
        form = new Form();
        form.setMETHOD(form.method_POST);
        form.setACTION(www);
        // name of the button
        setNAME(NAME);
        // add the button onto the form
        form.add(this.getVALUE(), super.getText());
        // output the result
        cache = form.getText();

        return cache;
    }


}
