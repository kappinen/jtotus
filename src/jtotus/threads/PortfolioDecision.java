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

import jtotus.methods.MethodEntry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import jtotus.common.Helper;
import jtotus.common.MethodResults;
import jtotus.config.MethodConfig;
import jtotus.engine.Engine;
import jtotus.gui.MethodResultsPrinter;

/**
 *
 * @author Evgeni Kappinen
 */
public class PortfolioDecision implements Runnable{

    private Helper help = null;
    private LinkedList<MethodEntry> threadList;
    private LinkedList<Future<MethodResults>> methodResults = null;
    private ExecutorService threadExecutor = null;



    
    private void init() {
        if (help == null)
            help = Helper.getInstance();
        if(threadList == null)
            threadList = new LinkedList<MethodEntry>();
        if (threadExecutor == null)
            threadExecutor = Executors.newCachedThreadPool();
        if (methodResults == null)
            methodResults = new LinkedList<Future<MethodResults>>();

    }

    public PortfolioDecision() {

        init();
    }

    public PortfolioDecision(LinkedList<MethodEntry> threads) {

        init();


        Iterator<MethodEntry> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            threadList.add(iterator.next());
        }

    }

    public boolean setList(LinkedList<MethodEntry> threads) {


        if (threadList == null) {
            threadList = new LinkedList<MethodEntry>();
        }

        help.debug("PortfolioDecision", "setting list with size:%d\n",
                threads.size());
        
        if (!threadList.isEmpty()) {
            threadList.clear();
        }

        Iterator<MethodEntry> iterator = threads.iterator();
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
        Future<MethodResults> methodResult = null;
        Engine engine = Engine.getInstance();


        
        help.debug("PortfolioDecision", "Dispatcher started..\n");
        
        if (threadList == null ||
            threadList.isEmpty()) {
            System.err.printf("Not tasks for Portfolio Decision\n");
        }

        //Start threads       
        Iterator<MethodEntry> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            MethodEntry tmp = iterator.next();
            if (tmp.isCallable()){
                Callable<MethodResults> callableTmp = (Callable<MethodResults>)tmp;


                methodResult = threadExecutor.submit(callableTmp);
                methodResults.add(methodResult);
            }else {
                //Lets support Runnable for now.
                threadExecutor.execute(tmp);
            }
        }
        help.debug("PortfolioDecision", "Dispatcher ended.. List:%d:%d\n",
                    threadList.size(),methodResults.size());

        //All tasks are executing.. lets wait for results
        while(!methodResults.isEmpty()) {
            Iterator<Future<MethodResults>> taskIter = methodResults.iterator();
            while (taskIter.hasNext()) {
                Future<MethodResults> task = taskIter.next();
                MethodResults result = null;
                if(task.isDone()) {
                     System.out.printf("Run all tasks: the size:"+task.isDone()+"\n");
                    try {
                        result = task.get();

                    } catch (InterruptedException ex) {
                        Logger.getLogger(PortfolioDecision.class.getName()).log(Level.SEVERE, null, ex);
                        taskIter.remove();
                        continue;
                    } catch (ExecutionException ex) {
                        Logger.getLogger(PortfolioDecision.class.getName()).log(Level.SEVERE, null, ex);
                        taskIter.remove();
                        continue;
                    }

                    result.printToConsole();
                    MethodResultsPrinter printer = engine.getResultsPrinter();
                    printer.drawResults(result);

                    
                    //Remove task from the list
                    taskIter.remove();
                    Thread.yield();
                }
            }
        }
        
    }


}
