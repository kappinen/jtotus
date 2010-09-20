/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jtotus.engine;

import jtotus.common.Helper;
import jtotus.config.MethodConfig;
import jtotus.common.StockType;
import jtotus.threads.Dispatcher;
import jtotus.threads.VoterThread;
import java.math.BigDecimal;
/**
 *
 * @author kappiev
 */
public class SimpleMovingAvg implements VoterThread {

    private Dispatcher dispatch = null;
    private MethodConfig config = null;
    private String methodName = "SimpleMovinAvg";
    private Helper help = Helper.getInstance();;

    public SimpleMovingAvg(Dispatcher tmp) {
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
