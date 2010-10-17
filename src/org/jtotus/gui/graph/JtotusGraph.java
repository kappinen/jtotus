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
 * http://stackoverflow.com/questions/1559958/glitchy-graphing-using-jfreechart
 *
 *
 */
package org.jtotus.gui.graph;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import org.jtotus.common.Helper;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

/**
 *
 * @author kappiev
 */
public class JtotusGraph implements Runnable {

    private JInternalFrame mainFrame = null;
    protected LinkedBlockingDeque<GraphPacket> queue = null;
    private Helper help = Helper.getInstance();
    public final Object seriesMap_lock = new Object();
    protected int defaultPort = 8888;
    protected String mainReviewTarget = null;
    protected DatagramSocket serverSocket = null;
    protected HashMap<String, TimeSeries> seriesMap = null;
    private Thread serverThread = null;
    private GraphPrinter lineChart = null;

    public JtotusGraph(JInternalFrame tmpFrame, String reviewTarget) {
        mainFrame = tmpFrame;
        seriesMap = new HashMap<String, TimeSeries>();
        queue = new LinkedBlockingDeque<GraphPacket>();


        //Initialize Graph
        mainReviewTarget = reviewTarget;
        lineChart = new GraphPrinter(reviewTarget);

        mainFrame.setName(reviewTarget);
        mainFrame.setTitle(reviewTarget);
        mainFrame.setContentPane(lineChart.getContainer());
        //mainFrame.setDefaultCloseOperation(JInternalFrame.EXIT_ON_CLOSE);

    }

    synchronized public int getBindPort() {
        if (serverSocket == null) {
            return -1;
        }
        return defaultPort;
    }

    synchronized private int addtBindPort(int value) {
        defaultPort += value;

        return defaultPort;
    }

    public boolean initialize() {
        JtotusGraphDispatcher dispatcher = new JtotusGraphDispatcher();

        if (dispatcher.bindHost() < 0) {
            return false;
        }

        serverThread = new Thread(dispatcher);

        return true;
    }

    public void run() {

        GraphPacket packet = null;

        if (serverThread == null) {
            if (initialize() == false) {
                return;
            }
        }

        serverThread.start();

        while (true) {
            try {
                packet = queue.takeLast();
            } catch (InterruptedException ex) {
                //Should not happend.
                Logger.getLogger(JtotusGraph.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }


            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(packet.date);

            Day tmpDay = new Day(cal.get(Calendar.DATE),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR));


            // add point to seriesMap
            if (seriesMap.containsKey(packet.seriesTitle)) { //Series Exists
                TimeSeries hashSeries = seriesMap.get(packet.seriesTitle);

                //update if already exists
                if (hashSeries.addOrUpdate(tmpDay, packet.result) != null) {
                    help.debug("JtotusGraph",
                            "Warning overwritting existent value in time series\n");
                }

            } else {  // New series
                TimeSeries newSeries = new TimeSeries(packet.seriesTitle);
                newSeries.add(tmpDay, packet.result);
                seriesMap.put(packet.seriesTitle, newSeries);
                lineChart.setRenderer(seriesMap.size(), packet.type);
                lineChart.drawSeries(newSeries);
            }

        }


    }

    //Creates Datagram server and reads packet in form of GraphPacket objects
    //and puts them to blocking queue
    private class JtotusGraphDispatcher implements Runnable {

        public int bindHost() {
            int tries = 0;

            while (serverSocket == null && tries < 100) {
                try {
                    serverSocket = new DatagramSocket(defaultPort);
                    help.debug(help.getClass().getName(),
                            ":%s %d\n", serverSocket, defaultPort);

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

            if (serverSocket == null) {
                return -1;
            }

            return defaultPort;
        }

        public void run() {

            final int maxSizeOfPacket = 1024 * 4;
            byte[] buf = new byte[maxSizeOfPacket];


            if (serverSocket == null) {
                int result = bindHost();
                if (result < 0) {
                    return;
                }
            }



            //Convert data to object

            ObjectInputStream is = null;

            DatagramPacket packet = new DatagramPacket(buf, maxSizeOfPacket);
            ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);


            while (true) {
                try {
                    //  byteStream.reset();
                    //Recieve packet
                    serverSocket.receive(packet);

                    is = new ObjectInputStream(new BufferedInputStream(byteStream));

                    GraphPacket obj = (GraphPacket) is.readObject();

                    if (obj.type == GraphSeriesType.SIMPLEBUBLE) // add it to blocking queue
                    {
                        queue.putFirst(obj);
                    }
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
