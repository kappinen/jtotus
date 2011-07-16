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
package org.jlucrum.realtime.listeners;

import org.jlucrum.realtime.BrokerWatcher;
import org.jlucrum.realtime.eventtypes.EsperEventRsi;
import org.jlucrum.realtime.eventtypes.IndicatorData;
import org.jlucrum.realtime.eventtypes.StockTick;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.common.StateIterator;
import org.jtotus.config.ConfTaLibRSI;
import org.jtotus.config.ConfigLoader;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;

/**
 *
 * @author Evgeni Kappinen
 */
public class ListenerRsiIndicator implements UpdateListener {
    //FIXME:read from somewhere else budjet.
    private double assumedBudjet = 7000;
    private HashMap<String, ArrayList<Double>> stockRsi = null;
    private HashMap<String, ConfTaLibRSI> configRsi = null;
    private HashMap<String, EvaluateMethodSignals> budjetCounter = null;
    private EPRuntime esperRuntime = null;

    private void initialize() {
        stockRsi = new HashMap<String, ArrayList<Double>>();
        configRsi = new HashMap<String, ConfTaLibRSI>();
        budjetCounter = new HashMap<String, EvaluateMethodSignals>();
    }

    public ListenerRsiIndicator() {
        this.initialize();
        configRsi = new HashMap<String, ConfTaLibRSI>();
    }

    public ListenerRsiIndicator(EPRuntime esperRuntime) {
        this.initialize();
        this.esperRuntime = esperRuntime;
    }

    private EvaluateMethodSignals getBudjetCounter(String stockName) {

        EvaluateMethodSignals eval = budjetCounter.get(stockName);
        if (eval == null) {
            eval = new EvaluateMethodSignals();
            eval.initialize(stockName,
                    "DecisionRSI",
                    assumedBudjet);

            budjetCounter.put(stockName, eval);
        }

        return eval;
    }

    private EPRuntime getEngine() {
        if (esperRuntime == null) {
            esperRuntime = BrokerWatcher.getMainEngine().getEPRuntime();
        }

        return esperRuntime;
    }


    //FIXME: see at original RSI implementation
    public ConfTaLibRSI getConfig(String stockName) {
        ConfTaLibRSI config = null;

        config = configRsi.get(stockName);
        if (config == null) {
            ConfigLoader<ConfTaLibRSI> loader 
                    = new ConfigLoader<ConfTaLibRSI>(stockName);

            config = loader.getConfig();
            if (config == null) {
                config = new ConfTaLibRSI();
                //FIXME:
                //loader.storeConfig(config);
                configRsi.put(stockName, config);
            }
        }

        return config;
    }

    public double[] actionRSI(double[] input,
                              MInteger outBegIdxDec,
                              MInteger outNbElementDec,
                              int decRSIPeriod) {

        int intput_size = input.length - 1;
        final Core core = new Core();
        final int allocationSizeDecision = intput_size - core.rsiLookback(decRSIPeriod);


        if (allocationSizeDecision <= 0) {
            System.err.printf("No data for period (%d)\n", allocationSizeDecision);
            return null;
        }

        double[] outputDec = new double[allocationSizeDecision];


        RetCode decCode = core.rsi(0, intput_size - 1,
                input, decRSIPeriod,
                outBegIdxDec, outNbElementDec,
                outputDec);

        if (decCode.compareTo(RetCode.Success) != 0) {
            //Error return empty method results
            throw new java.lang.IllegalStateException("RSI failed:" + decRSIPeriod
                    + " Begin:" + outBegIdxDec.value
                    + " NumElem:" + outNbElementDec.value + "\n");
        }

        return outputDec;
    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {

        //Update list of the price
        for (EventBean eb : ebs) {
            StockTick tick = (StockTick) eb.getUnderlying();
            ArrayList<Double> list = stockRsi.get(tick.getStockName());
            if (list == null) {
                list = new ArrayList<Double>();
                stockRsi.put(tick.getStockName(), list);
            }
            list.add(tick.getLatestSell());

        }

        for (EventBean eb : ebs) {
            StockTick tick = (StockTick) eb.getUnderlying();
            ArrayList<Double> stockTicks = stockRsi.get(tick.getStockName());


            if (stockTicks.size() > getConfig(tick.getStockName()).inputRSIPeriod + 1) {
                double[] input = ArrayUtils.toPrimitive(stockTicks.toArray(new Double[0]));

                makeTest(tick.getStockName(), input);

                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();

                double[] output = this.actionRSI(input,
                        outBegIdx, outNbElement,
                        getConfig(tick.getStockName()).inputRSIPeriod);


                if (output[output.length - 1] < getConfig(tick.getStockName()).outputRSILowestThreshold) {
                    //    System.err.printf("[%s] buy for: %f rsi:%d\n",tick.getStockName(), tick.getLatestSell(), getConfig(tick.getStockName()).outputRSILowestThreshold);
                    getBudjetCounter(tick.getStockName()).buy(tick.getLatestSell(), -1);
                } else if (output[output.length - 1] > getConfig(tick.getStockName()).outputRSIHigestThreshold) {
                    getBudjetCounter(tick.getStockName()).buy(tick.getLatestBuy(), -1);
                }
                //System.out.printf("Size of output:%d rsi:%d\n", output.length, getConfig(tick.getStockName()).inputRSIPeriod);

                EsperEventRsi rsiEvent = new EsperEventRsi();
                rsiEvent.setStockName(tick.getStockName());
                rsiEvent.setRsi(output[output.length - 1]);

                this.getEngine().sendEvent(rsiEvent);

                //Sending Indicator Data
                IndicatorData data = new IndicatorData();
                data.setStockName(tick.getStockName());
                data.setIndicatorValue(output[output.length - 1]);
                data.setIndicatorName("RSI");

                this.getEngine().sendEvent(data);


                getBudjetCounter(tick.getStockName()).dumpResults();
            }
        }
    }

    public void makeTest(String stockName, double[] input) {
        EvaluateMethodSignals budjetCounter = new EvaluateMethodSignals();

        for (StateIterator iter = new StateIterator()
                .addParam("RSIpriod", getConfig(stockName).inputRSIDecisionPeriod)
                .addParam("LowestThreshold", getConfig(stockName).inputRSILowestThreshold)
                .addParam("HigestThreshold", getConfig(stockName).inputRSIHigestThreshold);
                iter.hasNext() != StateIterator.END_STATE; iter.nextState()) {

            budjetCounter.initialize(stockName,
                                    "DecisionRSI",
                                    assumedBudjet);

            this.performDecisionTest(budjetCounter,
                                    input,
                                    iter.nextInt("RSIpriod"),
                                    iter.nextInt("LowestThreshold"),
                                    iter.nextInt("HigestThreshold"));

            if (budjetCounter.newBest()) {
                getConfig(stockName).inputRSIPeriod = iter.nextInt("RSIpriod");
                getConfig(stockName).outputRSIHigestThreshold = iter.nextInt("LowestThreshold");
                getConfig(stockName).outputRSILowestThreshold = iter.nextInt("HigestThreshold");
            }
        }

        getConfig(stockName).outputSuccessRate = budjetCounter.getProfitInProcents();
        //budjetCounter.dumpResults();
    }

    /************* DECISION TEST *************
     * @param evaluator Evaluation object
     * @param input closing price for period
     *
     * @return  void
     */
    public void performDecisionTest(EvaluateMethodSignals evaluator,
                                    double[] input,
                                    int decRSIPeriod,
                                    int lowestThreshold,
                                    int highestThreshold) {

        if (input.length < decRSIPeriod) {
            return;
        }

        boolean change=false;
        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double[] output = this.actionRSI(input,
                                         outBegIdx,
                                         outNbElement,
                                         decRSIPeriod);

        for (int elem = 0; elem < outNbElement.value; elem++) {
            if (output[elem] < lowestThreshold && change == false) {
                evaluator.buy(input[elem + outBegIdx.value], -1);
                change=true;
            } else if (output[elem] > highestThreshold && change == true) {
                evaluator.sell(input[elem + outBegIdx.value], -1);
                change=false;
            }
        }
    }


}
