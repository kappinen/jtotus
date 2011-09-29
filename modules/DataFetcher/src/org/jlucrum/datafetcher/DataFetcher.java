/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlucrum.datafetcher;

import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Evgeni Kappinen
 */
public class DataFetcher {

    private MarketFetcher fetcher = new FetcherOsuusPankki();
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final Map<String, Integer> typeMap = new HashMap<String, Integer>();
    
    /*
     * Types:
     * 
     * Close -> 0
     * Volume -> 1
     * Avrg -> 3
     */

    public DataFetcher() {
        typeMap.put("close", 0);
        typeMap.put("volume", 1);
        typeMap.put("avrg", 4);
    }

    public Map<String, Double> fetchClosePrice(String name, String fromDate, String toDate) {
        DateTime todate = dateFormatter.parseDateTime(toDate);
        DateTime fromdate = dateFormatter.parseDateTime(fromDate);

        return fetcher.fetchDataPeriod(name, fromdate, todate, 0);
    }

    public double fetchClosePrice(String name, String date) {
        DateTime tdate = dateFormatter.parseDateTime(date);

        return fetcher.fetchData(name, tdate, 0);
    }
    
    public Map<String, Double> fetchPeriodVolume(String name, String fromDate, String toDate) {
        DateTime todate = dateFormatter.parseDateTime(toDate);
        DateTime fromdate = dateFormatter.parseDateTime(fromDate);

        return fetcher.fetchDataPeriod(name, fromdate, todate, 1);
    }

    public double fetchVolume(String name, String date) {
        DateTime tdate = dateFormatter.parseDateTime(date);

        return fetcher.fetchData(name, tdate, 1);
    }
    
    public Map<String, Double> fetchPeriodData(String name, String fromDate, String toDate, String type) {
        DateTime todate = dateFormatter.parseDateTime(toDate);
        DateTime fromdate = dateFormatter.parseDateTime(fromDate);
        
        return fetcher.fetchDataPeriod(name, fromdate, todate, typeMap.get(type));
    }

    public double fetchData(String name, String date, String type) {
        DateTime tdate = dateFormatter.parseDateTime(date);

        return fetcher.fetchData(name, tdate, typeMap.get(type));
    }
}
