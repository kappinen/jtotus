/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jtotus.methods;


import org.jtotus.common.MethodResults;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author house
 */
public class StatisticsFreqPeriodTest {

    public StatisticsFreqPeriodTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getMethName method, of class StatisticsFreqPeriod.
     */
    @Test
    public void testGetMethName() {
        System.out.println("getMethName");
        StatisticsFreqPeriod instance = new StatisticsFreqPeriod();
        String expResult = "StatisticsFreqPeriod";
        String result = instance.getMethName();
        assertEquals(expResult, result);

    }

    /**
     * Test of statisticsForFreq method, of class StatisticsFreqPeriod.
     */
    @Test
    public void testStatisticsForFreq() {
        System.out.println("statisticsForFreq");
        int[][] output;
        int count = 0;
        double[] input = new double[] {1,2,3,4,
                                        1,2,3,4,
                                        1,2,3,4};
        
        StatisticsFreqPeriod instance = new StatisticsFreqPeriod();
        output = instance.statisticsForFreq(input);


        System.out.printf("%d - %d = %d\n", 0, 3, output[0][3]);
        Assert.assertFalse(output[0][3] != 3);
        Assert.assertFalse(output[1][1] != 2);

        for (int i = 0; i < 3;i++) {
            for (int y = 0; y < instance.getMaxPeriod();y++) {
                if (i == 0 && y == 3) {
                    if (output[0][3] == 3) {
                        count++;
                        continue;
                    } else
                        Assert.assertFalse(true);
                }else if (i == 1 && y == 1) {
                    if (output[i][y] == 2) {
                        count++;
                        continue;
                    } else
                        Assert.assertFalse(true);
                }
                Assert.assertFalse(output[i][y] != 0);
            }
        }

        
        double[] input2 = new double[]{1, 2, 3, 4,
                                       1, 2, 3, 4,
                                       4, 3, 2, 1};

        double[] input3 = new double[] {1,2,3,4,
                                        1,2,3,4,
                                        1,2,3,4};

//        System.out.printf("last trend:%d\n", instance.lastTrend(input2));
//        System.out.printf("last trend:%d\n", instance.lastTrend(input3));
        Assert.assertFalse(count != 2);
        Assert.assertFalse(instance.lastTrend(input2) != -3);
        Assert.assertFalse(instance.lastTrend(input3) != 3);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of lastTrend method, of class StatisticsFreqPeriod.
     */
    @Test
    public void testLastTrend() {
        System.out.println("lastTrend");
        String stockName = "";
        StatisticsFreqPeriod instance = new StatisticsFreqPeriod();

        int result = instance.lastTrend("Nokia Oyj");
        System.out.printf("trend:%d\n", result);
    }

}