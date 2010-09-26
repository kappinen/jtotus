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

package jtotus.gui.graph;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.engine.Engine;

/**
 *
 * @author kappiev
 */
public class GraphSender {
    private Engine mainEngine = null;
    private int mainPort = 0;
    private DatagramSocket clientSock = null;

    public GraphSender(Engine tmpEngine){
        mainEngine = tmpEngine;
    }

    public GraphSender(int port){
        mainPort = port;
    }


    public boolean sentPacket(String reviewTarget, GraphPacket packetObj){
        int port=0;
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
        } else if(mainPort != 0) {
            port = mainPort;
        } else {
            return false;
        }
        
        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");


            clientSock = new DatagramSocket();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024 * 10);

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
