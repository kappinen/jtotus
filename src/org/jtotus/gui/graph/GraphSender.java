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

import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.engine.Engine;

/**
 *
 * @author Evgeni Kappinen
 */
public final class GraphSender {
    private String mainReviewTarget = null;
    private Engine mainEngine = null;
    private LinkedBlockingDeque<GraphPacket> mainPort = null;

    public GraphSender(String reviewTarget) {
        mainReviewTarget = reviewTarget;
        mainEngine = Engine.getInstance();
        
        mainPort = mainEngine.fetchGraph(mainReviewTarget);
    }

    public boolean sentPacket(String reviewTarget, GraphPacket packetObj) {
        LinkedBlockingDeque<GraphPacket> queue = null;
        
        if (mainReviewTarget.compareTo(reviewTarget) != 0){
            queue = mainEngine.fetchGraph(reviewTarget);
            if (queue == null) {
                System.err.printf("Unable to fetch Graph port!\n");
                return false;
            }
        }else {
            queue = mainPort;
        }

        
        try {
            
            queue.putFirst(packetObj);
        } catch (InterruptedException ex) {
            Logger.getLogger(GraphSender.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
}
