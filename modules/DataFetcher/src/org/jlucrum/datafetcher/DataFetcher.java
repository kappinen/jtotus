/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlucrum.datafetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Evgeni Kappinen
 */
public class DataFetcher {

    List<MarketFetcher> sources = new ArrayList<MarketFetcher>();
       
    private MarketFetcher fetcher = null;
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
        typeMap.put("high", 2);
        typeMap.put("low", 3);
        typeMap.put("avrg", 4);
        typeMap.put("turnover", 5);
        typeMap.put("trades", 6);
        
        MarketFetcher defaultSource = new FetcherNasdaqOmxNordic();
        this.fetcher = defaultSource;
        sources.add(defaultSource);
        sources.add(new FetcherOsuusPankki());
                
    }

    public void setSource(String sourceName) {

        System.out.println("Source:");
        for (int i = 0; i < sources.size(); i++) {
            MarketFetcher fetchInstance = sources.get(i);
            String name = fetchInstance.getClass().getSimpleName().replaceAll("Fetcher", "");
            if (sourceName.equalsIgnoreCase(name)) {
                System.out.printf("Setting source to:%s\n", name);
                this.fetcher = fetchInstance;
                return;
            }
        }

        System.out.printf("%s is not found from source list\n", sourceName);
        this.listSources();
    }

    public String[] listSources() {
        List<String>names = new ArrayList<String>();

        System.out.println("Source:");
        for (int i = 0; i < sources.size(); i++) {
            MarketFetcher fetchInstance = sources.get(i);
            String name = fetchInstance.getClass().getSimpleName().replaceAll("Fetcher", "");
            System.out.printf("%d.   %s\n", i, name);
            names.add(name);
        }

        return (String[]) names.toArray();
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
