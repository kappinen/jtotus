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
import jtotus.common.Helper;
import jtotus.engine.DummyMethod;
/**
 *
 * @author kappiev
 */
public class Dispatcher {

    private Helper help = null;
    private LinkedList <Thread>threadList;


    public Dispatcher() {
        help = Helper.getInstance();
        threadList = new LinkedList<Thread>();

         // Add methods to thread

//        threadList.add(new Thread(new DummyMethod(this)));
//        threadList.add(new Thread(new DummyMethod(this)));
//        threadList.add(new Thread(new DummyMethod(this)));
//        threadList.add(new Thread(new DummyMethod(this)));
//        threadList.add(new Thread(new DummyMethod(this)));
//        threadList.add(new Thread(new DummyMethod(this)));
        
    }

    public Dispatcher(LinkedList <VoterThread>threads) {

        help = Helper.getInstance();
        threadList = new LinkedList<Thread>();

        
        Iterator iterator = threadList.iterator();
        while (iterator.hasNext()) {
            threadList.add(new Thread((VoterThread)iterator.next()));
         }
 
    }

    public boolean setList(LinkedList <VoterThread>threads) {

        help = Helper.getInstance();

        if (threadList == null) {
            threadList = new LinkedList<Thread>();
        }
        
        if(!threadList.isEmpty())
        {
            return true;
        }
        
        Iterator iterator = threads.iterator();
        while (iterator.hasNext()) {
            threadList.add(new Thread((VoterThread)iterator.next()));
         }
        return false;
    }

    public void run() {
        help.debug(3,"Dispatcher started..\n");



        //Start threads

        Iterator iterator = threadList.iterator();
        while (iterator.hasNext()) {
            Thread tmp = (Thread) iterator.next();
            tmp.start();
        }

        help.debug(3,"Dispatcher ended.. List:%d\n",threadList.size());
    }



    public synchronized Object dispatch() {

        //TODO: general interface for voters

        return null;
    }







    

}
