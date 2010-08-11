/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.threads;
import java.util.Iterator;
import java.util.LinkedList;
import jtotus.common.Helper;
import jtotus.engine.DummyMethod;
/**
 *
 * @author kappiev
 */
public class Dispatcher {
    Helper help = null;
    LinkedList <Thread>threadList;


    public Dispatcher() {
        help = Helper.getInstance();
        threadList = new LinkedList<Thread>();
        
    }


    public void run() {
        help.debug(3,"Dispatcher started..\n");


        // Add methods to thread
        
        threadList.add(new Thread(new DummyMethod()));
        threadList.add(new Thread(new DummyMethod()));
        threadList.add(new Thread(new DummyMethod()));
        threadList.add(new Thread(new DummyMethod()));
        threadList.add(new Thread(new DummyMethod()));
        threadList.add(new Thread(new DummyMethod()));




        //Start threads

        Iterator iterator = threadList.iterator();
        while (iterator.hasNext()) {
            Thread tmp = (Thread) iterator.next();
            tmp.start();

        }


        help.debug(3,"Dispatcher ended..\n");
    }


}
