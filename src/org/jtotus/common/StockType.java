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


package org.jtotus.common;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;
import org.jtotus.database.DataFetcher;




/**
 *
 * @author Evgeni Kappinen
 */
public class StockType implements Iterator{
    private String stockName=null;
    private StockNames stocks = new StockNames();
    private Iterator mapIter = null;
    private final DataFetcher fetcher = new DataFetcher();
    private Helper help=Helper.getInstance();


    public StockType() {
        mapIter = stocks.iterator();
    }
    
    public StockType(String name) {
        stockName = name;
        mapIter = stocks.iterator();
    }

    public boolean hasNext() {
        return mapIter.hasNext();
    }

    public Object next() {
        Map.Entry entry = (Map.Entry)mapIter.next();
        return (String)entry.getValue();
        
    }

    public String nextValue() {
        Map.Entry entry = (Map.Entry)mapIter.next();
        return (String)entry.getValue();

    }

   public String nextKey() {
        Map.Entry entry = (Map.Entry)mapIter.next();
        return (String)entry.getKey();

    }

    public void remove() {
        mapIter.remove();
    }


    public String getHexName(String name) {
        return stocks.getHexName(name);
    }

    public String getHexName() {
        return stocks.getHexName(stockName);
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String name) {
        stockName = name;
    }

    public BigDecimal fetchCurrentClosingPrice() {
        DateTime cal = new DateTime();
        BigDecimal retValue = null;
        help.debug("StockType", "Fetching:%s: Time:" + cal.toDate() + "\n" , stockName);

        while((retValue = fetcher.fetchClosingPrice(stockName, cal)) == null) {
            //TODO:check end
            cal = cal.minusDays(1);
        }

        return retValue;
    }

    public BigDecimal fetchClosingPrice(DateTime calendar) {

        help.debug("StockType", "Fetching:%s: Time:%s\n", stockName, calendar.toString());

        return fetcher.fetchClosingPrice(stockName, calendar);
    }

    public BigDecimal fetchClosingPrice(Date time){

        if (time==null) {
            return null;
        }
        
        DateTime cal = new DateTime(time);

        help.debug("StockType", "Fetching:%s: Time:" + time + "\n" , stockName);

        return fetcher.fetchClosingPrice(stockName, cal);
    }

    public double []fetchClosingPricePeriod(final String stockName, final DateTime startDate, final DateTime endDate) {
        return fetcher.fetchClosingPricePeriod(stockName, startDate, endDate);
    }

    public BigDecimal fetchPastDayClosingPrice(int count){
        BigDecimal tmp = null;

        DateTime cal = new DateTime().minusDays(count);
        tmp = fetcher.fetchClosingPrice(stockName, cal);
        return tmp;
    }

    public BigDecimal fetchCurrentVolume(){
       DateTime cal = new DateTime();
       BigDecimal retVolume = null;

        help.debug("StockType", "Fetching:%s: Time:" + cal.toDate() + "\n" , stockName);
        while((retVolume=fetcher.fetchVolumeForDate(stockName, cal)) == null) {
            //TODO:check end
            cal = cal.minusDays(1);
        }

        return retVolume;
    }

    public BigDecimal fetchVolume(DateTime calendar){

        return fetcher.fetchVolumeForDate(stockName, calendar);
    }
}
