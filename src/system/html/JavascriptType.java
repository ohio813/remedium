/*
 * This class helps to insert javascript code inside the HTML pages. For each
 * instance of this class it is possible to define functions and their
 * respective invoker code.
 */

package system.html;

/**
 *
 * @author Nuno Brito, 16th of April 2011 in Darmstadt, Germany.
 */
public abstract class JavascriptType {

    /** get the text for single use instead of combined with other JS classes*/
    public final String getText(){
        return
                " <script language=\"javascript\">"
                + getContent()
                + getInvoker()
                + "</script>";
    }

    abstract String getTitle();
    abstract String[] getArg();
    abstract String getContent();
    abstract String getInvoker();

}
