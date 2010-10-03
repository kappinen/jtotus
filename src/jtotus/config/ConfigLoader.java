/*
    This file is part of jTotus.

    jTotus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jTotus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
*/


package jtotus.config;

/**
 *
 * @author house
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfigLoader <T> {
    private XStream xstream = null;
    private String configName = null;
    private String configDir = "config" + File.separator;

    public ConfigLoader(String config) {
        configName = config;
        xstream = new XStream(new DomDriver());

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
            retObj = (T) xstream.fromXML(fis);
        } catch (FileNotFoundException ex) {
            System.err.printf("Failure to read config:%s\n", configName);
            //Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
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

    public void applyInputsToObject(Object obj) {
        T config = this.getConfig();


        Field[] toObjectFields = obj.getClass().getDeclaredFields();
        Field[] fromObjectFields = config.getClass().getDeclaredFields();


        for(int i = 0;i<toObjectFields.length;i++) {
            String inputToName = toObjectFields[i].getName();

            //Only input starting fields are used in configuration file
            if(inputToName.startsWith("input")) {

                for(int y = 0;y<fromObjectFields.length;y++) {

                    String inputFromName=fromObjectFields[i].getName();

                    if(toObjectFields[i].getType() == fromObjectFields[i].getType() &&

                        inputFromName.compareTo(inputToName)==0) {

                        try {
                            toObjectFields[i].set(obj, fromObjectFields[i]);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            }
        }
        

    }
    
}

