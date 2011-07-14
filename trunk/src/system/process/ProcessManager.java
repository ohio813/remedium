/*
 * This is the extension to the implementation of the Process Manager. The
 * purpose of this class is to ensure that we can produce different
 * implementations of the same Interface and then provide one single point of
 * usage for other classes that want a process manager.
 *
 * We just change the code here and the remaining components work without change
 */

package system.process;

import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 29th of March 2011 in Germany
 */
public class ProcessManager extends ProcessManagerAbstract{

    /** the constructor for this class */
    public ProcessManager(Remedium instance){
        super(instance); // call the class above this one
    }

}
