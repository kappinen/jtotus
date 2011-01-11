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
 */
package brokerwatcher.listeners;

import brokerwatcher.eventtypes.StockTick;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import java.util.Calendar;
import java.util.Date;
import org.jtotus.gui.graph.GraphSender;

/**
 *
 * @author Evgeni Kappinen
 */
public class TickListenerPrinter implements UpdateListener {

    private boolean sendEventToGUI = false;
    private GraphSender sender = new GraphSender(this.getClass().getSimpleName());
    private Calendar cal = Calendar.getInstance();

    public void sendEventsToGui() {
        sendEventToGUI = true;
    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {
        System.out.printf("[%s] " + ebs[0].getUnderlying() + "\n",
                ebs[0].getUnderlying().getClass().getSimpleName());

        if (sendEventToGUI) {
            if (ebs[0].getUnderlying().getClass().getSimpleName().equalsIgnoreCase("HashMap")) {
                StockTick tick = (StockTick) ebs[0].get("tick");

                sender.setSeriesName(tick.getStockName());
                cal.add(Calendar.DATE, 1);
                sender.addForSending(cal.getTime(), tick.getLatestPrice());
                sender.sendAllStored();
            }

        }
    }
}
