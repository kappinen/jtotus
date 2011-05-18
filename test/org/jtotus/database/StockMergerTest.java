/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jtotus.database;

import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author house
 */
public class StockMergerTest {
    
    public StockMergerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of mergedPeriods method, of class StockMerger.
     */
    @Test
    public void testMergedPeriods() {
        System.out.println("mergedPeriods");
        StockMerger instance = new StockMerger();
        
        
        DateTime endDate = new DateTime(2011, 1, 1, 0, 0, 0, 0);
        DateTime startDate = endDate.minusDays(10);
        
        double[][] expResult = {{20.250000, 20.290000, 20.210000, 20.290000, 20.210000, 20.050000}, 
                                {26.990000, 27.010000, 27.090000, 27.110000, 27.220000, 27.190000}};
        double[][] results = instance.mergedPeriods("Sampo Oyj A", "IP",  startDate, endDate);
        assertEquals(expResult, results);
    }
}
