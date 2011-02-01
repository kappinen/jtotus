/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jtotus.common;

import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author house
 */
public class DayisHolidayTest {

    public DayisHolidayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of isHoliday method, of class DayisHoliday.
     */
    @Test
    public void testIsHoliday() {
        System.out.println("isHoliday");
        Calendar date = null;
        DayisHoliday instance = new DayisHoliday();
        boolean expResult = false;
        boolean result = instance.isHoliday(date);
        assertEquals(expResult, result);
    }

}