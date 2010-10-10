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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.jtotus.common.DateIterator;
import org.jtotus.common.Helper;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.MethodConfig;
import org.jtotus.gui.graph.GraphPacket;
import org.jtotus.gui.graph.GraphSender;
import org.apache.commons.lang.ArrayUtils;
import java.io.File;
import java.math.BigDecimal;
import org.jtotus.common.NumberRangeIter;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.ConfTaLibEMA;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibEMA  implements MethodEntry, Callable<MethodResults>{
    private ArrayList<PeriodClosingPrice> periodList = null;

    /*Stock list */
    private Helper help = Helper.getInstance();
    private boolean printResults = true;


    //TODO: staring date, ending date aka period

    //INPUTS TO METHOD:
    public String inputPortofolio=null;
    public String inpuPortfolio=null;
    public int inputEMAPeriod = 9; //Default value
    public Calendar inputEndingDate = null;
    public Calendar inputStartingDate = null;
    public ConfTaLibEMA config = null;
    public String[] inputListOfStocks;
    public boolean inputPrintResults = true;
    ConfigLoader<ConfTaLibEMA> configFile = null;

    public boolean inputPerfomDecision = true;
    public String inputEMADecisionPeriod = null;
    public Double inputAssumedBudjet=null;

    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public boolean isCallable() {
        return true;
    }

        public void loadPortofolioInputs() {
                         //FIXME: set it in PortfolioDecision
            String portfolio = "OMXHelsinki";

            ConfigLoader<ConfPortfolio> configPortfolio =
                    new ConfigLoader<ConfPortfolio>(portfolio);

            if (configPortfolio.getConfig() == null){
                  //Load default values
                  ConfPortfolio newPortConfig = new ConfPortfolio();
                  configPortfolio.storeConfig(newPortConfig);
              }

            //Get stock names
            configPortfolio.applyInputsToObject(this);
            this.inputPortofolio = portfolio;
        }



        public void loadInputs(String configStock){

             configFile= new ConfigLoader<ConfTaLibEMA>(this.inputPortofolio +
                                                        File.separator +
                                                        configStock +
                                                        File.separator +
                                                        this.getMethName());

                   // new ConfigLoader<ConfTaLibEMA>(portfolio+File.separator+this.getMethName());

              if (configFile.getConfig() == null){
                  //Load default values
                  config = new ConfTaLibEMA();
                  configFile.storeConfig(config);
              }else {
                  config = configFile.getConfig();
              }


            configFile.applyInputsToObject(this);
        }


    public void createPeriods() {

      MethodConfig listOfTasks = new MethodConfig();
      Iterator<String> iter = listOfTasks.iterator();
      periodList = new ArrayList<PeriodClosingPrice>();


       //Build period history for stock
       while(iter.hasNext()) {
            StockType stock = new StockType(iter.next());
            periodList.add(new PeriodClosingPrice(stock));
            help.debug(this.getClass().getName(), "StockName for period:%s\n",stock.getName());
       }

    }

        public MethodResults performEMA(int EMA_period) {

            MethodResults results = new MethodResults(this.getMethName());
            List <Double>closingPrices = new ArrayList<Double>();
            GraphSender sender = new GraphSender();
            GraphPacket packet = new GraphPacket();

            double[] output = null;
            MInteger outBegIdx = null;
            MInteger outNbElement = null;
            int period = 0;

            for(int stockCount=0;stockCount<this.inputListOfStocks.length;stockCount++) {
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
                 final int allocationSize = period - core.emaLookback(this.inputEMAPeriod);

                 if (allocationSize <= 0) {
                     System.err.printf("No data for period (%d)\n", allocationSize);
                     return null;
                 }



                 output = new double[allocationSize];
                 outBegIdx = new MInteger();
                 outNbElement = new MInteger();

                 RetCode code = core.ema(0, period - 1, input ,
                                           this.inputEMAPeriod,
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
                    int decEMAPeriod = 0;

                    NumberRangeIter numberIter = new NumberRangeIter("EMARange");
                    numberIter.setRange(this.inputEMADecisionPeriod);
                    while(numberIter.hasNext()) {
                        amoutOfStocks = 0;
                        assumedBudjet = this.inputAssumedBudjet.doubleValue();
                        decEMAPeriod = numberIter.next().intValue();

                        final int allocationSizeDecision = period - core.emaLookback(decEMAPeriod);

                        if (allocationSizeDecision <= 0) {
                             System.err.printf("No data for period (%d)\n", allocationSizeDecision);
                             return null;
                         }

                         double[] outputDec = new double[allocationSizeDecision];
                         MInteger outBegIdxDec = new MInteger();
                         MInteger outNbElementDec = new MInteger();

                         RetCode decCode = core.ema(0, period - 1,
                                                    input ,
                                                    decEMAPeriod,
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
                                            bestPeriod = decEMAPeriod;
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
                                 if (this.inputPrintResults && decEMAPeriod == this.inputEMAPeriod) {
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
                    System.out.printf("%s:The best period:%f best budjet:%f pros:%f\n",
                            stockType.getName(),bestPeriod, bestAssumedBudjet,
                            ((bestAssumedBudjet/this.inputAssumedBudjet)-1)*100);

                    this.config.inputEMAPeriod = (int) bestPeriod;
                    this.configFile.storeConfig(config);
                }

            }


            return results;
        }

    public void run() {
        this.loadPortofolioInputs();

        this.createPeriods();
        this.performEMA(this.inputEMAPeriod);
    }

    public MethodResults call() throws Exception {
        this.loadPortofolioInputs();


        this.createPeriods();
        MethodResults results = this.performEMA(this.inputEMAPeriod);
        if (printResults) {
            Iterator<Entry<String, Double>> iter = results.iterator();
            while(iter.hasNext()){
                Entry<String, Double> next = iter.next();

                GraphSender sender = new GraphSender();
                GraphPacket packet = new GraphPacket();

                packet.seriesTitle = this.getMethName();
                packet.result = next.getValue().doubleValue();
                packet.date = inputEndingDate.getTimeInMillis();

                sender.sentPacket(next.getKey(), packet);
            }
        }
        return results;
    }


}
