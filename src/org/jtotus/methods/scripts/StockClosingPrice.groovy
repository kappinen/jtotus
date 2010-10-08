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
 * http://blogs.sun.com/geertjan/entry/netbeans_groovy_editor_in_a
 *
 */

package jtotus.rulebase

import org.jtotus.common.StockType
import org.jtotus.methods.SimpleMovingAvg
import org.jtotus.common.Helper;
import org.jtotus.engine.Engine;
import org.jtotus.gui.graph.GraphPacket;
import org.jtotus.config.MethodConfig;



def drawClosingPrice (String reviewTarget, int daysToSearch) {


stockType = new org.jtotus.common.StockType(reviewTarget)

sender = new org.jtotus.gui.graph.GraphSender();
packet = new org.jtotus.gui.graph.GraphPacket();
packet.seriesTitle = stockType.getName()+"_ClosingPrice";


println packet.day + ":" +packet.month + ":" + packet.year
for (int i=0; i<daysToSearch;i++) {
        sender.sentPacket(stockType.getName(), stockType.fetchPastDayClosingPricePacket(i))
    }


}


MethodConfig method = new MethodConfig();
Iterator<String> iter = method.iterator();

while(iter.hasNext()) {
    drawClosingPrice(iter.next(), 100)
}


println "DONE for StockClosingPrice"

