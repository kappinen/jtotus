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

package brokerwatcher.generators;

import brokerwatcher.eventtypes.StockTick;
import brokerwatcher.indicators.SimpleTechnicalIndicators;
import com.espertech.esper.client.EventBean;
import com.tictactec.ta.lib.MInteger;
import java.util.HashMap;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Evgeni Kappinen
 */
public class RsiGenerator extends TickAnalyzer {
    SimpleTechnicalIndicators indicators = new SimpleTechnicalIndicators();
    HashMap<String, Double[]> stockIndx = new HashMap<String, Double[]>();


     private Double[] storeValue(Double table[], double newValue) {
        Double newTable[] = new Double[table.length + 1];
        System.arraycopy(table, 0, newTable, 0, table.length);
        newTable[table.length] = newValue;
        return newTable;
    }

    //FIXME: bound period to Beta or test it !
    //Double -> double/float ?
    public void update(EventBean[] ebs, EventBean[] ebs1) {
        for (int i = 0; i < ebs.length; i++) {
            StockTick tick = (StockTick) ebs[i].getUnderlying();
            Double[] data = stockIndx.get(tick.getStockName());
            if (data == null) {
                data = new Double[0];
            }

            data = storeValue(data, tick.getLatestPrice());
            stockIndx.put(tick.getStockName(), data);

            double input[] = ArrayUtils.toPrimitive(data);
            if (data.length > config.rsiPeriod + 1) {
                MInteger outBegIdx = new MInteger();
                MInteger outNbElement = new MInteger();
                double[] output = indicators.RSI(input,
                        outBegIdx, outNbElement,
                        //config.rsiPeriod);
                        data.length - 2); //FIXME: Which is better, calculate over all available data or tested period ????
                if (output != null) {
                    sendEvent(tick.getStockName(), output[output.length - 1]);
                }
            }
        }
    }

    public String getName() {
        return "RSI";
    }

    public String getListnerInfo() {
        return "<html>"
                + "RSI - Relative Strength Index\n<br>"
                + "Sournce: http://tadoc.org/indicator/RSI.htm\n<br>"
                + "</html>";
    }
}
