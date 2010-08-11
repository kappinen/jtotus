/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.common.Helper;
import jtotus.threads.VoterThread;

/**
 *
 * @author kappiev
 */
public class DummyMethod implements Runnable,VoterThread {

    private Helper help = Helper.getInstance();

    
    public void run() {
        try {
            Thread.sleep(1000);
            help.debug(1, "DummyMethod is running\n");
        } catch (InterruptedException ex) {
            Logger.getLogger(DummyMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMethName() {
        return "DummyMethod";
    }

}
