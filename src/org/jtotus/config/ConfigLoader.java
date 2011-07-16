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

import java.io.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfigLoader<T> {

    private XStream xstream = null;
    private String configPathToFile = null; //config full name
    private String patchToCofigDir = null;    //config dir name
    private boolean debug = false;
    private final static String home = System.getProperty("user.home").toLowerCase();
    

    public String getPathToConfigDir() {
        
        if (this.patchToCofigDir == null) {
            this.patchToCofigDir = home + File.separator + ".jtotus" +File.separator+ "config" + File.separator;
        }

        return patchToCofigDir;
    }
    

    public ConfigLoader(String file) {
        configPathToFile = getPathToConfigDir() + file;

        xstream = new XStream(new DomDriver());
    }

    private boolean configDirExists(String fileName) {

        if (fileName != null && fileName.lastIndexOf(File.separator) != -1) {
            File dir = new File(fileName.substring(0, fileName.lastIndexOf(File.separator)));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            dir = null;
        }
        return true;
    }

    public boolean writeObj(Object obj, String fileName) {
        RandomAccessFile file = null;

        if (!this.configDirExists(fileName)) {
            return false;
        }

        try {
            file = new RandomAccessFile(fileName, "rw");
            String xml = xstream.toXML(obj);
            file.writeUTF(xml);
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                file.close();
            } catch (IOException ex) {
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return true;
    }

    public T readObj(String fileName) {
        RandomAccessFile file = null;
        T retObj = null;

        if (!configDirExists(fileName)) {
            return null;
        }

        try {
            file = new RandomAccessFile(fileName, "rw");
            if (debug) {
                System.out.printf("ConfigLoader reading:%s\n", fileName);
            }
            retObj = (T) xstream.fromXML(file.readUTF());

        } catch (IOException ex) {
            System.err.printf("Warning: failed to read config:%s : %s -- %s\n", configPathToFile, fileName, getPathToConfigDir());
            return null;
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (retObj != null && retObj instanceof ConfPortfolio) {
            ConfPortfolio config = (ConfPortfolio) retObj;
            if (config.useCurentDayAsEndingDate) {
                config.inputEndingDate = new DateTime();
            }
        }

        return retObj;
    }

    public boolean storeConfig(T saveObj) {
        return writeObj(saveObj, configPathToFile + ".xml");
    }

    public T getConfig() {
        return readObj(configPathToFile + ".xml");
    }

    public void applyInputsToObject(Object obj) {

        T config = this.getConfig();
        if (config == null) {
            return;
        }

        Field[] toObjectFields = obj.getClass().getSuperclass().getDeclaredFields();
        Field[] fromObjectFields = config.getClass().getDeclaredFields();

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
