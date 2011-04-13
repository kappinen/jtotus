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
package org.jtotus.threads;

import org.jtotus.methods.MethodEntry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jtotus.common.Helper;
import org.jtotus.common.MethodResults;
import org.jtotus.config.ConfPortfolio;

/**
 *
 * @author Evgeni Kappinen
 */
public class PortfolioDecision implements Runnable {

    private Helper help = null;
    private LinkedList<MethodEntry> threadList = null;
    private ExecutorService threadExecutor = null;
    

    private void init() {
        if (help == null) {
            help = Helper.getInstance();
        }
        if (threadList == null) {
            threadList = new LinkedList<MethodEntry>();
        }
        if (threadExecutor == null) {
            threadExecutor = Executors.newCachedThreadPool();
        }
        
    }

    public PortfolioDecision() {
        super();
        init();
    }

    public void addLongTermMethod(MethodEntry entry) {
        ConfPortfolio portfolioConfig = ConfPortfolio.getPortfolioConfig();
        threadList.add(entry);
        
        if (portfolioConfig.isAutoStared(entry.getMethName())) {
            startTask(entry);
        }
    }

    public PortfolioDecision(LinkedList<MethodEntry> threads) {

        init();

        Iterator<MethodEntry> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            threadList.add(iterator.next());
        }

    }

    public void startLongTermMethods(LinkedList<String> methodNames) {

        Iterator<MethodEntry> methodIter = threadList.iterator();
        while (methodIter.hasNext()) {
            MethodEntry entry = methodIter.next();
            if (methodNames.contains(entry.getMethName())){
                startTask(entry);
            }
        }
    }
  
    public void run() {

        help.debug("PortfolioDecision", "Dispatcher started..\n");

        if (threadList.isEmpty()) {
            System.err.printf("Not tasks for Portfolio Decision\n");
        }

        //Start threads       
        Iterator<MethodEntry> iterator = threadList.iterator();
        MethodFuture<MethodResults> futureTask = null;
        InterfaceMethodListner methodListener = null;
        while (iterator.hasNext()) {
            MethodEntry task = iterator.next();

            if (task.isCallable()) {
                //Callable<MethodResults> callableTmp = task;
                futureTask = new MethodFuture<MethodResults>(task);
                methodListener = new MethodListener();
                futureTask.addListener(methodListener);

                threadExecutor.execute(futureTask);
            } else {
                //Lets support Runnable for now.
                threadExecutor.execute(task);
            }
        }
    }

    private void startTask(MethodEntry task) {

        //Start threads
        MethodFuture<MethodResults> futureTask = null;
        InterfaceMethodListner methodListener = null;

        if (task.isCallable()) {
            //Callable<MethodResults> callableTmp = task;
            futureTask = new MethodFuture<MethodResults>(task);
            methodListener = new MethodListener();
            futureTask.addListener(methodListener);

            threadExecutor.execute(futureTask);
        } else {
            //Lets support Runnable for now.
            threadExecutor.execute(task);
        }
    }

    public LinkedList<MethodEntry> getMethodList() {
        return threadList;
    }

}
