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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import jtotus.database.DataFetcher;
import jtotus.gui.graph.GraphPacket;




/**
 *
 * @author kappiev
 */
public class StockType implements Iterator{
    private String stockName=null;
    private Map<String,String> stockMap = new HashMap<String,String>();
    private Iterator mapIter = null;
    private DataFetcher fetcher = null;
    private Helper help=null;


    public StockType(String name) {
        stockName = name;
        help = Helper.getInstance();
        fetcher = new DataFetcher();

        //Aliases
        stockMap.put("Fortum Oyj", "FUM1V.HE");
        stockMap.put("Fortum Oyj", "FUM1V.HSE");
        
        stockMap.put("Nokia Oyj", "NOK1V.HSE");

        stockMap.put("UPM-Kymmene Oyj","UPM1V.HSE");
        stockMap.put("Rautaruukki Oyj","RTRKS.HSE");

        stockMap.put("Sanoma Oyj","SAA1V.HSE");
        stockMap.put("Tieto Oyj","TIE1V.HSE");
        stockMap.put("Metso Oyj","MEO1V.HSE");

        stockMap.put("KONE Oyj","KNEBV.HSE");
        stockMap.put("Konecranes Oyj","KCR1V.HSE");
        stockMap.put("Kemira Oyj","KRA1V.HSE");
        stockMap.put("Uponor Oyj","UNR1V.HSE");
        stockMap.put("Stora Enso Oyj A","STEAV.HSE");



       Set entries = stockMap.entrySet();
       mapIter = entries.iterator();
        
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
        return stockMap.get(name);
    }

    public String getHexName() {
        return stockMap.get(stockName);
    }

    public String getName() {
        return stockName;
    }

    public BigDecimal fetchCurrentClosingPrice() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat();
        format.setCalendar(cal);

        help.debug("StockType", "Fetching:%s: Time:" + cal.getTime() + "\n" , stockName);

        while(fetcher.fetchClosingPrice(stockName, format) == null) {
            //TODO:check end
            cal.add(Calendar.DATE, -1);
        }

        return fetcher.fetchClosingPrice(stockName, format);
    }

    public BigDecimal fetchClosingPrice(SimpleDateFormat time) {

        help.debug("StockType", "Fetching:%s: Time:%s\n", stockName, help.dateToString(time));

        return fetcher.fetchClosingPrice(stockName, time);
    }
    public BigDecimal fetchClosingPrice(Date time){

        if (time==null) {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        SimpleDateFormat format = new SimpleDateFormat();
        format.setCalendar(cal);

        help.debug("StockType", "Fetching:%s: Time:" + time + "\n" , stockName);

        return fetcher.fetchClosingPrice(stockName, format);
    }

    public BigDecimal fetchPastDayClosingPrice(int count){
        BigDecimal tmp = null;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy");

        cal.add(Calendar.DAY_OF_MONTH, count*-1);

        date.setCalendar(cal);

        tmp = fetcher.fetchClosingPrice(stockName, date);
        
        return tmp;
    }

        public GraphPacket fetchPastDayClosingPricePacket(int count){
        GraphPacket packet = null;
        BigDecimal tmp = null;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy");

        cal.add(Calendar.DAY_OF_MONTH, count*-1);

        date.setCalendar(cal);

        tmp = fetcher.fetchClosingPrice(stockName, date);

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
    
}
