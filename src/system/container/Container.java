/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package system.container;

import java.io.File;
import system.log.LogMessage;

/**
 *
 * @author Nuno Brito
 */
public class Container extends ContainerFlatFile{

      // public constructor, empty for the time being.
    public Container(final String title, final String[] fields,
            File rootFolder, LogMessage result){
        // call the implementation
        super(title, fields, rootFolder, result);
    }

}
