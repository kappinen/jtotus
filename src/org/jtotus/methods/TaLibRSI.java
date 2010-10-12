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
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.common.StockType;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfTaLibRSI;
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.config.ConfigLoader;

/**
 *
 * @author Evgeni Kappinen
 */
public class TaLibRSI extends TaLibAbstract implements MethodEntry, Callable<MethodResults> {
    /*Stock list */

    //TODO: staring date, ending date aka period
    //INPUTS TO METHOD:
    public int inputRSIPeriod = 1; //Default value
    //public Calendar inputEndingDate = Calendar.getInstance();
    public Calendar inputStartingDate = null;
    public ConfTaLibRSI config = null;
    public ConfigLoader<ConfTaLibRSI> configFile = null;

    
    public TaLibRSI() {
        super();
    }

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
            config = (ConfTaLibRSI)configFile.getConfig();
        }

        configFile.applyInputsToObject(this);
    }

    public MethodResults performRSI(int rsi_period) {
        MethodResults results = new MethodResults(this.getMethName());
        List<Double> closingPrices = new ArrayList<Double>();
        GraphSender sender = new GraphSender();
        

        for (int stockCount = 0; stockCount < inputListOfStocks.length; stockCount++) {
            closingPrices.clear();

            this.loadInputs(super.inputListOfStocks[stockCount]);

            System.out.printf("period:%d\n", inputRSIPeriod);
            StockType stockType = new StockType(super.inputListOfStocks[stockCount]);

            DateIterator dateIter = new DateIterator(inputStartingDate.getTime(),
                    inputEndingDate.getTime());

            while (dateIter.hasNext()) {

                Calendar cal = Calendar.getInstance();
                cal.setTime(dateIter.next());
                closingPrices.add(stockType.fetchClosingPrice(cal).doubleValue());
            }


            final Core core = new Core();
            double[] input = ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));

            int period = input.length - 1;
            final int allocationSize = period - core.rsiLookback(this.inputRSIPeriod);

            if (allocationSize <= 0) {
                System.err.printf("No data for period (%d)\n", allocationSize);
                return null;
            }

            double[] output = new double[allocationSize];
            MInteger outBegIdx = new MInteger();
            MInteger outNbElement = new MInteger();

            RetCode code = core.rsi(0, period - 1, input,
                    this.inputRSIPeriod,
                    outBegIdx,
                    outNbElement, output);

            if (code.compareTo(RetCode.Success) != 0) {
                //Error return empty method results
                return new MethodResults(super.getMethName());
            }

            results.putResult(stockType.getName(), output[output.length - 1]);

            if (super.inputPrintResults) {
                DateIterator dateIterator = new DateIterator(inputStartingDate.getTime(),
                        inputEndingDate.getTime());
                dateIterator.move(outBegIdx.value);
                for (int i = 0; i < outNbElement.value && dateIterator.hasNext(); i++) {
                    Date stockDate = dateIterator.next();
                    //System.out.printf("Date:"+stockDate+" Time:"+inputEndingDate.getTime()+"Time2:"+inputStartingDate.getTime()+"\n");

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(stockDate);

                    packet.seriesTitle = super.getMethName();
                    packet.result = output[i];
                    packet.date = stockDate.getTime();

                    sender.sentPacket(stockType.getName(), packet);
                }
            }

        }


        return results;
    }

    @Override
    public MethodResults performMethod() {
        return this.performRSI(this.inputRSIPeriod);
    }
}
