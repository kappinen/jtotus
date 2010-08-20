/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 * FIXME:
 * http://www.javalobby.org/forums/thread.jspa?messageID=91836328
 * http://java-x.blogspot.com/2006/11/java-5-concurrency-callable-and-future.html
 * http://blogs.sun.com/CoreJavaTechTips/entry/get_netbeans_6
 * http://www.javalobby.org/java/forums/t16252.html
 * http://www.roseindia.net/java/thread/InterthreadCommunication.shtml
 * http://programmingexamples.wikidot.com/java-util-concurrent
 * http://stackoverflow.com/questions/2300579/how-to-access-a-runnable-object-by-thread
 *
 *
 * 
 */
package jtotus.threads;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jtotus.common.Helper;
import jtotus.common.MethodConfig;
import jtotus.engine.DummyMethod;

/**
 *
 * @author kappiev
 */
public class Dispatcher {

    private Helper help = null;
    private LinkedList<VoterThread> threadList;
    ExecutorService threadExecutor = null;

    private void init() {
        help = Helper.getInstance();
        threadList = new LinkedList<VoterThread>();
        threadExecutor = Executors.newCachedThreadPool();

    }

    public Dispatcher() {

        init();

        // Add methods to thread
        threadList.add(new DummyMethod(this));
//        threadList.add(new Thread(new DummyMethod(this)));

    }

    public Dispatcher(LinkedList<VoterThread> threads) {

        init();


        Iterator<VoterThread> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            threadList.add(iterator.next());
        }

    }

    public boolean setList(LinkedList<VoterThread> threads) {

        help = Helper.getInstance();

        if (threadList == null) {
            threadList = new LinkedList<VoterThread>();
        }

        if (!threadList.isEmpty()) {
            threadList.clear();
        }

        Iterator<VoterThread> iterator = threads.iterator();
        while (iterator.hasNext()) {
            threadList.add(iterator.next());
        }

        return false;
    }

    public void run() {
        help.debug(this.getClass().getName(), "Dispatcher started..\n");



        //Start threads       
        Iterator<VoterThread> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            VoterThread tmp = iterator.next();
            threadExecutor.execute(tmp);
        }

        help.debug(this.getClass().getName(), "Dispatcher ended.. List:%d\n", threadList.size());
    }

    public MethodConfig fetchConfig(String method) {
        //TODO: add which configuration to run by user

        MethodConfig config = new MethodConfig();

        return config;
    }
}
