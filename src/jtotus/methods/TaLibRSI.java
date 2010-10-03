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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jtotus.common.Helper;
import jtotus.common.StockType;
import jtotus.config.MethodConfig;
import jtotus.gui.graph.GraphPacket;
import jtotus.gui.graph.GraphSender;


/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibRSI  implements MethodEntry, Callable<MethodResults>{
    private ArrayList<PeriodClosingPrice> periodList = null;
    private final StockType stockType = null;

    /*Stock list */
    private List<String> stockNames = null;
    private List<Date>resutlsForDates = null;
    private Helper help = Helper.getInstance();
    private boolean printResults = true;
    

    //TODO: staring date, ending date aka period

    //INPUTS TO METHOD:
    private int inputParam_rsiPeriod = 4;
    private Calendar inputEndingDate = Calendar.getInstance();


    

    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public boolean isCallable() {
        return true;
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

    //RSI
    public MethodResults performRSI(int rsi_period) {
       int period = 0;
       final Core core = new Core();
       MethodResults results = new MethodResults("TaLibRSI");

 
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

    private void dumpArray(double []array) {
        for (int i = 0; i < array.length;i++){
            System.out.printf("%.3f,", array[i]);
            if ((i % 10) == 0) {
               System.out.printf("\n");
            }
        }
        System.out.printf("\n");
    }

    public void run() {
        this.createPeriods();
        this.performRSI(inputParam_rsiPeriod);
    }

    public MethodResults call() throws Exception {

        this.createPeriods();
        
        MethodResults results = this.performRSI(inputParam_rsiPeriod);
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
