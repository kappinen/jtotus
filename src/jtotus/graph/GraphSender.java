/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.graph;

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
