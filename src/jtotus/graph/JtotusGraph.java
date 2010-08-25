/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 * http://stackoverflow.com/questions/1559958/glitchy-graphing-using-jfreechart
 *
 *
 */

package jtotus.graph;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import jtotus.common.Helper;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author kappiev
 */
public class JtotusGraph implements Runnable{
    private JInternalFrame mainFrame = null;
  //  GraphPrinter lineChart = null;
    protected BlockingQueue<GraphPacket> queue = null;
    private Helper help = Helper.getInstance();
    
    protected int defaultPort = 8888;
    public final Object seriesMap_lock  = new Object();
    String mainReviewTarget = null;
    DatagramSocket serverSocket = null;
    Thread serverThread = null;
    private GraphPrinter lineChart = null;

    
    /* 
     * Secondary Object (DecisionName)
     * TimeSeries
     */
    HashMap <String,TimeSeries>seriesMap = null;




    public JtotusGraph(JInternalFrame tmpFrame, String reviewTarget) {
        mainFrame = tmpFrame;
        seriesMap = new HashMap<String,TimeSeries>();
        queue = new LinkedBlockingQueue<GraphPacket>();

        
        //Initialize Graph
        //lineChart = new GraphPrinter(reviewTarget);
        mainReviewTarget = reviewTarget;
       // mainFrame.setContentPane(lineChart.getContainer());
        lineChart = new GraphPrinter(reviewTarget);
        mainFrame.setContentPane(lineChart.getContainer());
        mainFrame.setDefaultCloseOperation(JInternalFrame.EXIT_ON_CLOSE);
        
        
    }

    synchronized public int getBindPort(){
            if (serverSocket == null) {
                return -1;
            }
            return defaultPort;
        }
    
    synchronized private int addtBindPort(int value){
            return defaultPort+=value;
        }


    public boolean initialize() {
         JtotusGraphDispatcher disp
                 = new JtotusGraphDispatcher(queue);

         if(disp.bindHost()<0) {
             return false;
         }
         
        serverThread = new Thread(disp);

        return true;
    }
    
    public void run() {
         
        GraphPacket packet = null;

        if (serverThread == null){
            if(initialize()==false) {
                return;
            }
        }
        
        serverThread.start();
        
        while(true){
            
            packet = queue.poll();

            if (packet==null){ //Queue is empty
                try {
                    //Queue is empty
                    Thread.sleep(50);
                   // System.err.printf("We are sleeping !!\n");
                } catch (InterruptedException ex) {
                    Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                   // System.err.printf("Thread is waiken up\n");
                }
                continue;
            }

            
            help.debug(this.getClass().getName(),"Drawing %d:%d:%d val:%f queue capacity:%d and contains:%d\n",
                    packet.day, packet.month, packet.year, packet.result, queue.remainingCapacity(),
                    queue.size());

            //Sanity checks
            if (packet.day <=0 || packet.day > 31 ||
                packet.month <=0 || packet.month > 12) {
                System.err.printf("%s incorrect packet format\n", this.getClass().getName());
                continue;
            }

            help.debug(this.getClass().getName(), "The packet:%d.%d.%d: result:%f\n",
                    packet.day, packet.month, packet.year, packet.result);
            //FIXME:change this ugliness
             Day tmpDay = new Day(packet.day,
                                  packet.month,
                                  packet.year);
                

            // add point to seriesMap
            if(seriesMap.containsKey(packet.seriesTitle)) { //Series Exists
                TimeSeries hashSeries = seriesMap.get(packet.seriesTitle);

                //update if already exists
                if (hashSeries.addOrUpdate(tmpDay, packet.result) != null)
                {
                    help.debug(this.getClass().getName(),
                        "Warning overwritting existent value in time series\n");
                }

            } else {  // New series
                 TimeSeries newSeries = new TimeSeries(packet.seriesTitle);
                 newSeries.add(tmpDay, packet.result);
                 seriesMap.put(packet.seriesTitle, newSeries);
                 lineChart.drawSeries(newSeries);
            }
    
        }
        
 
    }


    //Creates Datagram server and reads packet in form of GraphPacket objects
    //and puts them to blocking queue
    private class JtotusGraphDispatcher implements Runnable {

        private BlockingQueue<GraphPacket> mainQueue;
        //private Thread parent = null;


        public JtotusGraphDispatcher(BlockingQueue <GraphPacket>tmpQueue) {
            mainQueue = tmpQueue;
            //parent = tmpThread;
        }



        public int bindHost() {
            int tries = 0;

            while (serverSocket == null && tries < 100) {
                try {
                   serverSocket = new DatagramSocket(defaultPort);
                   help.debug(help.getClass().getName(),
                           ":%s %d\n",serverSocket, defaultPort);

                   if (serverSocket == null) {
                        addtBindPort(tries);
                        tries++;
                    }
                    //seriesMap = new HashMap<String,TimeSeries>();
                } catch (SocketException ex) {
                    addtBindPort(tries);
                    tries++;
                }
           }
          
            if(serverSocket==null)
            {
                return -1;
            }

            return defaultPort;
        }


        
        public void run() {

            final int maxSizeOfPacket = 1024*10*5;
            byte []buf = new byte[maxSizeOfPacket];


           if(serverSocket==null)
           {
               int result = bindHost();
               if (result < 0){
                   return;
               }
           }


           
            //Convert data to object
           
           ObjectInputStream is = null;
           
           DatagramPacket packet = new DatagramPacket(buf, maxSizeOfPacket);
           ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);


           while(true) {
                try {
                  //  byteStream.reset();
                    //Recieve packet
                    serverSocket.receive(packet);

                     is = new ObjectInputStream(new BufferedInputStream(byteStream));

                    GraphPacket obj = (GraphPacket) is.readObject();

                    // add it to blocking queue
                    mainQueue.put(obj);
                    byteStream.reset();

//                    if(parent.getState() == Thread.State.TIMED_WAITING) {
//                        parent.interrupt();
//                    }
                    

                } catch (InterruptedException ex) {
                    Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
            }


            return;
        }
        
    }










    
}
