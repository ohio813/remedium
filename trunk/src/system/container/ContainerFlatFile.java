/*
 * This container implementation uses flat files to store and retrieve data.
 *
 * Characteristics:
 *      - We can set the max number of records
 *      -
 *
 *
 */

package system.container;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import system.LogMessage;
import system.Message;

/**
 *
 * @author Nuno Brito, 29th of June 2011 in Darmstadt, Germany.
 */
public class ContainerFlatFile implements ContainerInterface {

    // should we output debug messages?
    private boolean debug = true;
    // maximum number of accepted records per file
    private long maxRecords = 10000;

    // objects
    // our array of files
    private ArrayList<fileRecord> storage = new ArrayList();
    // our current working file
    private fileRecord currentFile = null;
    // where our files will be located
    private File rootFolder = null;

    private String id; // identification of our files
    private String[] fields; // columns of data


    /* Public constructor */
    public ContainerFlatFile(final String title, final String[] fields,
            File rootFolder, LogMessage result){
        // preflight checks
        if(utils.text.isEmpty(title)){
            result.set(Message.ERROR, 0, "Title is empty");
            return;
        }
        if(fields == null){
            result.set(Message.ERROR, 1, "Fields are null");
            return;
            }

        // do the assignments
        this.id = title;
        this.fields = fields;
        this.rootFolder = rootFolder;
        // output a success message
        result.set(Message.ROUTINE, 2, "All done.");
    }



    public Boolean write(String[] fields) {
        return true;
    }

    public boolean delete(String field, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ArrayList<Properties> read(String field, String find) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long count() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long countBetween(long since, long until) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String webRequest(Request request, Response response) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
