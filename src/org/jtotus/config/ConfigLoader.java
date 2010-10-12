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
package org.jtotus.config;

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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfigLoader<T> {

    private XStream xstream = null;
    private String configName = null;
    private String configDir = "config" + File.separator;

    public ConfigLoader(String config) {
        configName = configDir + config;


        if (config != null && configName.lastIndexOf(File.separator) != -1) {
            File dir = new File(configName.substring(0, configName.lastIndexOf(File.separator)));

            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        xstream = new XStream(new DomDriver());

    }

    public boolean configDirExists() {

        File dir = new File(configDir);


        if (!dir.exists()) {
            dir.mkdirs();
            System.err.printf("directory %s does not exists\n", configDir);
            return true;
        }

        if (!dir.isDirectory()) {
            System.err.printf("%s is not directory\n", configDir);
            return false;
        }

        dir = null;
        return true;
    }

    public boolean writeObj(Object obj, String path) {

        FileOutputStream fos = null;
        if (!this.configDirExists()) {
            return false;
        }

        try {
            fos = new FileOutputStream(path, false);

            xstream.toXML(obj, fos);
            fos.flush();
        } catch (IOException ex) {
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

    public boolean storeConfig(T saveObj) {
        return writeObj(saveObj, configName + ".xml");
    }

    public T getConfig() {
        return readObj(configName + ".xml");
    }

    public void applyInputsToObject(Object obj) {

        T config = this.getConfig();
        if (config == null) {
            return;
        }

        Field[] toObjectFields = obj.getClass().getSuperclass().getDeclaredFields();
        Field[] fromObjectFields = config.getClass().getDeclaredFields();

        System.out.printf("Copying config from:%s to:%s\n",config.getClass().getName(), obj.getClass().getName());

        for (int to = 0; to < toObjectFields.length; to++) {
            String inputToName = toObjectFields[to].getName();

            //Only input starting fields are used in configuration file
            if (inputToName.startsWith("input")) {

                for (int from = 0; from < fromObjectFields.length; from++) {

                    String inputFromName = fromObjectFields[from].getName();

                    if (toObjectFields[to].getType() == fromObjectFields[from].getType()
                            && inputFromName.compareTo(inputToName) == 0) {

                        try {
                            if (toObjectFields[to].getType() == Calendar.class
                                    && inputToName.compareTo("inputEndingDate") == 0) {
                                Calendar currentDate = Calendar.getInstance();
                                toObjectFields[to].set(obj, currentDate);
                                continue;
                            }

                            toObjectFields[to].set(obj, fromObjectFields[from].get(config));

                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }

                    }
                }
            }
        }

        toObjectFields = obj.getClass().getDeclaredFields();
        fromObjectFields = config.getClass().getDeclaredFields();
        for (int to = 0; to < toObjectFields.length; to++) {
            System.out.printf("Found in class obj:%s type:%s\n",
                    toObjectFields[to].getName(), toObjectFields[to].getType().getName());
        }
        for (int to = 0; to < toObjectFields.length; to++) {
            String inputToName = toObjectFields[to].getName();
            //Only input starting fields are used in configuration file
            if (inputToName.startsWith("input")) {
                for (int from = 0; from < fromObjectFields.length; from++) {
                    String inputFromName = fromObjectFields[from].getName();

                    if (toObjectFields[to].getType() == fromObjectFields[from].getType()
                            && inputFromName.compareTo(inputToName) == 0) {
                        try {
                            if (toObjectFields[to].getType() == Calendar.class
                                    && inputToName.compareTo("inputEndingDate") == 0) {
                                Calendar currentDate = Calendar.getInstance();
                                toObjectFields[to].set(obj, currentDate);
                                continue;
                            }
                            toObjectFields[to].set(obj, fromObjectFields[from].get(config));

                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }

                    }
                }
            }
        }


    }
}
