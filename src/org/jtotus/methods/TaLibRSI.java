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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfTaLibRSI;
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.common.StateIterator;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphSeriesType;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibRSI extends TaLibAbstract implements MethodEntry {
    /*Stock list */

    //TODO: staring date, ending date aka period
    //INPUTS TO METHOD:
    private double avgSuccessRate = 0.0f;
    private int totalStocksAnalyzed = 0;
    protected ConfTaLibRSI config = null;
    public ConfigLoader<ConfTaLibRSI> configFile = null;

    public void loadInputs(String configStock) {

        configFile = new ConfigLoader<ConfTaLibRSI>(super.inputPortofolio
                + File.separator
                + configStock
                + File.separator
                + this.getMethName());

        if (configFile.getConfig() == null) {
            //Load default values
            config = new ConfTaLibRSI();
            configFile.storeConfig(config);
        } else {
            config = (ConfTaLibRSI) configFile.getConfig();
        }
        super.child_config = config;
        configFile.applyInputsToObject(this);
    }

    public MethodResults performRSI(String stockName) {

        List<Double> closingPrices = new ArrayList<Double>();

        closingPrices.clear();

        stockType.setStockName(stockName);
        this.loadInputs(stockName);

        System.out.printf("period:%d\n", config.inputRSIPeriod);

        DateIterator dateIter = new DateIterator(config.inputStartingDate.getTime(),
                config.inputEndingDate.getTime());

        while (dateIter.hasNext()) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateIter.next());
            BigDecimal ret = stockType.fetchClosingPrice(cal);
            //Can be null only, if there is no data for today
            if (ret != null) {
                closingPrices.add(ret.doubleValue());
            }
        }


        final Core core = new Core();
        double[] input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));
        int period = input.length - 1;


        //************* DECISION TEST *************//

        if (this.inputPerfomDecision) {
            double bestAssumedBudjet = 0;
            double bestPeriod = 0;
            double assumedBudjet = 0.0f;
            int decSMAPeriod = 0;
            EvaluateMethodSignals budjetCounter = new EvaluateMethodSignals();


            for (StateIterator iter = new StateIterator()
                    .addParam("RSIpriod", config.inputRSIDecisionPeriod)
                    .addParam("LowestThreshold", config.inputRSILowestThreshold)
                    .addParam("HigestThreshold", config.inputRSIHigestThreshold);
                    iter.hasNext() != StateIterator.END_STATE; iter.nextState()) {

                double amountOfStocks = 0;
                budjetCounter.initialize(super.inputAssumedBudjet);

                assumedBudjet = this.inputAssumedBudjet.doubleValue();
                decSMAPeriod = iter.nextInt("RSIpriod");

                final int allocationSizeDecision = period - core.rsiLookback(decSMAPeriod);

                if (allocationSizeDecision <= 0) {
                    System.err.printf("No data for period (%d)\n", allocationSizeDecision);
                    return null;
                }

                double[] outputDec = new double[allocationSizeDecision];
                MInteger outBegIdxDec = new MInteger();
                MInteger outNbElementDec = new MInteger();

                RetCode decCode = core.rsi(0, period - 1,
                        input,
                        decSMAPeriod,
                        outBegIdxDec,
                        outNbElementDec, outputDec);

                if (decCode.compareTo(RetCode.Success) != 0) {
                    //Error return empty method results
                    System.err.printf("RSI failed in Decision!\n");
                    return new MethodResults(this.getMethName());
                }

                //TODO: Evaluate and store best config
                boolean changed = true;

                double lowestThreshold = iter.nextDouble("LowestThreshold");
                double highestThreshold = iter.nextDouble("HigestThreshold");

                for (int elem = 0; elem < outNbElementDec.value; elem++) {

                    if (outputDec[elem] > highestThreshold) { //above the line, sell
                        if (amountOfStocks != 0) {
                            assumedBudjet = amountOfStocks * input[elem + outBegIdxDec.value];
                            amountOfStocks = 0;
                            changed = false;
//                                        System.out.printf("%s selling for:"+input[elem+outBegIdxDec.value]+" budjet:%f per:%d bestper:%f low:%f high:%f elem:%d = %f\n",
//                                                stockType.getName(), assumedBudjet, decSMAPeriod, bestPeriod, lowestThreshold,highestThreshold, elem, outputDec[elem]);
                            budjetCounter.buy(input[elem + outBegIdxDec.value], -1);
                            if (bestAssumedBudjet < assumedBudjet) {
                                bestAssumedBudjet = assumedBudjet;
                                bestPeriod = decSMAPeriod;
                            }
                        }

                    } else if (outputDec[elem] < lowestThreshold) {
                        if (amountOfStocks == 0) {
                            budjetCounter.sell(input[elem + outBegIdxDec.value], -1);
                            amountOfStocks = assumedBudjet / input[elem + outBegIdxDec.value];
                            changed = false;
                        }
//                                    System.out.printf("%s buying for:"+input[elem+outBegIdxDec.value]+" budjet:%f period:%d bestper:%f low:%f high:%f elem:%d = %f\n",
//                                            stockType.getName(), assumedBudjet, decSMAPeriod, bestPeriod,lowestThreshold,highestThreshold,elem, outputDec[elem]);
                    }

                    if (false) {
                        BigDecimal budjet = budjetCounter.getCurrentBestBudjet();
                        if (this.inputPrintResults && decSMAPeriod == config.inputRSIPeriod) {
                            sender = new GraphSender(stockType.getStockName());
                            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                                                                         config.inputEndingDate.getTime());
                            dateIterator.move(elem + outBegIdxDec.value);
                            packet.seriesTitle = this.getMethName();
                            packet.result = input[elem + outBegIdxDec.value] + 0.1;
                            packet.date = dateIterator.getCurrent().getTime();

                            sender.sentPacket(stockType.getStockName(), packet);
                        }
                        changed = false;
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
            this.config.inputRSIPeriod = (int) bestPeriod;
            this.config.inputPerfomDecision = false;
            if (bestAssumedBudjet != 0) {
                config.inputPerfomDecision = false;
                this.configFile.storeConfig(config);
            }

        }



        final int allocationSize = period - core.rsiLookback(config.inputRSIPeriod);

        if (allocationSize <= 0) {
            System.err.printf("No data for period (%d)\n", allocationSize);
            return null;
        }

        double[] output = new double[allocationSize];
        MInteger outBegIdx = new MInteger();
        MInteger outNbElement = new MInteger();

        RetCode code = core.rsi(0, period - 1, input,
                config.inputRSIPeriod,
                outBegIdx,
                outNbElement, output);

        if (code.compareTo(RetCode.Success) != 0) {
            //Error return empty method methodResults
            throw new java.lang.IllegalStateException("RSI failed");
        }

        methodResults.putResult(stockType.getStockName(), output[output.length - 1]);

        if (super.inputPrintResults) {
            sender = new GraphSender(stockType.getStockName());
            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                                                         config.inputEndingDate.getTime());
            
            dateIterator.move(outBegIdx.value);
            for (int i = 0; i < outNbElement.value && dateIterator.hasNext(); i++) {
                Date stockDate = dateIterator.next();

                
                packet.type = GraphSeriesType.SIMPLECANDLESTICK;
                packet.seriesTitle = this.getMethName();
                packet.result = output[i];
                packet.date = stockDate.getTime();

                sender.sentPacket(stockType.getStockName(), packet);
            }
        }




        methodResults.setAvrSuccessRate(avgSuccessRate / totalStocksAnalyzed);
        System.out.printf("%s has %d successrate\n", this.getMethName(), methodResults.getAvrSuccessRate().intValue());
        return methodResults;
    }

    @Override
    public MethodResults performMethod(String stockName) {
        MethodResults results = this.performRSI(stockName);

        return results;
    }
}
