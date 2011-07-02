/*
 * The INI container provides INI style persistent data to components.
 */

package system.container;

import java.util.ArrayList;
import java.util.Properties;
import system.msg;
import system.core.Component;

/**
 *
 * @author Nuno Brito, 28th of May 2011 in Darmstadt, Germany.
 */
public class INIcontainer {

     // where we hold the persistent data
    private Container INIcontainer;
    private Component hostComponent;

    /** the public constructor */
    public INIcontainer(Component assignedComponent){
        // inherit a component to serve as backbone
        hostComponent = assignedComponent;

        // start our own
        INIcontainer = hostComponent.createDB
                ("INI", new String[] {msg.FIELD_KEY, msg.FIELD_VALUE});
   }

    // Write INI
    public void write(String key, String value){
        this.write("", key, value);
    }

    public void write(String section, String key, String value){
        INIcontainer.write(new String[] {section + "/" + key, value});
    }


    // Read INI

    public String read(String key){
        return read("", key, "");
    }

    public String read(String section, String key){
        return read(section, key, "");
    }

    public String read(String section, String key, String defaultValue){
        // get the record
        ArrayList<Properties> result = INIcontainer.read
                (msg.FIELD_KEY, section +"/"+ key);

        // preflight check, if result is null then provide an empty string
        if(
            (result == null)
            ||
            (result.isEmpty()))
            return "";

        // get the value
        String value = result.get(0).getProperty
                (msg.FIELD_VALUE, defaultValue);
        // give back the expected value for our question
        return value;
    }



    // delete keys
    public void delete(String key){
        delete("",key);
    }

    public void delete(String section, String key){
        INIcontainer.delete(msg.FIELD_KEY, section +"/"+ key);
    }



}
