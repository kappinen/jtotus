/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 *
 * http://blogs.sun.com/geertjan/entry/netbeans_groovy_editor_in_a
 *
 */

package jtotus.rulebase

import jtotus.common.StockType
import jtotus.engine.SimpleMovingAvg
import jtotus.common.Helper;
import jtotus.engine.Engine;
import jtotus.graph.GraphPacket;

//def reviewTarget = "Fortum Oyj"
//def daysToSearch = 70;


def drawClosingPrice (String reviewTarget, int daysToSearch) {

Engine engine = Engine.getInstance()

stockType = new jtotus.common.StockType(reviewTarget)

sender = new jtotus.graph.GraphSender(engine);
packet = new jtotus.graph.GraphPacket();
packet.seriesTitle = stockType.getName()+"_ClosingPrice";


println packet.day + ":" +packet.month + ":" + packet.year
for (int i=0; i<daysToSearch;i++) {
        sender.sentPacket(stockType.getName(), stockType.fetchPastDayClosingPricePacket(i))
    }


}

drawClosingPrice("Fortum Oyj", 70)
drawClosingPrice("UPM-Kymmene Oyj",100)

println "DONE for StockClosingPrice"

