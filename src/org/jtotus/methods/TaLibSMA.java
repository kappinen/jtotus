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

import java.util.concurrent.Callable;
import org.jtotus.common.MethodResults;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphSender;
import org.apache.commons.lang.ArrayUtils;
import java.io.File;
import java.math.BigDecimal;
import org.jtotus.common.NumberRangeIter;
import org.jtotus.config.ConfTaLibSMA;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibSMA  extends TaLibAbstract implements MethodEntry{

    /*Stock list */
    private double avgSuccessRate = 0.0f;
    private int totalStocksAnalyzed = 0;

    //INPUTS TO METHOD:
    public Calendar inputEndingDate = null;
    public Calendar inputStartingDate = null;
    public ConfTaLibSMA config = null;
    ConfigLoader<ConfTaLibSMA> configFile = null;


        public void loadInputs(String configStock){

             configFile= new ConfigLoader<ConfTaLibSMA>(this.inputPortofolio +
                                                        File.separator +
                                                        configStock +
                                                        File.separator +
                                                        this.getMethName());

                   // new ConfigLoader<ConfTaLibSMA>(portfolio+File.separator+this.getMethName());

              if (configFile.getConfig() == null){
                  //Load default values
                  config = new ConfTaLibSMA();
                  configFile.storeConfig(config);
              }else {
                  config = configFile.getConfig();
              }

             
            configFile.applyInputsToObject(this);
        }


        public MethodResults performSMA() {

            MethodResults results = new MethodResults(this.getMethName());
            List <Double>closingPrices = new ArrayList<Double>();

            double[] output = null;
            MInteger outBegIdx = null;
            MInteger outNbElement = null;
            int period = 0;

            for(int stockCount=0;stockCount<super.inputListOfStocks.length;stockCount++) {
               closingPrices.clear();

               this.loadInputs(this.inputListOfStocks[stockCount]);

               StockType stockType = new StockType(this.inputListOfStocks[stockCount]);

               DateIterator dateIter = new DateIterator(inputStartingDate.getTime(),
                                                        inputEndingDate.getTime());

                //Filling input data with Closing price for days
               while(dateIter.hasNext()) {

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateIter.next());
                    BigDecimal closDay = stockType.fetchClosingPrice(cal);
                    if (closDay!=null) {
                        closingPrices.add(closDay.doubleValue());
                   }
                }


                final Core core = new Core();
                double []input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));

                 period = input.length-1;
                 final int allocationSize = period - core.smaLookback(config.inputSMAPeriod);

                 if (allocationSize <= 0) {
                     System.err.printf("No data for period (%d)\n", allocationSize);
                     return null;
                 }



                 output = new double[allocationSize];
                 outBegIdx = new MInteger();
                 outNbElement = new MInteger();

                 RetCode code = core.sma(0, period - 1, input ,
                                           config.inputSMAPeriod,
                                           outBegIdx,
                                           outNbElement, output);

                 if (code.compareTo(RetCode.Success) != 0) {
                     //Error return empty method results
                     System.err.printf("SMI failed!\n");
                     return new MethodResults(this.getMethName());
                 }



                
                 //System.out.printf("The original size: (%d:%d) alloc:%d\n", outBegIdx.value,outNbElement.value,allocationSize);

                 results.putResult(stockType.getName(), output[output.length - 1]);

                   if (this.inputPrintResults) {
                       sender = new GraphSender(this.getMethName());
                       DateIterator dateIterator = new DateIterator(inputStartingDate.getTime(),
                                                                    inputEndingDate.getTime());
                        dateIterator.move(outBegIdx.value);
                        for(int i=0;i < outNbElement.value && dateIterator.hasNext();i++) {
                            Date stockDate = dateIterator.next();
                            //System.out.printf("Date:"+stockDate+" Time:"+inputEndingDate.getTime()+"Time2:"+inputStartingDate.getTime()+"\n");

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(stockDate);

                            packet.seriesTitle = this.getMethName();
                            packet.result = output[i];
                            packet.date = stockDate.getTime();

                            sender.sentPacket(stockType.getName(), packet);
                        }
                  }

                //************* DECISION TEST *************//
                if (this.inputPerfomDecision) {
                    
                    
                    double amoutOfStocks = 0;
                    double bestAssumedBudjet = 0;
                    double bestPeriod = 0;
                    double assumedBudjet = 0.0f;
                    int decSMAPeriod = 0;

                    NumberRangeIter numberIter = new NumberRangeIter("SMARange");
                    numberIter.setRange(config.inputSMADecisionPeriod);
                    while(numberIter.hasNext()) {
                        amoutOfStocks = 0;
                        assumedBudjet = this.inputAssumedBudjet.doubleValue();
                        decSMAPeriod = numberIter.next().intValue();
                        
                        final int allocationSizeDecision = period - core.smaLookback(decSMAPeriod);

                        if (allocationSizeDecision <= 0) {
                             System.err.printf("No data for period (%d)\n", allocationSizeDecision);
                             return null;
                         }

                         double[] outputDec = new double[allocationSizeDecision];
                         MInteger outBegIdxDec = new MInteger();
                         MInteger outNbElementDec = new MInteger();

                         RetCode decCode = core.sma(0, period - 1,
                                                    input ,
                                                    decSMAPeriod,
                                                    outBegIdxDec,
                                                    outNbElementDec, outputDec);
                        
                         if (decCode.compareTo(RetCode.Success) != 0) {
                             //Error return empty method results
                             System.err.printf("SMI failed in Decision!\n");
                             return new MethodResults(this.getMethName());
                         }

                        //TODO: Evaluate and store best config
                        int direction=0;
                        boolean changed=true;
                        
                        for (int elem=1;elem < outNbElementDec.value;elem++) {


                            double threshold = (outputDec[elem-1]+outputDec[elem])/2;
                            changed=false;
                            if (input[outBegIdxDec.value+elem] > threshold) { //above the line
                                if (direction==-1) {
                                    changed=true; //Price is going down
                                    if (amoutOfStocks!=0) {
                                        assumedBudjet=amoutOfStocks*input[elem+outBegIdxDec.value];
                                        if(bestAssumedBudjet < assumedBudjet) {
                                            bestAssumedBudjet = assumedBudjet;
                                            bestPeriod = decSMAPeriod;
                                        }
                                    }
                                }

                                direction=1;
                            } else {
                                if (direction==1) {
                                    changed=true; //Price is going up
                                    amoutOfStocks = assumedBudjet / input[elem+outBegIdxDec.value];
                                    
                                }
                                
                                direction=-1;
                            }

                            if(changed){
                                 if (this.inputPrintResults && decSMAPeriod == config.inputSMAPeriod) {
                                    DateIterator dateIterator = new DateIterator(inputStartingDate.getTime(),
                                                                                inputEndingDate.getTime());
                                    dateIterator.move(elem + outBegIdxDec.value);


                                    
                                    packet.seriesTitle = "CrossingPoint";
                                    packet.result = input[elem+outBegIdxDec.value]+0.1;
                                    packet.date = dateIterator.getCurrent().getTime();

//                                     System.err.printf("The dec period:%s:%s (%d:%d) elem:%d\n",
//                                             dateIterator.getCurrent().toString(),stockType.getName(),
//                                             outBegIdxDec.value, outNbElementDec.value, elem);
                                    sender.sentPacket(stockType.getName(), packet);
                                }

                            }

                        }

                    }

                    Double successRate=((bestAssumedBudjet/this.inputAssumedBudjet)-1)*100;
                    System.out.printf("%s:The best period:%f best budjet:%f pros:%f\n",
                            stockType.getName(),bestPeriod, bestAssumedBudjet,
                            successRate.doubleValue());

                    totalStocksAnalyzed++;
                    this.avgSuccessRate += successRate;
                    this.config.outputSuccessRate = successRate;
                    this.config.inputSMAPeriod = (int) bestPeriod;
                    this.configFile.storeConfig(config);
                }

            }


            results.setAvrSuccessRate(avgSuccessRate/totalStocksAnalyzed);
            System.out.printf("%s has %d successrate\n", this.getMethName(), results.getAvrSuccessRate().intValue());
            return results;
        }

     public MethodResults performMethod() {
        return this.performSMA();
    }

}
