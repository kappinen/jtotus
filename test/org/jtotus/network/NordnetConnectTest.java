/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jtotus.network;

import org.jtotus.crypt.JtotusKeyRingPassword;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author house
 */
public class NordnetConnectTest {

    public NordnetConnectTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of connect method, of class NordnetConnect.
     */
    @Test
    public void testConnect() {
        System.out.println("connect");
        JtotusKeyRingPassword pass = JtotusKeyRingPassword.getInstance();
        pass.putKeyRingPassword("ForTestOnly");

        NordnetConnect instance = new NordnetConnect();
        boolean expResult = true;
        boolean result = instance.connect();
        assertEquals(expResult, result);

        if(instance.getTick("Nokia Oyj")==null) {
            fail("Failed to fetch page");
        }


        // TODO review the generated test code and remove the default call to fail.
        
    }


}