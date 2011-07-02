/*
 * This is the interface for our database_interface. We are basing our databases on
 * the JDBC connector. This allows to use other database_interface systems in the future
 * without needing to change any code as long as all the implementation follow
 * the guidelines set on this interface.
 * 
 */

package system;

import java.sql.ResultSet;
import java.util.Properties;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito
 */
public interface database_interface extends msg{


// start the database using the default settings
    public boolean start();
/** Start the database using customized parameters
 *  Supported parameters:
 *      - DIR - Set the directory where work files are expected to be placed
 */
    public boolean start(Properties parameters);

// stop the msg queue, flush all messages
    public boolean stop();
    public boolean stop(Properties parameters);

// called before starting the database_interface system
    public boolean start_before();

// called after starting the database_interface system
    public boolean start_after();

// called before stopping the database_interface system
    public boolean stop_before();

// called after stopping the database_interface system
    public boolean stop_after();

// use for SQL command SELECT
    public boolean query(String expression);

// use for SQL commands CREATE, DROP, INSERT and UPDATE
    public String update(String expression);

// this is called to handle the resulting set after a query
    public boolean dump(ResultSet rs);

// check if the database_interface server has already started or not
    public boolean hasStarted();

    public void setRemedium(Remedium remedium);

}
