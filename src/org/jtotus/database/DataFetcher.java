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

import java.util.LinkedList;
import java.math.BigDecimal;
import org.jlucrum.realtime.BrokerWatcher;
import org.jlucrum.realtime.eventtypes.MarketData;
import com.espertech.esper.client.EPRuntime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jtotus.common.DateIterator;
import org.jtotus.common.DayisHoliday;

/**
 *
 * @author Evgeni Kappinen
 */
public class DataFetcher {

    private LinkedList<InterfaceDataBase> listOfResources = null;
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
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
    
    public double[][] fetchStockData(final String startDate,
                                     final String endDate,
                                     final String... stockNamesAndTypes) {
        int count = 0;

        ArrayList<String[]> namesAndTypes = new ArrayList<String[]>();
        ArrayList<ArrayList<Double>> stockData = new ArrayList<ArrayList<Double>>(stockNamesAndTypes.length);

        for(int i=0; i < stockNamesAndTypes.length; i++) {
            stockData.add(new ArrayList<Double>());
        }
        localJDBC.setFetcher(this);

        if (debug) {
            System.out.printf("Fetching data for: %s\n", stockNamesAndTypes.toString());
        }
        System.out.printf("Testing - 1\n");
        for (int i = 0; i < stockNamesAndTypes.length; i++) {
            String[] nameAndType = stockNamesAndTypes[i].split(",");
            if (nameAndType.length != 2) {
                return null;
            } else {
                System.out.printf("puting %s and %s\n", nameAndType[0], nameAndType[1]);
                namesAndTypes.add(nameAndType);
            }
        }

        DateTime end = formatter.parseDateTime(endDate);
        DateTime start = formatter.parseDateTime(startDate);
        if (end.isBefore(start)) {
            throw new RuntimeException("End day should be before start");
        }

        DateIterator iter = new DateIterator(start, end);
        ArrayList<BigDecimal> dayData = new ArrayList<BigDecimal>();
        while (iter.hasNext()) {
            dayData.clear();
            DateTime nextDay = iter.nextInCalendar();

            for (String[] entry : namesAndTypes) {
                if (debug) {
                    System.out.printf("Fetching %s and %s\n", entry[0], entry[1]);
                }
                //System.out.printf("Fetching %s and %s\n", entry[0], entry[1]);
                BigDecimal data = this.fetchData(entry[0], nextDay, entry[1]);
                if (data == null) {
                    break;
                }
                dayData.add(data);
            }

            if (dayData.size() == stockNamesAndTypes.length) {
                count++;
                for (int i = 0; i < dayData.size(); i++) {
                    stockData.get(i).add(dayData.get(i).doubleValue());
                }
            } else {
                System.out.printf("Couldn't find data for : %s -> %d == %d\n", nextDay.toString(), dayData.size(), stockNamesAndTypes.length);
            }
        }
        System.out.printf("Testing - 1 : count:%d names:%d\n", count, stockNamesAndTypes.length);
        double retMatrix[][] = new double[stockNamesAndTypes.length][count];
        for (int i = 0; i < stockData.size(); i++) {
            System.out.printf("Testing - 122 : %d\n", i);
            retMatrix[i] = ArrayUtils.toPrimitive(stockData.get(i).toArray(new Double[count]));
        }

        return retMatrix;
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
    public static void main(String[] arv) {
        DataFetcher fetcher = new DataFetcher();
        double mat [][] = fetcher.fetchStockData("01-01-2009", "01-01-2010", "Metso Oyj,CLOSE", "Metso Oyj,VOLUME");

        System.out.printf("Done! : %d\n", mat.length);
    }
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
