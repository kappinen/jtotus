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
import jtotus.threads.PortfolioDecision;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.Callable;
/**
 *
 * @author Evgeni Kappinen
 */
public class SimpleMovingAvg implements Callable, MethodEntry {

    private MethodConfig config = null;
    private Helper help = Helper.getInstance();;

    
    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public void run() {

        config = new MethodConfig();

        analyzeFromNowToFrequency();
        return;
    }

    private void analyzeFromNowToFrequency() {
        BigDecimal avr = new BigDecimal(0.0);
        BigDecimal tmp = null;
        long count = 0;
        Calendar calendar = Calendar.getInstance();

        //FIXME:ensure that asked period will be fetched
        String[] stocks = config.StockNames;
        for (int i = stocks.length - 1; i >= 0; i--) {
            avr.valueOf(0.0);
            count = 0;
            StockType stockType = new StockType(stocks[i]);

            for (int y = 0; count <= (config.day_period - 1) && count < Integer.MAX_VALUE-1; y++) {

                tmp = stockType.fetchClosingPrice(calendar);
                if (tmp != null) {
                    avr = avr.add(tmp);
                    count++;
                }
                
              calendar.add(Calendar.DATE, -1);

            }

            avr = avr.divide(BigDecimal.valueOf(count));
            help.debug(this.getClass().getName(),
                    "%s:%.2f for %d last days\n",
                    stocks[i], avr.floatValue(), count);

        }
    }

    public boolean isCallable() {
       return false;
    }

    public Object call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
