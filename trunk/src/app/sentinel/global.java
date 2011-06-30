/*
 * This is the Global class for the Centrum application, it is used to launch
 * the roles as necessary.
 */

package app.sentinel;

import java.util.Properties;
import remedium.Remedium;

/**
 * The global definition for this application
 * @author Nuno Brito
 */
public class global {

   public boolean start(Remedium rem, Properties parameters,
           long assignedRemLock){
     
     // all done
     return true;
   }

}
