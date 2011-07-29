/*
 * This class implements the Table object in the HTML protocol
 */

package system.html;

/**
 *
 * @author Nuno Brito, 24th of April 2011 in Darmstadt, Germany.
 */
public class Table extends HtmlObject{

    String
            columnTitles = "",
            columns = "",
            title = "";

    Boolean lineVisible = false;


    public Table (){

    }
    
    /** Should we display a division line or not? */
    private String getLine(){
        if(lineVisible)
            return "<HR>\n";
        else
            return "";
    }

    /** Should we display a division line or not? */
    public void setLineVisible(Boolean status){
        lineVisible = status;
        setDirty(true);
    }

    /** Set the title for this table */
    public void setTitle(String assignedTitle){
        title = "<H3>"+assignedTitle+"</H3>\n";
        setDirty(true);
    }

    /** Add another column to the table */
    public void addLine(String... columnValues){
        // iterate all column values
        String result = "";
        for(String column : columnValues)
            result = result.concat("<TD><TT>"+column+"</TT></TD>");
        // add the values to the list    
        columns = columns + "<TR>"+result+"</TR>\n";
        setDirty(true);
    }

    /** The title for each column on this table*/
    public void setColumnTitles(String... columnTitles){

        // iterate throught all listed titles
        String temp = "";
        for(String column : columnTitles)
            temp = temp.concat("<TD><B>"+column+"</B></TD>");
        // set the titles
        this.columnTitles = "<TR>"+temp +"</TR>\n";
        setDirty(true);
    }

    @Override
    String id() {
        return "TABLE";
    }

    @Override
    Boolean needToCloseTag() {
        return true;
    }

    /** Get all attributes registered for this table */
    private String getAttributes(){
        String result = "";
        // now add the contents in the middle, convert tags to expected values
        String[] tags = tagOrdering.split(divider);

        // do the actual translation
        for(String tag : tags){
            if(tagList.containsKey(tag))
                result = " "
                        +tag
                        +"=\""
                        +result.concat(tagList.getProperty(tag))
                        +"\""
                        ;
        }
        return result;
    }

    @Override
    // get the text representation of this object
    public String getText() {

        if(notDirty())
            return cache;
        else
            return    title
                + getLine()
                + "<TABLE"
                + getAttributes()
                + "><TBODY style=\"vertical-align: top;\">\n"
                + columnTitles
                + columns
                +"</TBODY></TABLE>\n";
    }

}
