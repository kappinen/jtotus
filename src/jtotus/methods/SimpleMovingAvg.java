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

package jtotus.methods;

import jtotus.common.Helper;
import jtotus.config.MethodConfig;
import jtotus.common.StockType;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import jtotus.common.DateIterator;
import jtotus.gui.graph.GraphPacket;
import jtotus.gui.graph.GraphSender;
/**
 *
 * @author Evgeni Kappinen
 */
public class SimpleMovingAvg implements Callable, MethodEntry {

    private MethodConfig config = null;
    private Helper help = Helper.getInstance();;


    private boolean printResults = true;
    
    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public void run() {

        config = new MethodConfig();

        Calendar startingDate = Calendar.getInstance();
        startingDate.add(Calendar.DATE, -100);
        DateIterator dateIterator = new DateIterator(startingDate.getTime());
        while(dateIterator.hasNext()) {
            analyzeFromNowToFrequency(dateIterator.next());
        }

        return;
    }

    private void analyzeFromNowToFrequency(Date date) {
        BigDecimal avr = new BigDecimal(0.0);
        BigDecimal tmp = null;
        long count = 0, failures=0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        //FIXME:ensure that asked period will be fetched
        String[] stocks = config.StockNames;
        for (int i = stocks.length - 1; i >= 0; i--) {
            avr.valueOf(0.0);
            count = 0;
            StockType stockType = new StockType(stocks[i]);

            for (int y = 0; count <= (config.day_period - 1) && failures < 100 ; y++) {

                tmp = stockType.fetchClosingPrice(calendar);
                if (tmp != null) {
                    avr = avr.add(tmp);
                    count++;

                } else { failures++; }
                
              calendar.add(Calendar.DATE, -1);

            }

            avr = avr.divide(BigDecimal.valueOf(count));
            help.debug(this.getClass().getName(),
                    "%s:%.2f for %d last days\n",
                    stocks[i], avr.floatValue(), count);

            if (printResults) {
                GraphSender sender = new GraphSender();
                GraphPacket packet = new GraphPacket();

                packet.seriesTitle = this.getMethName();
                packet.result = avr.doubleValue();
                packet.day = calendar.get(Calendar.DATE);
                packet.month = calendar.get(Calendar.MONTH) + 1;
                packet.year = calendar.get(Calendar.YEAR);

                sender.sentPacket(stockType.getName(), packet);
             }
        }
    }

    public boolean isCallable() {
       return false;
    }

    public Object call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}