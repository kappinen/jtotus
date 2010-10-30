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
import java.math.BigDecimal;
import org.jtotus.common.StateIterator;
import org.jtotus.config.ConfTaLibSMA;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;

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

        // new ConfigLoader<ConfTaLibSMA>(portfolio+File.separator+this.getMethName());

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


    public double[] actionSMA(double []input, 
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
            throw new java.lang.IllegalStateException("SMA failed:" + decSMAPeriod +
                                        " Begin:"+outBegIdxDec.value+
                                        " NumElem:"+outNbElementDec.value+"\n");
        }
        
        return outputDec;
    }


    public void performDecisionTest(String stockName) {
        List<Double> closingPrices = null;
        int period = 0;

        this.loadInputs(stockName);

        closingPrices = super.createClosingPriceList(stockName,
                                                     config.inputStartingDate,
                                                     config.inputEndingDate);
        double[] input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));
        period = input.length - 1;
        
        //************* DECISION TEST *************//

            EvaluateMethodSignals budjetCounter = new EvaluateMethodSignals();

            double bestAssumedBudjet = 0;
            double bestPeriod = 0;
            int decSMAPeriod = 0;

            for (StateIterator iter = new StateIterator()
                    .addParam("SMAperiod", config.inputSMADecisionPeriod);
                    iter.hasNext() != StateIterator.END_STATE;
                    iter.nextState())
            {
                budjetCounter.initialize(super.inputAssumedBudjet);

                MInteger outBegIdxDec = new MInteger();
                MInteger outNbElementDec = new MInteger();

                decSMAPeriod = iter.nextInt("SMAperiod");
                double []outputDec = this.actionSMA(input, period,
                                                   outBegIdxDec, outNbElementDec,
                                                   decSMAPeriod);

                int direction = 0;
                boolean changed = true;
                for (int elem = 1; elem < outNbElementDec.value; elem++) {

                    double threshold = (outputDec[elem - 1] + outputDec[elem]) / 2;
                    changed = false;
                    if (input[outBegIdxDec.value + elem] > threshold) { //above the line
                        if (direction == -1) {
                            changed = true; //Price is going down
                                budjetCounter.sell(input[elem + outBegIdxDec.value], -1);

                                if (bestAssumedBudjet < budjetCounter.getCurrentBestBudjet().doubleValue()) {
                                    bestAssumedBudjet = budjetCounter.getCurrentBestBudjet().doubleValue();
                                    bestPeriod = decSMAPeriod;
                                }
                        }

                        direction = 1;
                    } else {
                        if (direction == 1) {
                            changed = true; //Price is going up
                            budjetCounter.buy(input[elem + outBegIdxDec.value], -1);
                        }
                        direction = -1;
                    }

                    if (changed) {
                    BigDecimal budjet = budjetCounter.getCurrentBestBudjet();
                   // System.out.printf("The budjet is:%f trade:%d\n",
                     //       budjet.doubleValue(), budjetCounter.getStatActions());

                        if (this.inputPrintResults && decSMAPeriod == config.inputSMAPeriod) {
                            sender = new GraphSender(stockType.getStockName());
                            sender.setSeriesName("DecisionSMA");

                            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                                                                         config.inputEndingDate.getTime());
                            dateIterator.move(elem + outBegIdxDec.value);
                            sender.addForSending(dateIterator.getCurrent(), input[elem + outBegIdxDec.value] + 0.05);
                            sender.sendAllStored();
                        }
                    }

                }

            }

            Double successRate = ((bestAssumedBudjet / this.inputAssumedBudjet) - 1) * 100;
            System.out.printf("%s:The best period:%f best budjet:%f pros:%f\n",
                    stockType.getStockName(), bestPeriod, bestAssumedBudjet,
                    successRate.doubleValue());

            totalStocksAnalyzed++;
            this.avgSuccessRate += successRate;
            this.config.outputSuccessRate = successRate;
            this.config.inputSMAPeriod = (int) bestPeriod;
            this.configFile.storeConfig(config);
    }




    public MethodResults performSMA(String stockName) {

        List<Double> closingPrices = null;
        int period = 0;

        this.loadInputs(stockName);

        closingPrices = super.createClosingPriceList(stockName,
                                                     config.inputStartingDate,
                                                     config.inputEndingDate);
        double[] input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));
        period = input.length - 1;
 
        if (this.inputPerfomDecision) {
            this.performDecisionTest(stockName);
        }

        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        System.out.printf("hoP : %d\n", config.inputSMAPeriod);
        double[] output = this.actionSMA(input, period,
                                         outBegIdx, outNbElement,
                                         config.inputSMAPeriod);

        //System.out.printf("The original size: (%d:%d) alloc:%d\n", outBegIdx.value,outNbElement.value,allocationSize);

        methodResults.putResult(stockType.getStockName(), output[output.length - 1]);

        if (this.inputPrintResults) {
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

        methodResults.setAvrSuccessRate(avgSuccessRate / totalStocksAnalyzed);
        System.out.printf("%s has %d successrate\n", this.getMethName(), methodResults.getAvrSuccessRate().intValue());
        return methodResults;
    }

    @Override
    public MethodResults performMethod(String stockName) {
        return this.performSMA(stockName);
    }
}
