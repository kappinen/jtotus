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

import java.util.Iterator;
import java.util.LinkedList;
import java.math.BigDecimal;
import brokerwatcher.BrokerWatcher;
import brokerwatcher.eventtypes.MarketData;
import com.espertech.esper.client.EPRuntime;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jtotus.common.DayisHoliday;
import org.jtotus.common.Helper;

/**
 *
 * @author Evgeni Kappinen
 */
public class DataFetcher {

    private LinkedList<InterfaceDataBase> listOfResources = null;
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
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

    public BigDecimal fetchAveragePrice(String stockName, DateTime time) {
        return fetchData(stockName, time, "AVRG");
    }

    public BigDecimal fetchVolumeForDate(String stockName, DateTime date) {
        return fetchData(stockName, date, "VOLUME");
    }

    public BigDecimal fetchClosingPrice(String stockName, DateTime date) {
        return fetchData(stockName, date, "CLOSE");
    }

    public BigDecimal fetchData(String stockName, DateTime date, String type) {
        BigDecimal result = null;

        if (DayisHoliday.isHoliday(date)) {
            return result;
        }

        //Check with cache first
        result = cache.getValue(stockName + type, date);
        if (result != null) {
            //System.out.printf("FROM CACHE:%s %s %f\n",stockName, date.getTime().toString(), result.floatValue());
            return result;
        }

        result = localJDBC.fetchData(stockName, date, type);
        if (result == null) {

            for (InterfaceDataBase listOfResource : listOfResources) {
                InterfaceDataBase res = listOfResource;

                result = res.fetchData(stockName, date, type);
                if (result != null) {
                    localJDBC.storeData(stockName, date, result, type);
                    cache.putValue(stockName + type, date, result);
                    return result;
                }
            }
        } else {
            //put to cache
            cache.putValue(stockName + type, date, result);
        }

        return result;
    }

    public double[] fetchClosingPricePeriod(final String stockName, 
                                            final DateTime startDate,
                                            final DateTime endDate) {

        if (debug) {
            System.out.printf("Fetching data for: %s\n", stockName);
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End day should be before start");
        }

        localJDBC.setFetcher(this);
        return localJDBC.fetchPeriod(stockName,
                startDate,
                endDate,
                "CLOSE");
    }

    public double[] fetchVolumePeriod(final String stockName, 
                                      final DateTime startDate,
                                      final DateTime endDate) {

        if (debug) {
            System.out.printf("Fetching data for: %s\n", stockName);
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End day should be before start");
        }

        localJDBC.setFetcher(this);
        return localJDBC.fetchPeriod(stockName,
                startDate,
                endDate,
                "VOLUME");
    }

    public double[] fetchPeriodByString(final String stockName, 
                                        final String startDate,
                                        final String endDate, String type) {

        if (debug) {
            System.out.printf("Fetching data for: %s\n", stockName);
        }

        DateTime end = formatter.parseDateTime(endDate);
        DateTime start = formatter.parseDateTime(startDate);

        localJDBC.setFetcher(this);
        return localJDBC.fetchPeriod(stockName,
                start,
                end,
                type);
    }

    public boolean sendMarketData(final String[] listOfStocks, final DateTime startDate, final DateTime endDate) {

        final MarketData marketData = new MarketData().setAsClosingPrice();

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
                    endDate,
                    "CLOSE");

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
        EPRuntime runtime = BrokerWatcher.getMainEngine().getEPRuntime();
        runtime.sendEvent(marketData);


        return true;
    }

    public MarketData prepareMarketData(final String[] listOfStocks, 
                                        final DateTime startDate,
                                        final DateTime endDate) {

        final MarketData marketData = new MarketData().setAsClosingPrice();

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
                    endDate,
                    "CLOSE");

            if (data == null) {
                return null;
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

        return marketData;
    }
//    public static void main(String[] arv) {
//        LocalJDBCFactory factory = LocalJDBCFactory.getInstance();
//        LocalJDBC localJDBC = factory.jdbcFactory();
//        System.out.printf("Fetching data..\n");
//        DataFetcher fetcher = new DataFetcher();
//        localJDBC.setFetcher(fetcher);
//        localJDBC.setDebug(true);
//
//        DateTime endDate = new DateTime();
//        DateTime startDate = new DateTime().minusDays(25);
////        localJDBC.fetchPeriod("Outokumpu Oyj", startDate, endDate);
//        String[] list;
//        list = new String[]{"Outokumpu Oyj", "Metso Oyj", "Nokia Oyj"};
////        list = new String[]{"Outokumpu Oyj"};
//
//        fetcher.sendMarketData(list, startDate, endDate);
//
//
//    }
}
