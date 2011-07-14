/*
 * This is the generic implementation of the Network class that can be used
 * statically by other components on the system. Typically you can access an
 * instance of this class at the Main.java file as "net".
 */

package system.net;

import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 11th of July 2011 in Darmstadt, Germany
 */
//public class Network extends network_simple_hsql {
    public class Network extends network_version1 {

         

    // public constructor, empty for the time being.
    public Network(Remedium instance){
        super(instance);
    }
}
