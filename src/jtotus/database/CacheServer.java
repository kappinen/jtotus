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

package jtotus.database;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

/**
 *
 * @author Evgeni Kappinen
 */
public class CacheServer {
    private HashMap <String,JCS> cacheRegions=null;
    
    public CacheServer() {
        //FIXME:maker proper configuration file and replace Helper.debug()
        org.apache.log4j.Logger logger = org.apache.log4j.LogManager.getRootLogger();
        logger.setLevel(org.apache.log4j.Level.ERROR);
        
        cacheRegions = new HashMap <String,JCS>();

    }

    private boolean createRegion(String stockName) {
        try {


            if (cacheRegions.containsKey(stockName)==false) {
                //TODO:JCS.setConfigFilename(stockName)
                cacheRegions.put(stockName, JCS.getInstance(stockName));
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

    public void putValue(String stockName, Calendar date, BigDecimal value){
        try {
            if(this.createRegion(stockName)) {
                this.getRegion(stockName).put(date.toString(), value);
            }else {
                System.err.printf("Unable to find region:%s\n",stockName);
            }
        } catch (CacheException ex) {
            Logger.getLogger(CacheServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public BigDecimal getValue(String stockName, Calendar date){
        if(this.createRegion(stockName)) {
         return (BigDecimal) this.getRegion(stockName).get(date.toString());
        }  else {
         return null;
        }
    }
    


}
