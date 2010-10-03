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

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.math.BigDecimal;
import jtotus.common.DayisHoliday;
import jtotus.common.Helper;

/**
 *
 * @author Evgeni Kappinen
 */

public class DataFetcher{

    private LinkedList<InterfaceDataBase> listOfResources = null;
    private LocalJavaDB javadb = null;
    private Helper help = Helper.getInstance();
    private DayisHoliday holidays = null;


    public DataFetcher()
    {
        listOfResources = new LinkedList<InterfaceDataBase>();
        listOfResources.add(new FileSystemFromHex());
        listOfResources.add(new NetworkOP());
        javadb = new LocalJavaDB();
        holidays = new DayisHoliday();
        // listOfResources.add(new NetworkGoogle());
    }

    //TRUE  failuer
    //FALSE success
    private boolean timeFailsSanityCheck(Calendar date) {
        boolean result = false;
        
        if(date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ||
           date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
           result = true;
        } else if(holidays.isHoliday(date)){
            result = true;
        }

        return result;
    }

    //TODO:generic fetcher with EnumFetcherCall

    public BigDecimal fetchClosingPrice(String stockName, Calendar date){
        BigDecimal result = null;
        

        if (timeFailsSanityCheck(date)) {
            return result;
        }

        Iterator <InterfaceDataBase>resources = listOfResources.iterator();

        result = javadb.fetchClosingPrice(stockName, date);
        if(result == null) {
            help.debug("DataFetcher",
                    "Closing Price is not found int in javadb stock:%s time:%s\n",
                    date.toString());
            
            while(resources.hasNext()){
                InterfaceDataBase res = resources.next();
                
                System.out.printf("Searching for price\n");

                result = res.fetchClosingPrice(stockName, date);
                if (result != null) {
                        javadb.storeClosingPrice(stockName, date, result);
                    return result;
                }
            }
        }
        
        return result;
    }

    public BigDecimal fetchAveragePrice(String stockName, Calendar time) {
       BigDecimal result = null;


          return result;
    }

    public BigDecimal fetchVolumeForDate(String stockName, Calendar date) {
      BigDecimal result = null;


        if (timeFailsSanityCheck(date)) {
            return result;
        }

       
        Iterator <InterfaceDataBase>resources = listOfResources.iterator();
    
        result = javadb.fetchVolume(stockName, date);
        
        if(result == null) {
            help.debug("DataFetcher",
                    "Volume is not found int in javadb stock:%s time:%s\n",
                    stockName,
                    date.toString());
                    
            while(resources.hasNext()){
                InterfaceDataBase res = resources.next();
                
                result = res.fetchVolume(stockName, date);
                if (result != null) {
                        System.out.printf("Searching for volume3\n");
                        javadb.storeVolume(stockName, date, result);
                    return result;
                }
            }
        }

        return result;
    }


}

