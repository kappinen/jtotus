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

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.math.BigDecimal;

import brokerwatcher.BrokerWatcher;
import brokerwatcher.eventtypes.MarketData;
import com.espertech.esper.client.EPRuntime;
import org.jtotus.common.DayisHoliday;
import org.jtotus.common.Helper;

/**
 *
 * @author Evgeni Kappinen
 */
public class DataFetcher {

    private LinkedList<InterfaceDataBase> listOfResources = null;
    private Helper help = Helper.getInstance();
    private DayisHoliday holidays = null;
    private CacheServer cache = null;
    private LocalJDBC localJDBC = null;
    private boolean debug = false;

    public DataFetcher() {
        listOfResources = new LinkedList<InterfaceDataBase>();

        //Supported resource
        listOfResources.add(new FileSystemFromHex());
        listOfResources.add(new NetworkOP());

        holidays = new DayisHoliday();
        cache = CacheServer.getInstance();
        LocalJDBCFactory factory = LocalJDBCFactory.getInstance();
        localJDBC = factory.jdbcFactory();

        // listOfResources.add(new NetworkGoogle());

    }

    //TRUE  Failuer
    //FALSE Success
    private boolean timeFailsSanityCheck(Calendar date) {
        boolean result = false;

        if (holidays.isHoliday(date)) {
            result = true;
        }

        return result;
    }

    //TODO:generic fetcher with EnumFetcherCall
    public BigDecimal fetchClosingPrice(String stockName, Calendar date) {
        BigDecimal result = null;

        if (this.timeFailsSanityCheck(date)) {
            return result;
        }

        //Check with cache first
        result = cache.getValue(stockName, date);
        if (result != null) {
            //System.out.printf("FROM CACHE:%s %s %f\n",stockName, date.getTime().toString(), result.floatValue());
            return result;
        }

        result = localJDBC.fetchClosingPrice(stockName, date);
        if (result == null) {

            for (InterfaceDataBase listOfResource : listOfResources) {
                InterfaceDataBase res = listOfResource;

                result = res.fetchClosingPrice(stockName, date);
                if (result != null) {
                    localJDBC.storeClosingPrice(stockName, date, result);
                    cache.putValue(stockName, date, result);
                    return result;
                }
            }
        } else {
            //put to cache
            cache.putValue(stockName, date, result);
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

        Iterator<InterfaceDataBase> resources = listOfResources.iterator();

        result = localJDBC.fetchVolume(stockName, date);

        if (result == null) {
            help.debug("DataFetcher",
                       "Volume is not found int in javadb stock:%s time:%s\n",
                       stockName,
                       date.toString());

            while (resources.hasNext()) {
                InterfaceDataBase res = resources.next();

                result = res.fetchVolume(stockName, date);
                if (result != null) {
                    localJDBC.storeVolume(stockName, date, result);
                    return result;
                }
            }
        }

        return result;
    }

    public double[] fetchClosingPricePeriod(final String stockName, final Calendar startDate, final Calendar endDate) {

        if (debug) {
            System.out.printf("Fetching data for: %s\n", stockName);
        }
        localJDBC.setFetcher(this);
        return localJDBC.fetchPeriod(stockName,
                                     startDate,
                                     endDate);
    }

    public boolean sendMarketData(final String[] listOfStocks, final Calendar startDate, final Calendar endDate) {

        final MarketData marketData = new MarketData()
                .setAsClosingPrice();

        EPRuntime runtime = BrokerWatcher.getMainEngine()
                .getEPRuntime();


        for (String stockName : listOfStocks) {
//            final Thread thread = new Thread() {
//                public void run() {
            if (debug) {
                System.out.printf("Fetching data for: %s\n", stockName);
            }

            LocalJDBCFactory factory = LocalJDBCFactory.getInstance();
            LocalJDBC locJDBC = factory.jdbcFactory();
            locJDBC.setFetcher(new DataFetcher());
            double[] data = locJDBC.fetchPeriod(stockName,
                                                startDate,
                                                endDate);

            if (data == null) {
                return false;
            }

            marketData.data.put(stockName, data);
            marketData.setDate(endDate);
//                }
//            };
//            thread.start();
        }

        if (debug) {
            System.out.printf("Sending market data : %d\n", marketData.data.size());
        }
        
        runtime.sendEvent(marketData);


        return true;
    }

    public static void main(String[] arv) {
        LocalJDBCFactory factory = LocalJDBCFactory.getInstance();
        LocalJDBC localJDBC = factory.jdbcFactory();
        System.out.printf("Fetching data..\n");
        DataFetcher fetcher = new DataFetcher();
        localJDBC.setFetcher(fetcher);
        localJDBC.setDebug(true);

        Calendar endDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, -25);
//        localJDBC.fetchPeriod("Outokumpu Oyj", startDate, endDate);
        String[] list;
        list = new String[]{"Outokumpu Oyj", "Metso Oyj", "Nokia Oyj"};
//        list = new String[]{"Outokumpu Oyj"};

        fetcher.sendMarketData(list, startDate, endDate);


    }
}
