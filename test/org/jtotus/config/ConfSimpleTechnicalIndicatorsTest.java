/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jtotus.config;

import org.junit.Assert;
import brokerwatcher.indicators.SimpleTechnicalIndicators;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author house
 */
public class ConfSimpleTechnicalIndicatorsTest {

    public ConfSimpleTechnicalIndicatorsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        double cor = SimpleTechnicalIndicators.correlation(new double[] {0,1,2,3}, new double[] {0,1,2,3});
        Assert.assertFalse(cor != 1.0);

    }

}