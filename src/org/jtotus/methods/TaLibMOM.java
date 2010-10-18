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

https://www.nordnet.fi/mux/page/hjalp/ordHjalp.html?ord=diagram%20momentum

momentum

Momentum toimii hyvin markkinoilla, joilla on havaittavissa joko nousevia tai laskevia trendejä.

Momentum kertoo sen, kuinka paljon osakkeen kurssi on muuttunut
valitulla aikavälillä. Se antaa seuraavat signaalit:
- Osta, kun indikaattori käy pohjalla ja kääntyy ylös
- Myy, kun indikaattori käy huipulla ja kääntyy alas


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
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.common.DateIterator;
import org.jtotus.common.NumberRangeIter;
import org.jtotus.config.ConfTaLibMOM;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.gui.graph.GraphSeriesType;
import org.jtotus.methods.utils.Normalizer;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibMOM extends TaLibAbstract implements MethodEntry {

    private double avgSuccessRate = 0.0f;
    private int totalStocksAnalyzed = 0;
    public ConfTaLibMOM config = null;
    public ConfigLoader<ConfTaLibMOM> configFile = null;
    //INPUTS TO METHOD:
    private int inputParam_Period = 10;

    public TaLibMOM() {
        super();
    }

    public void loadInputs(String configStock) {

        configFile = new ConfigLoader<ConfTaLibMOM>(super.inputPortofolio
                + File.separator
                + configStock
                + File.separator
                + this.getMethName());

        if (configFile.getConfig() == null) {
            //Load default values
            config = new ConfTaLibMOM();
            configFile.storeConfig(config);
        } else {
            config = (ConfTaLibMOM) configFile.getConfig();
        }

        configFile.applyInputsToObject(this);
    }

    //MOM
    public MethodResults performMOM(String stockName) {
        List<Double> closingPrices = new ArrayList<Double>();

        double[] output = null;
        MInteger outBegIdx = null;
        MInteger outNbElement = null;
        int period = 0;


        closingPrices.clear();
        this.loadInputs(stockName);
        stockType.setStockName(stockName);
        
        DateIterator dateIter = new DateIterator(config.inputStartingDate.getTime(),
                config.inputEndingDate.getTime());

        //Filling input data with Closing price for days
        while (dateIter.hasNext()) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateIter.next());
            BigDecimal closDay = stockType.fetchClosingPrice(cal);
            if (closDay != null) {
                closingPrices.add(closDay.doubleValue());
            }
        }


        final Core core = new Core();
        double[] input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));

        period = input.length - 1;
        final int allocationSize = period - core.momLookback(config.inputMOMPeriod);

        if (allocationSize <= 0) {
            System.err.printf("No data for period (%d)\n", allocationSize);
            return null;
        }



        output = new double[allocationSize];
        outBegIdx = new MInteger();
        outNbElement = new MInteger();

        RetCode code = core.mom(0, period - 1, input,
                config.inputMOMPeriod,
                outBegIdx,
                outNbElement, output);

        if (code.compareTo(RetCode.Success) != 0) {
            //Error return empty method results
            System.err.printf("SMI failed!\n");
            return new MethodResults(this.getMethName());
        }

        methodResults.putResult(stockType.getStockName(), output[output.length - 1]);

        if (this.inputPrintResults) {
            sender = new GraphSender(this.getMethName());
            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                    inputEndingDate.getTime());
            dateIterator.move(outBegIdx.value);
            for (int i = 0; i < outNbElement.value && dateIterator.hasNext(); i++) {
                Date stockDate = dateIterator.next();
                //System.out.printf("Date:"+stockDate+" Time:"+inputEndingDate.getTime()+"Time2:"+inputStartingDate.getTime()+"\n");

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(stockDate);

                packet.seriesTitle = this.getMethName();
                packet.result = output[i];
                packet.date = stockDate.getTime();

                sender.sentPacket(stockType.getStockName(), packet);
            }
        }

        //************* DECISION TEST *************//
        if (this.inputPerfomDecision) {


            double amoutOfStocks = 0;
            double bestAssumedBudjet = 0;
            double bestPeriod = 0;
            double assumedBudjet = 0.0f;
            int decMOMPeriod = 0;

            NumberRangeIter numberIter = new NumberRangeIter("MOMRange");
            numberIter.setRange(config.inputMOMDecisionPeriod);
            while (numberIter.hasNext()) {
                amoutOfStocks = 0;
                assumedBudjet = this.inputAssumedBudjet.doubleValue();
                decMOMPeriod = numberIter.next().intValue();

                final int allocationSizeDecision = period - core.momLookback(decMOMPeriod);

                if (allocationSizeDecision <= 0) {
                    System.err.printf("No data for period (%d)\n", allocationSizeDecision);
                    return null;
                }

                double[] outputDec = new double[allocationSizeDecision];
                MInteger outBegIdxDec = new MInteger();
                MInteger outNbElementDec = new MInteger();

                RetCode decCode = core.mom(0, period - 1,
                        input,
                        decMOMPeriod,
                        outBegIdxDec,
                        outNbElementDec, outputDec);

                if (decCode.compareTo(RetCode.Success) != 0) {
                    //Error return empty method results
                    throw  new java.lang.IllegalStateException("MOM failed");
                }

                //TODO: Evaluate and store best config
                int direction = 0;
                boolean changed = true;

                for (int elem = 1; elem < outNbElementDec.value; elem++) {
                    // [elem-1] > [elem] => ln([elem-1]/[elem]) > 0)
                    if (outputDec[elem - 1] > outputDec[elem]) { //stock is falling
                        if (direction == 1 && amoutOfStocks != 0) {
                            //selling, Price went to the top and starts to fall
                            changed = true; //Price is going down
                            assumedBudjet = amoutOfStocks * input[elem + outBegIdxDec.value];
                            amoutOfStocks = 0;
                            System.out.printf("%s selling for:" + input[elem + outBegIdxDec.value] + " budjet:%f per:%d bestper:%f\n",
                                    stockType.getStockName(), assumedBudjet, decMOMPeriod, bestPeriod);

                            if (bestAssumedBudjet < assumedBudjet) {
                                bestAssumedBudjet = assumedBudjet;
                                bestPeriod = decMOMPeriod;
                            }
                        }
                        direction = -1;
                    } else {
                        //buying, Price went to the buttom and raising
                        if (direction == -1 && amoutOfStocks == 0) {
                            changed = true; //Price is going up
                            amoutOfStocks = assumedBudjet / input[elem + outBegIdxDec.value];
                            System.out.printf("%s buying for:" + input[elem + outBegIdxDec.value] + " budjet:%f period:%d bestper:%f\n",
                                    stockType.getStockName(), assumedBudjet, decMOMPeriod, bestPeriod);
                        }
                        direction = 1;
                    }

                    if (changed) {
                        if (this.inputPrintResults && decMOMPeriod == config.inputMOMPeriod) {
                            DateIterator dateIterator = new DateIterator(config.inputStartingDate.getTime(),
                                    config.inputEndingDate.getTime());
                            dateIterator.move(elem + outBegIdxDec.value);

                            packet.seriesTitle = "Sell/Buy signals";
                            packet.type = GraphSeriesType.SIMPLEBUBLE;

                            packet.result = input[elem + outBegIdxDec.value] + 0.1;
                            packet.date = dateIterator.getCurrent().getTime();

//                                     System.err.printf("The dec period:%s:%s (%d:%d) elem:%d\n",
//                                             dateIterator.getCurrent().toString(),stockType.getName(),
//                                             outBegIdxDec.value, outNbElementDec.value, elem);
                            sender.sentPacket(stockType.getStockName(), packet);
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
            this.config.inputMOMPeriod = (int) bestPeriod;
            this.configFile.storeConfig(config);
        }



        methodResults.setAvrSuccessRate(avgSuccessRate / totalStocksAnalyzed);
        System.out.printf("%s has %d successrate\n", this.getMethName(), methodResults.getAvrSuccessRate().intValue());

        return methodResults;
    }

    @Override
    public MethodResults performMethod(String stockName) {
        MethodResults results = this.performMOM(stockName);

        System.out.printf("Normilizer:%s\n", config.inputNormilizerType);

        if (config.inputNormilizerType != null) {
            Normalizer norm = new Normalizer();

            return norm.perform(config.inputNormilizerType, results);
        }

        return results;
    }
}
