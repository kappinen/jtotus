/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import jtotus.common.Helper;

/**
 *
 * @author kappiev
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

    public Float fetchClosingPrice(String stockName, SimpleDateFormat time){
        Float result = null;
        

        if (timeFailsSanityCheck(time)) {
            return result;
        }

        Iterator <InterfaceDataBase>resources = listOfResources.iterator();

        result = javadb.fetchClosingPrice(stockName, time);
        if(result == null) {
            help.debug(this.getClass().getName(),
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

    public Float fetchAveragePrice(String stockName, SimpleDateFormat time) {
       Float result = null;


          return result;
    }

}
