/*
 * A collection of handy JavascriptType snippets that can be added to our pages
 */

package system.html;

import system.html.JavascriptType;


/**
 *
 * @author Nuno Brito, 11th of April 2011 in Darmstadt, Germany
 */
public class JS_CloseWindowTimer extends JavascriptType{


    /** Close the current browser window
     * @param waitSeconds The number of seconds to wait before closing window
     */
    public final String closeWindow(int waitSeconds){
        String result =
                 " <script language=\"javascript\">"
                +" function CloseCurrentWindow(){"
                +"  window.opener=window.open('','_self','');"
                +"  window.opener.close();"
                +"  document.location.href=\"about:blank\";"
                +" } "
                + "setTimeout(\"CloseCurrentWindow()\","+waitSeconds*1000+");"
                + "</script>";
                return result;
    }

    @Override
    String getTitle() {
        return "CloseCurrentWindow()";
    }

    @Override
    String getContent() {
        return  " function CloseCurrentWindow(){"
                +"  window.opener=window.open('','_self','');"
                +"  window.opener.close();"
                +"  document.location.href=\"about:blank\";"
                +" } ";
    }

    @Override
    String getInvoker() {
        return "setTimeout(\"CloseCurrentWindow()\","
                +Integer.parseInt(getArg()[0])*1000
                +");";
    }

    @Override
    String[] getArg() {
        return new String[]{"3"};
    }
    
}
