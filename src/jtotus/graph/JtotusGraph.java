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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import jtotus.common.Helper;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author kappiev
 */
public class JtotusGraph implements Runnable{
    JInternalFrame mainFrame = null;
    GraphPrinter lineChart = null;
    BlockingQueue<GraphPacket> queue = null;
    Helper help = null;
    
    protected int defaultPort = 8888;
    DatagramSocket serverSocket = null;
    Thread serverThread = null;
    
    /* 
     * Secondary Object (DecisionName)
     * TimeSeries
     */
    HashMap <String,TimeSeries>seriesMap = null;




    public JtotusGraph(JInternalFrame tmpFrame, String reviewTarget) {
        mainFrame = tmpFrame;
        seriesMap = new HashMap<String,TimeSeries>();
        queue = new LinkedBlockingQueue<GraphPacket>();
        help = Helper.getInstance();

        
        //Initialize Graph
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
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
                continue;
            }

            //FIXME:change this ugliness
            System.out.printf("Drawing %d:%d:%d val:%f\n",
                    packet.day, packet.month, packet.year, packet.result);
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
                        "Warning overwritting existent value in time series");
                }

            } else {  // New series
                 TimeSeries newSeries = new TimeSeries(packet.seriesTitle);
                 newSeries.add(tmpDay, packet.result);
                 seriesMap.put(packet.seriesTitle, newSeries);
            }

             
            // Ask drawer to clean screen
            lineChart.cleanChart();

             
            // Draw the series
            Iterator <String>mapIter = seriesMap.keySet().iterator();
            while(mapIter.hasNext()){
                TimeSeries mapSeries = seriesMap.get(mapIter.next());
                lineChart.drawSeries(mapSeries);
            }
        }
        
 
    }


    //Creates Datagram server and reads packet in form of GraphPacket objects
    //and puts them to blocking queue
    class JtotusGraphDispatcher implements Runnable {

        BlockingQueue<GraphPacket> mainQueue;


        public JtotusGraphDispatcher(BlockingQueue <GraphPacket>tmpQueue) {
            mainQueue = tmpQueue;
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

            final int maxSizeOfPacket = 1024*10*10;
            byte []buf = new byte[maxSizeOfPacket];
  

           if(serverSocket==null)
           {
               int result = bindHost();
               if (result < 0){
                   return;
               }
           }

           DatagramPacket packet = new DatagramPacket(buf, maxSizeOfPacket);

           while(true) {
                try {
                    //Recieve packet
                    serverSocket.receive(packet);

                    //Convert data to object
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
                    ObjectInputStream is = new
                        ObjectInputStream(new BufferedInputStream(byteStream));

                    GraphPacket obj = (GraphPacket) is.readObject();

                    System.out.print("Putting to queue\n");
                    // add it to blocking queue
                    mainQueue.put(obj);
                    

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
