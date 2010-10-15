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
 *
 * http://tutorials.jenkov.com/java-collections/navigableset.html
 */
package org.jtotus.gui.graph;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.common.DateIterator;
import org.jtotus.engine.Engine;

/**
 *
 * @author Evgeni Kappinen
 */
public class GraphSender {

    private Engine mainEngine = null;
    private int mainPort = 0;
    private DatagramSocket clientSock = null;

    class ThreadSender implements Runnable {

        private int startingPoint = 0;
        private int endingPoint = 0;
        private double dataToSend[] = null;
        private DateIterator dateIterator = null;
        private String methodName = null;
        private GraphPacket packet = null;
        private int mainPort = 0;

        public  ThreadSender(String methName, int port) {
            methodName = methName;
            mainPort = port;
        }

        public void executeTask(double data[],
                                int start, int end,
                                Calendar startDate, Calendar endDate) {

            startingPoint = start;
            endingPoint = end;

            dataToSend = new double[end - start + 1];
            for (int i = start; i < end; i++) {
                dataToSend[i - start] = data[i];
            }

            dateIterator = new DateIterator(startDate.getTime(),
                    endDate.getTime());
        }

        public void run() {

            packet = new GraphPacket();
            dateIterator.move(startingPoint);
            for (int i = 0; i < endingPoint - startingPoint
                   && dateIterator.hasNext(); i++) {
                Date stockDate = dateIterator.next();
                //System.out.printf("Date:"+stockDate+" Time:"+inputEndingDate.getTime()+"Time2:"+inputStartingDate.getTime()+"\n");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(stockDate);

                packet.seriesTitle = methodName;
                packet.result = dataToSend[i];
                packet.date = stockDate.getTime();
            }
        }
    }

    public void executeTask(String methName,
            double data[],
            int start, int end,
            Calendar startDate, Calendar endDate) {

        ThreadSender task = new ThreadSender(methName, mainPort);
        task.executeTask(data, start, end, startDate, endDate);
        Thread thread = new Thread(task);
        thread.start();
    }

    public GraphSender(int port) {
        mainPort = port;
    }

    public GraphSender(String reviewTarget) {
        mainEngine = Engine.getInstance();
        mainPort = mainEngine.fetchGraph(reviewTarget);

    }

    public GraphSender() {
    }

    public boolean sentPacket(String reviewTarget, GraphPacket packetObj) {
        int port = 0;
        ObjectOutputStream os = null;

        if (packetObj == null || reviewTarget == null) {
            return false;
        }


        if (mainEngine != null) {
            port = mainEngine.fetchGraph(reviewTarget);
            if (port <= 0) {
                System.err.printf("Unable to fetch Graph port!\n");
                return false;
            }
            mainPort = port;
        } else if (mainPort != 0) {
            port = mainPort;
        } else {
            mainEngine = Engine.getInstance();
            port = mainEngine.fetchGraph(reviewTarget);
            if (port <= 0) {
                System.err.printf("Unable to fetch Graph port!\n");
                return false;
            }
        }

        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");

            clientSock = new DatagramSocket();

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024 * 7);

            os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject(packetObj);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, port);

            clientSock.send(packet);

            os.close();
            clientSock.close();

        } catch (IOException ex) {
            Logger.getLogger(GraphSender.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }


        return true;
    }
}
