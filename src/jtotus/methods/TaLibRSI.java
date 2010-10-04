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

package jtotus.methods;

import java.util.concurrent.Callable;
import jtotus.common.MethodResults;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map.Entry;
import jtotus.common.DateIterator;
import jtotus.common.Helper;
import jtotus.common.StockType;
import jtotus.config.ConfigLoader;
import jtotus.config.MethodConfig;
import jtotus.gui.graph.GraphPacket;
import jtotus.gui.graph.GraphSender;
import jtotus.methods.config.ConfTaLibRSI.ConfTaLibRSI;
import org.apache.commons.lang.ArrayUtils;


/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibRSI  implements MethodEntry, Callable<MethodResults>{
    private ArrayList<PeriodClosingPrice> periodList = null;

    /*Stock list */
    private Helper help = Helper.getInstance();
    private boolean printResults = true;
    

    //TODO: staring date, ending date aka period

    //INPUTS TO METHOD:
    public String portfolio=null;
    public int inputRSIPeriod = 9; //Default value
    public Calendar inputEndingDate = Calendar.getInstance();
    public Calendar inputStartingDate = Calendar.getInstance();
    public ConfTaLibRSI config = null;
    public String[] inputListOfStocks;
    public boolean inputPrintResults = true;
    

    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public boolean isCallable() {
        return true;
    }


        public void loadInputs(){
            ConfigLoader<ConfTaLibRSI> configFile =
                    new ConfigLoader<ConfTaLibRSI>(this.getMethName());
                   // new ConfigLoader<ConfTaLibRSI>(portfolio+File.separator+this.getMethName());

              if (configFile.getConfig() == null){
                  //Load default values 
                  config = new ConfTaLibRSI();
                  configFile.storeConfig(config);
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

        public MethodResults performRSIv2(int rsi_period) {
           MethodResults results = new MethodResults(this.getMethName());
           ArrayDeque <Double>dequePrice = new ArrayDeque<Double>();
           final Core core = new Core();
           DateIterator dateIter = new DateIterator(inputStartingDate.getTime(),
                                                    inputEndingDate.getTime());
           
           
           StockType stockType = new StockType(this.inputListOfStocks[0]);
           while(dateIter.hasNext()) {
               
               
               if(dequePrice.size() < this.inputRSIPeriod){
                   BigDecimal stockValue = stockType.fetchClosingPrice(dateIter.next());
                   if (stockValue != null) {
                        Double addValue = Double.valueOf(stockValue.doubleValue());
                        dequePrice.add(addValue);
                    }
                   
                   if (dequePrice.size() != this.inputRSIPeriod) {
                        continue;
                   }
                 } 

               
               final int allocationSize = dequePrice.size() - core.rsiLookback(this.inputRSIPeriod);
               if (allocationSize <= 0) {
                    System.err.printf("%s: No data for period allocationSize:%d deque:%d rsiPeriod:%d\n",
                            stockType.getName(), allocationSize, dequePrice.size(),this.inputRSIPeriod);
                    return null;
                }

               double[] output = new double[allocationSize];
               MInteger outBegIdx = new MInteger();
               MInteger outNbElement = new MInteger();
               
               double[] closingPrices = ArrayUtils.toPrimitive(dequePrice.toArray(new Double[0]));

               RetCode code = core.rsi(0, dequePrice.size() - 1, closingPrices , this.inputRSIPeriod, outBegIdx, outNbElement, output);
               //FIXME: if (code.code.Success){
               results.putResult(stockType.getName(), output[outNbElement.value - 1]);
               dequePrice.pop();
               if (this.inputPrintResults) {
                    Iterator<Entry<String, Double>> iter = results.iterator();
                    while(iter.hasNext()){
                        Entry<String, Double> next = iter.next();

                        GraphSender sender = new GraphSender();
                        GraphPacket packet = new GraphPacket();

                        packet.seriesTitle = this.getClass().getCanonicalName();
                        packet.result = next.getValue().doubleValue();
                        packet.day = inputEndingDate.get(Calendar.DATE);
                        packet.month = inputEndingDate.get(Calendar.MONTH) + 1;
                        packet.year = inputEndingDate.get(Calendar.YEAR);

                        sender.sentPacket(next.getKey(), packet);
                    }
              }

           }
            

            return results;
        }
        
    //RSI
    public MethodResults performRSI(int rsi_period) {
       int period = 0;
       final Core core = new Core();
       MethodResults results = new MethodResults(this.getClass().getCanonicalName());

 
       Iterator<PeriodClosingPrice> periodsIter = periodList.iterator();
       while(periodsIter.hasNext()) {

           PeriodClosingPrice periodPrice = periodsIter.next();
           period = periodPrice.getPeriodLength();

           final int allocationSize = period - core.rsiLookback(rsi_period);
            if (allocationSize <= 0) {
                System.err.printf("%s: No data for period (%d)\n", periodPrice.getStockName(), allocationSize);
                return null;
            }

           double[] output = new double[allocationSize];
           MInteger outBegIdx = new MInteger();
           MInteger outNbElement = new MInteger();
           double[] values = periodPrice.toDoubleArray();
           
//           System.out.printf("Size:%d alloc:%d loop:%d\n", values.length, allocationSize, core.rsiLookback(rsi_period));
//           this.dumpArray(values);
           RetCode code = core.rsi(0, period - 1, values , rsi_period, outBegIdx, outNbElement, output);
           System.out.printf("[TaLibRSI:%s] outBegIdx:%d outNbElement:%d outputLen:%d RSI:"+output[outNbElement.value - 1]+" Result:%s\n",
                   periodPrice.getStockName(), outBegIdx.value, outNbElement.value, output.length, code.toString());
           results.putResult(periodPrice.getStockName(), output[outNbElement.value - 1]);

       }

       return results;
    }

    public void run() {
        this.createPeriods();
        this.loadInputs();
        this.performRSIv2(this.inputRSIPeriod);
    }

    public MethodResults call() throws Exception {

        this.createPeriods();
        this.loadInputs();
        MethodResults results = this.performRSIv2(this.inputRSIPeriod);
        if (printResults) {
            Iterator<Entry<String, Double>> iter = results.iterator();
            while(iter.hasNext()){
                Entry<String, Double> next = iter.next();
                
                GraphSender sender = new GraphSender();
                GraphPacket packet = new GraphPacket();
                
                packet.seriesTitle = this.getMethName();
                packet.result = next.getValue().doubleValue();
                packet.day = inputEndingDate.get(Calendar.DATE);
                packet.month = inputEndingDate.get(Calendar.MONTH) + 1;
                packet.year = inputEndingDate.get(Calendar.YEAR);

                sender.sentPacket(next.getKey(), packet);
            }
        }
        return results;
    }


}
