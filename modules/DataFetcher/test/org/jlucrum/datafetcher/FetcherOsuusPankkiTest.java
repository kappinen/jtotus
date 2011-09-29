/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlucrum.datafetcher;

import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author home
 */
public class FetcherOsuusPankkiTest {
    private String patternString = "yyyy-MM-dd";
    private DateTimeFormatter formatter = DateTimeFormat.forPattern(patternString);
    
    public FetcherOsuusPankkiTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testFetchData() {
        System.out.println("fetchData");
        String stockName = "KONE Oyj";
        int type = 0;
        
        FetcherOsuusPankki instance = new FetcherOsuusPankki();
        
        DateTime date = formatter.parseDateTime("2011-05-02");
        Double expResult = Double.valueOf("42.450000");
        Double result = instance.fetchData(stockName, date, type);
        assertEquals(expResult, result);
        
        
        date = formatter.parseDateTime("2011-05-03");
        expResult = Double.valueOf("41.770000");
        result = instance.fetchData(stockName, date, type);
        assertEquals(expResult, result);
    }

    @Test
    public void testFetchDataPeriod() {
        System.out.println("fetchDataPeriod");
        String name = "KONE Oyj";

        DateTime fromDate = formatter.parseDateTime("2011-05-01");
        DateTime toDate = formatter.parseDateTime("2011-05-05");;
        int type = 0;
        FetcherOsuusPankki instance = new FetcherOsuusPankki();
        
        
        Map<String,Double> result = instance.fetchDataPeriod(name, fromDate, toDate, type);
        
        assertEquals(result.get("2011-05-05"), Double.valueOf("41.120000"));
        assertEquals(result.get("2011-05-04"), Double.valueOf("41.640000"));
        assertEquals(result.get("2011-05-03"), Double.valueOf("41.770000"));
        assertEquals(result.get("2011-05-02"), Double.valueOf("42.450000"));
    }

}
