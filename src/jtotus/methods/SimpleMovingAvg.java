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
import jtotus.threads.VoterThread;
import java.math.BigDecimal;
/**
 *
 * @author kappiev
 */
public class SimpleMovingAvg implements VoterThread {

    private PortfolioDecision dispatch = null;
    private MethodConfig config = null;
    private String methodName = "SimpleMovinAvg";
    private Helper help = Helper.getInstance();;

    public SimpleMovingAvg(PortfolioDecision tmp) {
        dispatch = tmp;
    }

    public String getMethName() {
        return methodName;
    }

    public void run() {

        config = dispatch.fetchConfig(methodName);

        analyzeFromNowToFrequency();
        return;
    }

    private void analyzeFromNowToFrequency() {
        BigDecimal avr = new BigDecimal(0.0);
        BigDecimal tmp = null;
        long count = 0;

        //FIXME:ensure that asked period will be fetched
        String[] stocks = config.StockNames;
        for (int i = stocks.length - 1; i >= 0; i--) {
            avr.valueOf(0.0);
            count = 0;
            StockType stockType = new StockType(stocks[i]);

            for (int y = 0; count <= (config.day_period - 1) && count < Integer.MAX_VALUE-1; y++) {
                tmp = stockType.fetchClosingPrice(help.dateReduction(help.getTimeNow(), y));
                if (tmp != null) {
                    avr = avr.add(tmp);
                    count++;
                }

            }
            avr = avr.divide(BigDecimal.valueOf(count));
            help.debug(methodName, "%s:%.2f for %d last days\n",
                    stocks[i], avr.floatValue(), count);
        }
    }
}
