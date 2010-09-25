/*

    This file is part of jTotus.

    jTotus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jTotus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jTotus.  If not, see <http://www.gnu.org/licenses/>.


 *
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
import jtotus.config.MethodConfig;

/**
 *
 * @author kappiev
 */
public class PortfolioDecision implements Runnable{

    private Helper help = null;
    private LinkedList<VoterThread> threadList;
    ExecutorService threadExecutor = null;

    private void init() {
        help = Helper.getInstance();
        threadList = new LinkedList<VoterThread>();
        threadExecutor = Executors.newCachedThreadPool();

    }

    public PortfolioDecision() {

        init();
    }

    public PortfolioDecision(LinkedList<VoterThread> threads) {

        init();


        Iterator<VoterThread> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            threadList.add(iterator.next());
        }

    }

    public boolean setList(LinkedList<VoterThread> threads) {


        if (threadList == null) {
            threadList = new LinkedList<VoterThread>();
        }

        help.debug(this.getClass().getName(), "setting list with size:%d\n",
                threads.size());
        
        if (!threadList.isEmpty()) {
            threadList.clear();
        }

        Iterator<VoterThread> iterator = threads.iterator();
        while (iterator.hasNext()) {
            threadList.add(iterator.next());
        }

        return false;
    }

    public MethodConfig fetchConfig(String method) {
        //TODO: add which configuration to run by user

        MethodConfig config = new MethodConfig();

        return config;
    }

    public void run() {
        help.debug(this.getClass().getName(), "Dispatcher started..\n");

        if (threadList == null ||
            threadList.isEmpty()) {
            System.err.printf("Not tasks for Portfolio Decision\n");
        }

        //Start threads       
        Iterator<VoterThread> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            VoterThread tmp = iterator.next();
            threadExecutor.execute(tmp);
        }

        help.debug(this.getClass().getName(), "Dispatcher ended.. List:%d\n", threadList.size());
    }


}
