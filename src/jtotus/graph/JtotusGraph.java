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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author kappiev
 */
public class JtotusGraph implements Runnable{
    JInternalFrame mainFrame = null;
    DatagramSocket serverSocket = null;
    int defaultPort = 8888;
    GraphPrinter lineChart = null;

    /* Main Object (StocksName),
     * Secondary Object (DecisionName)
     * TimeSeries
     */
    HashMap <String,TimeSeries>seriesMap = null;




    public JtotusGraph(JInternalFrame tmpFrame) {
        mainFrame = tmpFrame;
        seriesMap = new HashMap<String,TimeSeries>();

        //Initialize Graph
        lineChart = new GraphPrinter();
        mainFrame.setContentPane(lineChart.getContainer());

        int tries = 0;
        while (serverSocket == null && tries > 100) {
            try {
               serverSocket = new DatagramSocket(defaultPort);
               defaultPort+=tries;

                //seriesMap = new HashMap<String,TimeSeries>();
            } catch (SocketException ex) {
               tries++;
            }
       }

        
    }



    
    public void run() {
        final int maxSizeOfPacket = 1024;
        byte []buf = new byte[maxSizeOfPacket];

        DatagramPacket packet = new DatagramPacket(buf, maxSizeOfPacket);


        while(true){
            try {
                //Recieve packet
                serverSocket.receive(packet);

                //Extract Data
                // read and write to blocking queue
                // this thread will draw the actual lines
                // by using blockingqueue
                //Draw line


            } catch (IOException ex) {
                Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }

    }
    

}
