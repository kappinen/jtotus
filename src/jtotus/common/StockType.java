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


package jtotus.common;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import jtotus.database.DataFetcher;
import jtotus.gui.graph.GraphPacket;




/**
 *
 * @author Evgeni Kappinen
 */
public class StockType implements Iterator{
    private String stockName=null;
    private StockNames stocks = new StockNames();
    private Iterator mapIter = null;
    private final DataFetcher fetcher = new DataFetcher();
    private Helper help=null;


    public StockType(String name) {
        stockName = name;
        help = Helper.getInstance();

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

    public String getName() {
        return stockName;
    }

    public BigDecimal fetchCurrentClosingPrice() {
        Calendar cal = Calendar.getInstance();

        help.debug("StockType", "Fetching:%s: Time:" + cal.getTime() + "\n" , stockName);

        while(fetcher.fetchClosingPrice(stockName, cal) == null) {
            //TODO:check end
            cal.add(Calendar.DATE, -1);
        }

        return fetcher.fetchClosingPrice(stockName, cal);
    }

    public BigDecimal fetchClosingPrice(Calendar calendar) {

        help.debug("StockType", "Fetching:%s: Time:%s\n", stockName, calendar.toString());

        return fetcher.fetchClosingPrice(stockName, calendar);
    }

    public BigDecimal fetchClosingPrice(Date time){

        if (time==null) {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);

        help.debug("StockType", "Fetching:%s: Time:" + time + "\n" , stockName);

        return fetcher.fetchClosingPrice(stockName, cal);
    }

    public BigDecimal fetchPastDayClosingPrice(int count){
        BigDecimal tmp = null;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, count*-1);

        tmp = fetcher.fetchClosingPrice(stockName, cal);
        
        return tmp;
    }

    public GraphPacket fetchPastDayClosingPricePacket(int count){
        GraphPacket packet = null;
        BigDecimal tmp = null;

        Calendar cal = Calendar.getInstance();
        
        cal.add(Calendar.DAY_OF_MONTH, count*-1);

        tmp = fetcher.fetchClosingPrice(stockName, cal);

        if (tmp != null) {
            packet = new GraphPacket();
            packet.seriesTitle = stockName;
            packet.day = cal.get(Calendar.DATE);
            packet.month = cal.get(Calendar.MONTH) + 1;
            packet.year = cal.get(Calendar.YEAR);
            packet.result = tmp.floatValue();
        }

        return packet;
    }

    public BigDecimal fetchCurrentVolume(){
       Calendar cal = Calendar.getInstance();
       BigDecimal retVolume = null;

        help.debug("StockType", "Fetching:%s: Time:" + cal.getTime() + "\n" , stockName);
        System.out.printf("!!!!STOCKTYPE Searching for volume:!\n");
        while((retVolume=fetcher.fetchVolumeForDate(stockName, cal)) == null) {
            //TODO:check end
            cal.add(Calendar.DATE, -1);
        }

        return retVolume;
    }

    public BigDecimal fetchVolume(Calendar calendar){

        return fetcher.fetchVolumeForDate(stockName, calendar);
    }
}
