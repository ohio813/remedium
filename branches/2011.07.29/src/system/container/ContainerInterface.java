/*
 * This interface defines the interactions between the Container classes
 * implementing this interface and other classes who require the Container
 * functionality.
 *
 */

package system.container;

import java.util.ArrayList;
import java.util.Properties;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 *
 * @author Nuno Brito, 29th of June 2011 in Darmstadt, Germany
 */
public interface ContainerInterface {

   /** This method puts a given file record onto our database */
    public Boolean write(String[] fields);

    /** This method puts a given file record onto our database */
    public boolean delete(String field, String key);

    /** Retrieves a list of records that match a given field and value */
    public ArrayList<Properties> read(String field, String find);

     /** count the number of records available on this container */
    public long count();

    /** Count all records published between a given time interval. */
    public long countBetween(long since, long until);

    /** Get the public store name */
    public String getName();

    /** Get the public store name */
    public String[] getFields();

    /** Is this container running or not? */
    public boolean isRunning();

     /** Stop our service */
    public boolean stop();

    /** Process incoming web requests.
     * Supported parameters:
     *      - count
     *      - sync
     */
    public String webRequest(Request request, Response response);
}
