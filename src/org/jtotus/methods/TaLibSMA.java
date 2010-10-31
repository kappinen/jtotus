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
RSI

RSI on hintaa seuraava oskilaattori, joka saavuttaa 0-100 välisiä arvoja.
Se vertaa viimeisten ylöspäin tapahtuneiden hintamuutosten voimakkuutta
alaspäin suuntautuneisiin hintamuutoksiin. Suosituimmat tarkasteluvälit
ovat 9, 14 ja 25 päivän RSI.

Tulkinta:
- RSI huipussa: korkea arvo (yli 70/noususuhdanteessa yleensä 80) indikoi yliostotilannetta
- RSI pohjassa: matala arvo (alle 30/laskusuhdanteessa yleenäs 20) indikoi aliostotilannetta

Signaalit:
- Osta, kun RSI:n arvo leikkaa aliostorajan alapuolelta
- Myy, kun RSI:n arvo leikkaa yliostorajan yläpuolelta

Vaihtoehtoisesti:
- Osta, kun RSI leikkaa keskilinjan (50) alapuolelta
- Myy, kun RSI leikkaa keskilinjan (50) yläpuolelta



 */
package org.jtotus.methods;

import org.jtotus.common.MethodResults;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.util.Date;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphSender;
import org.apache.commons.lang.ArrayUtils;
import java.io.File;
import org.jtotus.common.StateIterator;
import org.jtotus.config.ConfTaLibSMA;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;
import org.jtotus.methods.evaluators.TimeSeriesCondition;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibSMA extends TaLibAbstract implements MethodEntry {

    /*Stock list */
    private double avgSuccessRate = 0.0f;
    private int totalStocksAnalyzed = 0;
    //INPUTS TO METHOD:
    public ConfTaLibSMA config = null;
    public ConfigLoader<ConfTaLibSMA> configFile = null;

    public TaLibSMA() {
        super();

    }

    public void loadInputs(String configStock) {

        configFile = new ConfigLoader<ConfTaLibSMA>(this.inputPortofolio
                + File.separator
                + configStock
                + File.separator
                + this.getMethName());

        if (configFile.getConfig() == null) {
            //Load default values
            config = new ConfTaLibSMA();
            configFile.storeConfig(config);
        } else {
            config = configFile.getConfig();
        }

        super.child_config = config;
        configFile.applyInputsToObject(this);

    }

    public double[] actionSMA(double[] input,
            int intput_size,
            MInteger outBegIdxDec,
            MInteger outNbElementDec,
            int decSMAPeriod) {

        final Core core = new Core();
        final int allocationSizeDecision = intput_size - core.smaLookback(decSMAPeriod);


        if (allocationSizeDecision <= 0) {
            System.err.printf("No data for period (%d)\n", allocationSizeDecision);
            return null;
        }

        double[] outputDec = new double[allocationSizeDecision];


        RetCode decCode = core.sma(0, intput_size - 1,
                input, decSMAPeriod,
                outBegIdxDec, outNbElementDec,
                outputDec);

        if (decCode.compareTo(RetCode.Success) != 0) {
            //Error return empty method results
            throw new java.lang.IllegalStateException("SMA failed:" + decSMAPeriod
                    + " Begin:" + outBegIdxDec.value
                    + " NumElem:" + outNbElementDec.value + "\n");
        }

        return outputDec;
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
                                    int decSMAPeriod) {

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double[] output = this.actionSMA(input, input.length - 1,
                                            outBegIdx,
                                            outNbElement,
                                            decSMAPeriod);

        int direction = 0;

        DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                                                     config.inputEndingDate.getTime());

        TimeSeriesCondition signals = new TimeSeriesCondition();
        signals.declareFunc("A", input);
        signals.declareFunc("B", output);
        dateIterator.move(outBegIdx.value);
        for (int elem = 1; elem <= outNbElement.value && dateIterator.hasNext(); elem++) {
            Date date = dateIterator.next();
            if (signals.setA(elem + outBegIdx.value).crosses().setB(elem).and().smaller().isTrue()) {
                evaluator.buy(input[elem + outBegIdx.value], -1, date);
            }else if (signals.setA(elem + outBegIdx.value).crosses().setB(elem).and().bigger().isTrue()) {
                evaluator.sell(input[elem + outBegIdx.value], -1, date);
            }
        }
    }

    public MethodResults performSMA(String stockName, double[] input) {

        int inputSize = input.length - 1;

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double[] output = this.actionSMA(input, inputSize,
                                        outBegIdx, outNbElement,
                                        config.inputSMAPeriod);


        methodResults.putResult(stockType.getStockName(), output[output.length - 1]);

        TimeSeriesCondition signals = new TimeSeriesCondition();
        signals.declareFunc("A", input);
        signals.declareFunc("B", output);
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
            sender.setSeriesName(this.getMethName());

            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                                                         config.inputEndingDate.getTime());
            dateIterator.move(outBegIdx.value);
            for (int i = 0; i < outNbElement.value && dateIterator.hasNext(); i++) {
                Date stockDate = dateIterator.next();
                sender.addForSending(stockDate, output[i]);
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
        if (this.inputPerfomDecision) {
            EvaluateMethodSignals budjetCounter = new EvaluateMethodSignals();

            for (StateIterator iter = new StateIterator().addParam("SMAperiod", config.inputSMADecisionPeriod);
                    iter.hasNext() != StateIterator.END_STATE;
                    iter.nextState()) {

                budjetCounter.initialize(stockType.getStockName(),
                                        "DecisionSMA",
                                        super.inputAssumedBudjet,
                                        sender);

                this.performDecisionTest(budjetCounter,
                        stockName,
                        input,
                        iter.nextInt("SMAperiod"));
            }
            
            this.config.outputSuccessRate = budjetCounter.getProfitInProcents();
            methodResults.putSuccessRate(stockName, budjetCounter.getProfitInProcents());
            this.configFile.storeConfig(config);
            budjetCounter.printBestResults();
        }

        //Perform method
        return this.performSMA(stockName, input);
    }
}
