
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.config;

/**
 *
 * @author house
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author house
 */
public class ConfigLoader <T> {
    private XStream xstream = null;
    private String configName = null;
    private String configDir = "config/";

    public ConfigLoader(String config) {
        configName = config;
        xstream = new XStream();

    }


    public boolean configDirExists(){

        File dir = new File(configDir);


        if (!dir.exists()) {
            dir.mkdirs();
            return true;
        }

        if (!dir.isDirectory()) {
            System.err.printf("%s is not directory\n", configDir);
            return false;
        }

        dir = null;
        return true;
    }


    public boolean writeObj(Object obj, String path)  {

        FileOutputStream fos = null;

        if (!configDirExists()) {
            return false;
        }
        
        try {
            fos = new FileOutputStream(path);
            xstream.toXML(obj, fos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public T readObj(String path) {
        FileInputStream fis = null;
        T retObj = null;

        if (!configDirExists()) {
            return null;
        }
        
        try {
            fis = new FileInputStream(path);
            xstream.fromXML(fis, retObj);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return retObj;
    }


    public boolean storeConfig(T saveObj){
        return writeObj(saveObj, configDir + configName + ".xml");
    }
    public T getConfig(){
        return readObj(configDir + configName + ".xml");


    }

    
}

