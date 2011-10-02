/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlucrum.datafetcher;

import java.util.Map.Entry;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author home
 */
public class DataFetcherTest {
    
    public DataFetcherTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Test
    public void testDataSource() {
        DataFetcher instance = new DataFetcher();
        
        
        instance.setSource("NasdaqOmxNordic");
        Map <String,Double>result = instance.fetchPeriodData("Metso Oyj", "2011-09-13", "2011-09-15", "close");
        
        for (Entry<String, Double> entry : result.entrySet()) {
            System.out.printf("%s - %f\n", entry.getKey(), entry.getValue());
        }
        
        assertEquals(result.get("2011-09-15"), Double.valueOf("25.230000"));
        assertEquals(result.get("2011-09-14"), Double.valueOf("23.880000"));
        assertEquals(result.get("2011-09-13"), Double.valueOf("23.370000"));
        
        
        result = instance.fetchPeriodData("Metso Oyj", "2011-09-13", "2011-09-15", "volume");
        
        for (Entry<String, Double> entry : result.entrySet()) {
            System.out.printf("Volume: %s - %f\n", entry.getKey(), entry.getValue());
        }
        
        assertEquals(result.get("2011-09-15"), Double.valueOf("1021909"));
        assertEquals(result.get("2011-09-14"), Double.valueOf("718663"));
        assertEquals(result.get("2011-09-13"), Double.valueOf("819216"));
        
        result = instance.fetchPeriodData("Metso Oyj", "2011-09-13", "2011-09-15", "trades");
        
        for (Entry<String, Double> entry : result.entrySet()) {
            System.out.printf("trades: %s - %f\n", entry.getKey(), entry.getValue());
        }
        
        assertEquals(result.get("2011-09-15"), Double.valueOf("5704"));
        assertEquals(result.get("2011-09-14"), Double.valueOf("4795"));
        assertEquals(result.get("2011-09-13"), Double.valueOf("4901"));
        
    }
}
