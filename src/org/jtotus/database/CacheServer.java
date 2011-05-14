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
package org.jtotus.database;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Evgeni Kappinen
 */
public class CacheServer {

    private HashMap<String, JCS> cacheRegions = null;
    private static CacheServer cache = null;

    protected CacheServer() {
        //FIXME:maker proper configuration file and replace Helper.debug()
        org.apache.log4j.Logger logger = org.apache.log4j.LogManager.getRootLogger();
        logger.setLevel(org.apache.log4j.Level.ERROR);

//        System.out.printf("Creating hAHSMAP!!!!!\n");
        cacheRegions = new HashMap<String, JCS>();

    }

    public synchronized static CacheServer getInstance() {
        if (cache == null) {
            cache = new CacheServer();
        }
        return cache;
    }

    private synchronized boolean createRegion(String stockName) {
        try {


            if (cacheRegions.containsKey(stockName) == false) {
                //   System.out.printf("Creating region:%s\n",stockName);
                //TODO:JCS.setConfigFilename(stockName)
                JCS cacheRegion = JCS.getInstance(stockName);
                cacheRegion.clear();
                cacheRegions.put(stockName, cacheRegion);
            }

        } catch (CacheException ex) {
            Logger.getLogger(CacheServer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private JCS getRegion(String stockName) {
        return cacheRegions.get(stockName);
    }

    private String getKeyValue(DateTime cal) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMddyyyy");
        String newKey = formatter.print(cal);
        return newKey;
    }

    public synchronized void putValue(String stockName, DateTime date, BigDecimal value) {
        try {
            if (this.createRegion(stockName)) {
                //     System.out.printf("Putting "+date.getTime()+"value:%d key:%s\n",value.intValue(), this.getKeyValue(date));
                this.getRegion(stockName).put(this.getKeyValue(date), value);
            } else {
                System.err.printf("Unable to find region:%s key:%s\n", stockName, this.getKeyValue(date));
            }
        } catch (CacheException ex) {
            Logger.getLogger(CacheServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public synchronized BigDecimal getValue(String stockName, DateTime date) {
        if (this.createRegion(stockName)) {
            BigDecimal ret = (BigDecimal) this.getRegion(stockName).get(this.getKeyValue(date));
            //  System.out.printf("gettng value:%s key:%s\n",ret != null ?ret.toString() : "null", this.getKeyValue(date));
            return ret;
        } else {
            return null;
        }
    }
}
