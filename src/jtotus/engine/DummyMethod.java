/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.common.Helper;
import jtotus.threads.Dispatcher;
import jtotus.threads.VoterThread;

/**
 *
 * @author kappiev
 */
public class DummyMethod implements Runnable,VoterThread {

    private Helper help = Helper.getInstance();
    private static Random genNum = new Random();
    private Dispatcher control = null;
    private String methodName = "DummyMethod";

    public DummyMethod(Dispatcher tmp) {
        control = tmp;
    }

    public DummyMethod(Dispatcher tmp, String name) {
        methodName = name;
        control = tmp;
    }

    public void run() {
        try {
            Thread.sleep(1000+genNum.nextInt(3000));
            help.debug(1, "DummyMethod is running\n");
        } catch (InterruptedException ex) {
            Logger.getLogger(DummyMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMethName() {
        return methodName;
    }

}
