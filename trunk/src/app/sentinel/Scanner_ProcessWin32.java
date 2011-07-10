/*
 * This class is intended to process a given Windows executable file.
 * It will extract details such as version, author and native language.
 * These details are then stored inside a container. This container needs
 * to be setup when the Scanner_ProcessWin32 class is instantiated.
 */

package app.sentinel;

import system.container.Box;
import system.core.Component;
import java.io.File;
import java.util.ArrayList;
import system.container.Container;
import system.msg;
import system.raw.image_win_executable;

/**
 *
 * @author Nuno Brito, 26th of March 2011 in Germany
 */
public class Scanner_ProcessWin32 implements msg{

 // definitions

    private ArrayList<String>
            accepted_extensions = new ArrayList<String>();


 // objects
    private Container
           container; // the container of Windows related information

    private Component
            component; // the component with whom this class is associated

    /**
     * Public constructor. We need to create a database container with our
     * required fields. We will need to be associated with a given Role object
     * to use the create container features.
     */
    Scanner_ProcessWin32(IndexerComponent assignedComp,
            Box box) {
 //preflight checks
        if(assignedComp == null){
            System.out.println("ProcessWin32 can't be assigned to a null "
                    + "component");
            return;
        }
       
        // all checks done, let's create this class

        component = assignedComp;

        // create the filters
        accepted_extensions.add(".exe");
        accepted_extensions.add(".dll");
        accepted_extensions.add(".ocx");
        accepted_extensions.add(".drv");
        accepted_extensions.add(".sys");
        accepted_extensions.add(".msi");

        // create the container
         container = box.add(component.createDB(TABLE_FILE_WIN32,
                   new String[]{
            // for some reason this needs to be unique, so we add a 32
                                FIELD_REFERENCE+32,
                                FIELD_UPDATED,
                                FIELD_WIN32_VERSION,
                                FIELD_WIN32_LANG,
                                FIELD_WIN32_ARCH,
                                FIELD_DATE_CREATED,
                                FIELD_ID_SERIAL
                               })
                               );


        log(ROUTINE, "Ready to process Win32 files");
    }


    /** 
     * Read the details of a given file if it is related to Windows and index
     * these details on the Win32 container
     */
    public void index(File file, String reference, String when){

        // the file extension
        int extPosition = file.getAbsolutePath().lastIndexOf(".");

        if(extPosition <= 0) // this one doesn't even has an extension
            return;

        // grab the extension
        String extension = 
                file.getAbsolutePath().substring(extPosition).toLowerCase();

        // check if we should index it or not
        if( (utils.text.isEmpty(extension)==false)
           && (accepted_extensions.contains(extension) == false))
            return;

        // can we read this file?
        if(file.canRead()== false){
            component.log(ERROR, "Win32 index: Can't read '"
                    + file.getAbsolutePath()
                    + "'");
            return;
        }

                image_win_executable win_exe;
                win_exe = new image_win_executable();

                String target = file.getAbsolutePath();

                file = null;

                // read the version of this file;
              try{
                win_exe.read(target);
                }catch (Exception e){
                    return;
                }
                
                // if it is null, exit
                if(win_exe == null)
                    return;

                // if there is no version, there is no point in proceeding
                if(win_exe.hasVersion()==false)
                    return;

                String version = win_exe.getVersion();
                String lang = win_exe.getLanguage();
                String arch = win_exe.getArchitecture();

        log(INFO,"Indexed "+target
                +" version="+version
                +" lang="+lang
                +" arch="+arch
                );


        // write this data to the container
        component.writeDB(
                container, new String[]{
                    reference,
                    when, // updated date
                    version,
                    lang,
                    arch,
                    when, // first index date
                    component.getIDserial()
        });


       
    }
   
    private void log(int gender, String message){
        component.log(//lock,
                gender, message);
    }

}
