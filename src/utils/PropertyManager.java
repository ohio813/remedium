/*
 *  Adapter pattern for class java.util.Properties.
 *
 *  A PropertyProvider is used to suport external/resilient data in the system.
 *  With this central tool, we can store/read the properties (such as the local
 *  Centrum's IP address) in a local file.
 */

package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter pattern for class
 *
 * @author feiteira
 */
public class PropertyManager extends Properties{
    public static String DEFAULT_MANAGER_NAME = "remedium_properties";


    private static PropertyManager self = null;

    private Properties properties = null;

    // Making the constructor private because we want a singleton
    protected PropertyManager(){
        properties = new Properties();
    };


    public static PropertyManager getPropertyProvider(){
        if (self == null) self = new PropertyManager();
        return self;
    }

    public boolean save(String name){
        try {
            this.storeToXML(new FileOutputStream(name + ".xml"), name);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean load(String name){
        try {
            loadFromXML(new FileInputStream(name + ".xml"));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
