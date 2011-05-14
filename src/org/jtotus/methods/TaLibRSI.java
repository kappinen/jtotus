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
import java.io.File;
import java.util.Date;
import org.jtotus.common.DateIterator;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfTaLibRSI;
import org.jtotus.common.StateIterator;
import org.jtotus.config.ConfigLoader;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibRSI extends TaLibAbstract implements MethodEntry {
    /*Stock list */
    private boolean debug = false;
    protected ConfTaLibRSI config = null;
    public ConfigLoader<ConfTaLibRSI> configFile = null;

    public void loadInputs(String configStock) {

        configFile = new ConfigLoader<ConfTaLibRSI>(super.portfolioConfig.portfolioName
                + File.separator
                + configStock
                + File.separator
                + this.getMethName());

        if (configFile.getConfig() == null) {
            //Load default values
            config = new ConfTaLibRSI();
            configFile.storeConfig(config);
        } else {
            config = configFile.getConfig();
        }
        super.child_config = config;
        configFile.applyInputsToObject(this);
    }

    

    /************* DECISION TEST *************
     * @param evaluator Evaluation object
     * @param stockName ReviewTarget of the method
     * @param input closing price for period
     * @return  void
     *
     */
    public void performDecisionTest(EvaluateMethodSignals evaluator,
                                    String stockName,
                                    double[] input,
                                    int decRSIPeriod,
                                    int lowestThreshold,
                                    int highestThreshold) {

        boolean change=false;
        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double[] output = this.actionRSI(input,
                                        outBegIdx,
                                        outNbElement,
                                        decRSIPeriod);

        if (config.inputPrintResults) {
            DateIterator dateIterator = new DateIterator(portfolioConfig.inputStartingDate,
                    portfolioConfig.inputEndingDate);

            dateIterator.move(outBegIdx.value);
            for (int elem = 0; elem < outNbElement.value && dateIterator.hasNext(); elem++) {
                Date date = dateIterator.next();
                if (output[elem] < lowestThreshold && change == false) {
                    evaluator.buy(input[elem + outBegIdx.value], -1, date);
                    change = true;
                } else if (output[elem] > highestThreshold && change == true) {
                    evaluator.sell(input[elem + outBegIdx.value], -1, date);
                    change = false;
                }
            }
        } else {
            for (int elem = 0; elem < outNbElement.value; elem++) {
                if (output[elem] < lowestThreshold && change == false) {
                    evaluator.buy(input[elem + outBegIdx.value], -1);
                    change = true;
                } else if (output[elem] > highestThreshold && change == true) {
                    evaluator.sell(input[elem + outBegIdx.value], -1);
                    change = false;
                }
            }
        }
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
    

    public MethodResults performRSI(String stockName, double[] input) {

        this.loadInputs(stockName);

        if (debug) {
            System.out.printf("RSI period:%d\n", config.inputRSIPeriod);
        }
        //************* DECISION TEST *************//

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        double[] output = this.actionRSI(input,
                outBegIdx, outNbElement,
                config.inputRSIPeriod);


        methodResults.putResult(stockName, output[output.length - 1]);

        if (config.inputPrintResults) {
            sender = new GraphSender(stockName);
            sender.setPlotName("RSI");
            sender.setSeriesName(this.getMethName());

            DateIterator dateIterator = new DateIterator(portfolioConfig.inputStartingDate,
                                                         portfolioConfig.inputEndingDate);
            dateIterator.move(outBegIdx.value);
            for (int i = 0; i < outNbElement.value && dateIterator.hasNext(); i++) {
                Date stockDate = dateIterator.next();
                sender.addForSending(stockDate, output[i]);
            }
            sender.sendAllStored();
        }

        if (debug) {
            System.out.printf("%s (%s) has %d successrate\n", this.getMethName(),
                    stockName, methodResults.getSuccessRate().intValue());
        }

        return methodResults;

    }
    

    @Override
    public MethodResults performMethod(String stockName, double [] input) {

        //Load config for a stock
        this.loadInputs(stockName);


        //Perform testing if it is asked
        if (config.inputPerfomDecision) {
            EvaluateMethodSignals budjetCounter = new EvaluateMethodSignals();

            for (StateIterator iter = new StateIterator()
                    .addParam("RSIpriod", config.inputRSIDecisionPeriod)
                    .addParam("LowestThreshold", config.inputRSILowestThreshold)
                    .addParam("HigestThreshold", config.inputRSIHigestThreshold);
                    iter.hasNext() != StateIterator.END_STATE; iter.nextState()) {

                budjetCounter.initialize(stockName,
                                        "DecisionRSI",
                                        portfolioConfig.inputAssumedBudjet);

                this.performDecisionTest(budjetCounter,
                                        stockName,
                                        input,
                                        iter.nextInt("RSIpriod"),
                                        iter.nextInt("LowestThreshold"),
                                        iter.nextInt("HigestThreshold"));

                if(budjetCounter.newBest()) {
                    config.inputRSIPeriod = iter.nextInt("RSIpriod");
                }
            }

            this.config.outputSuccessRate = budjetCounter.getProfitInProcents();
            methodResults.putSuccessRate(stockName, budjetCounter.getProfitInProcents());
            this.configFile.storeConfig(config);
            if (debug) {
                budjetCounter.printBestResults();
                budjetCounter.dumpResults();
            }
        }

        //Perform method
        return this.performRSI(stockName, input);
    }

}
