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

import java.util.concurrent.Callable;
import org.jtotus.common.MethodResults;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.jtotus.common.Helper;
import org.jtotus.common.StockType;
import org.jtotus.config.MethodConfig;


/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibMOM  implements MethodEntry{
    private ArrayList<PeriodClosingPrice> periodList = null;
    private final StockType stockType = null;
    /*Stock list */
    private List<String> stockNames = null;
    private List<Date>resutlsForDates = null;
    private Helper help = Helper.getInstance();
    //TODO: staring date, ending date aka period

    //INPUTS TO METHOD:
    private int inputParam_Period = 10;


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

    //MOM
    public MethodResults performAction(int a_period) {
       int period = 0;
       final Core core = new Core();
       MethodResults results = new MethodResults("TaLibMOM");


       Iterator<PeriodClosingPrice> periodsIter = periodList.iterator();
       while(periodsIter.hasNext()) {

           PeriodClosingPrice periodPrice = periodsIter.next();
           period = periodPrice.getPeriodLength();

           final int allocationSize = period - core.momLookback(a_period);
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
           RetCode code = core.mom(0, period - 1, values , a_period, outBegIdx, outNbElement, output);
           System.out.printf("[TaLibMOM:%s] outBegIdx:%d outNbElement:%d outputLen:%d RSI:"+output[outNbElement.value - 1]+" Result:%s\n",
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
        this.performAction(inputParam_Period);
    }

    public MethodResults call() throws Exception {

        this.createPeriods();

        return this.performAction(inputParam_Period);

    }


}
