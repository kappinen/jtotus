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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author kappiev
 */
public class MethodConfig implements Iterable<String>, Iterator<String>{
    private int iterPoint = 0;
    private GUIConfig config = null;
    public String []StockNames = null;
    public int day_period = 5;

    public MethodConfig() {
        ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");

        //config = new GUIConfig();
        config = loader.getConfig();
        //if config does not exists create new one
        if (config == null) {
            config = new GUIConfig();
        }
        StockNames = config.fetchStockName();
        day_period = config.day_period;
    }


    public String []fetchStockName() {
        return StockNames;
    }

    public Date getStartTime() {
        Calendar calen = Calendar.getInstance();
        calen.set(2005, 8 -1, 30);
        return calen.getTime();
    }

    public Date getEndTime() {
        Calendar calen = Calendar.getInstance();
        return calen.getTime();
    }

    public Iterator<String> iterator() {
       iterPoint = 0;
       return this;
    }

    public boolean hasNext() {
        if(iterPoint < StockNames.length) {
            return true;
        }
        
        return false;
    }

    public String next() {
        if (iterPoint < StockNames.length) {
            String ret = StockNames[iterPoint];
            iterPoint++;
            return ret;
        }
        return null;
    }

    public void remove() {
        //FIXME:add support
        return;
    }
    

}
