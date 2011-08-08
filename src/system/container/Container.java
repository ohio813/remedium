/*
 * This class links the Container abstraction with a concrete implementation.
 */

package system.container;

import java.io.File;
import system.log.LogMessage;

/**
 *
 * @author Nuno Brito, 23rd of July 2011 in Darmstadt, Germany
 */
public class Container extends ContainerFlatFile{

      // public constructor, empty for the time being.
    public Container(final String title, final String[] fields,
            File rootFolder, LogMessage result){
        // call the implementation
        super(title, fields, rootFolder, result);
    }

}
