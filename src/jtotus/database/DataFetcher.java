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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.math.BigDecimal;
import jtotus.common.Helper;

/**
 *
 * @author Evgeni Kappinen
 */

public class DataFetcher implements InterfaceDataBase {

    private LinkedList<InterfaceDataBase> listOfResources = null;
    private LocalJavaDB javadb = null;
    private Helper help = Helper.getInstance();

    public DataFetcher()
    {
        listOfResources = new LinkedList<InterfaceDataBase>();
        listOfResources.add(new FileSystemFromHex());
        listOfResources.add(new NetworkOP());
        javadb = new LocalJavaDB();
        // listOfResources.add(new NetworkGoogle());
    }

    //TRUE  failuer
    //FALSE success
    private boolean timeFailsSanityCheck(SimpleDateFormat time) {
        boolean result = false;
        Calendar cal = null;

        cal = time.getCalendar();
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
           cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
           result = true;
        }

        cal = null;
        return result;
    }

    //TODO:generic fetcher with EnumFetcherCall

    public BigDecimal fetchClosingPrice(String stockName, SimpleDateFormat time){
        BigDecimal result = null;
        

        if (timeFailsSanityCheck(time)) {
            return result;
        }

        Iterator <InterfaceDataBase>resources = listOfResources.iterator();

        result = javadb.fetchClosingPrice(stockName, time);
        if(result == null) {
            help.debug("DataFetcher",
                    "Closing Price is not found int in javadb stock:%s time:%s\n",
                    stockName,help.dateToString(time));
            
            while(resources.hasNext()){
                InterfaceDataBase res = resources.next();

                
                result = res.fetchClosingPrice(stockName, time);
                if (result != null) {
                        javadb.storeClosingPrice(stockName, time, result);
                    return result;
                }
            }
        }
        return result;
    }

    public BigDecimal fetchAveragePrice(String stockName, SimpleDateFormat time) {
       BigDecimal result = null;


          return result;
    }

}
