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
/*
 *
https://www.nordnet.fi/mux/page/hjalp/ordHjalp.html?ord=diagram%20rsi
 *
MACD




 */
package org.jtotus.methods;

import org.jtotus.common.MethodResults;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.io.File;
import java.util.Date;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfTaLibMACD;
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.common.StateIterator;
import org.jtotus.config.ConfigLoader;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibMACD extends TaLibAbstract implements MethodEntry {
    /*Stock list */

    protected ConfTaLibMACD config = null;
    public ConfigLoader<ConfTaLibMACD> configFile = null;

    public void loadInputs(String configStock) {

        configFile = new ConfigLoader<ConfTaLibMACD>(super.inputPortofolio
                + File.separator
                + configStock
                + File.separator
                + this.getMethName());

        if (configFile.getConfig() == null) {
            //Load default values
            config = new ConfTaLibMACD();
            configFile.storeConfig(config);
        } else {
            config = (ConfTaLibMACD) configFile.getConfig();
        }
        super.child_config = config;
        configFile.applyInputsToObject(this);
    }



    /************* DECISION TEST *************
     * @param evaluator Evaluation object
     * @param stockName ReviewTarget of the method
     * @param input closing price for period
     * @param stateConfig Contains all information
     *                    about a state for particular test.
     *
     * @return  void
     */
    public void performDecisionTest(EvaluateMethodSignals evaluator,
                                    String stockName,
                                    double[] input,
                                    int fastPeriod,
                                    int slowPeriod,
                                    int signalPeriod) {

        boolean change=false;
        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double []macd = new double [input.length - 1];
        double []macdSignal = new double [input.length - 1];
        double []macdHis = new double [input.length - 1];

        double[] output = this.actionMACD(input,
                                        outBegIdx, outNbElement,
                                        macd, macdSignal,macdHis,
                                        fastPeriod, slowPeriod, signalPeriod);

    }


    public double[] actionMACD(double[] input,
                                MInteger outBegIdxDec,
                                MInteger outNbElementDec,
                                double []macd,
                                double []macdSignal,
                                double []macdHis,
                                int fastPeriod,
                                int slowPeriod,
                                int signalPeriod) {

        int intput_size = input.length - 1;
        final Core core = new Core();
        final int allocationSizeDecision = intput_size - core.macdLookback(fastPeriod,
                                                                           slowPeriod,
                                                                           signalPeriod);


        if (allocationSizeDecision <= 0) {
            System.err.printf("No data for period (%d)\n", allocationSizeDecision);
            return null;
        }

        RetCode decCode = core.macd(0, intput_size - 1,
                input, fastPeriod, slowPeriod, signalPeriod,
                outBegIdxDec, outNbElementDec,
                macd, macdSignal, macdHis);

        if (decCode.compareTo(RetCode.Success) != 0) {
            //Error return empty method results
            throw new java.lang.IllegalStateException("MACD failed:"
                    + " Fast: " +fastPeriod+" Slow: "+slowPeriod+" Signal: "+signalPeriod
                    + " Begin:" + outBegIdxDec.value
                    + " NumElem:" + outNbElementDec.value + "\n");
        }

        return macd;
    }


    public MethodResults performMACD(String stockName, double[] input) {
        
        stockType.setStockName(stockName);
        this.loadInputs(stockName);

        System.out.printf("Periods fast:%d slow:%d signal:%d\n",
                                         config.inputMACDFastPeriod,
                                         config.inputMACDSlowPeriod,
                                         config.inputMACDSignalPeriod);

        //************* DECISION TEST *************//

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double []macd = new double [input.length - 1];
        double []macdSignal = new double [input.length - 1];
        double []macdHis = new double [input.length - 1];
        
        double[] output = this.actionMACD(input,
                                        outBegIdx, outNbElement,
                                        macd, macdSignal, macdHis,
                                        config.inputMACDFastPeriod,
                                        config.inputMACDSlowPeriod,
                                        config.inputMACDSignalPeriod);


        methodResults.putResult(stockType.getStockName(), macd[outNbElement.value - 1]);

        sender = new GraphSender(stockType.getStockName());
        for (int elem = 0; elem <= outNbElement.value; elem++) {
            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                    config.inputEndingDate.getTime());
            dateIterator.move(elem + outBegIdx.value);
            sender.setSeriesName("Original");
            sender.addForSending(dateIterator.getCurrent(), input[elem + outBegIdx.value]);

        }
        sender.sendAllStored();


        if (config.inputPrintResults) {
            sender = new GraphSender(stockType.getStockName());
            sender.setPlotName("MACD");
            sender.setSeriesName(this.getMethName());

            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                    config.inputEndingDate.getTime());
            dateIterator.move(outBegIdx.value);
            for (int i = 0; i < outNbElement.value && dateIterator.hasNext(); i++) {
                Date stockDate = dateIterator.next();
                sender.addForSending(stockDate, macd[i]);
            }
            sender.sendAllStored();
        }

        System.out.printf("%s (%s) has %d successrate\n", this.getMethName(),
                stockType.getStockName(), methodResults.getSuccessRate().intValue());

        return methodResults;

    }


    @Override
    public MethodResults performMethod(String stockName) {
        List<Double> closingPrices = null;

        //Load config for a stock
        this.loadInputs(stockName);
        //Create period
        closingPrices = super.createClosingPriceList(stockName,
                config.inputStartingDate,
                config.inputEndingDate);
        double[] input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));

        //Perform testing if it is asked
        if (false) {
            EvaluateMethodSignals budjetCounter = new EvaluateMethodSignals();

            for (StateIterator iter = new StateIterator()
                    .addParam("macdFastPeriod", config.inputDecisionFastPeriod)
                    .addParam("macdSlowPeriod", config.inputDecisionSlowPeriod)
                    .addParam("macdSignal", config.inputDecisionSinal);
                    iter.hasNext() != StateIterator.END_STATE;
                iter.nextState()) {

                budjetCounter.initialize(stockType.getStockName(),
                                        "DecisionMACD",
                                        super.inputAssumedBudjet);

                this.performDecisionTest(budjetCounter,
                                        stockName,
                                        input,
                                        iter.nextInt("macdFastPeriod"),
                                        iter.nextInt("macdSlowPeriod"),
                                        iter.nextInt("macdSignal"));

                if(budjetCounter.newBest()) {
                    config.inputMACDFastPeriod = iter.nextInt("macdFastPeriod");
                    config.inputMACDSlowPeriod = iter.nextInt("macdSlowPeriod");
                    config.inputMACDSignalPeriod = iter.nextInt("macdSignal");
                }
            }

            this.config.outputSuccessRate = budjetCounter.getProfitInProcents();
            methodResults.putSuccessRate(stockName, budjetCounter.getProfitInProcents());
            this.configFile.storeConfig(config);
            budjetCounter.printBestResults();
            budjetCounter.dumpResults();
        }

        //Perform method
        return this.performMACD(stockName, input);
    }
}
