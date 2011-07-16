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

package org.jlucrum.realtime.generators;

import org.jlucrum.realtime.eventtypes.IndicatorData;
import org.jlucrum.realtime.eventtypes.StockTick;
import com.espertech.esper.client.EventBean;
import java.util.HashMap;
import java.util.Map.Entry;
import org.jtotus.common.StockNames;

/**
 *
 * @author Evgeni Kappinen
 */
public class IndicatorIndexGenerator extends TickAnalyzer {
    private String indicator =  "OMXH25";
    private HashMap <String, Double>indicatorMap = new HashMap <String, Double>();
    private StockNames stockWeight;
    private boolean debug = false;

    public IndicatorIndexGenerator() {
        super();
        stockWeight = new StockNames();
    }

    private void init(){
        super.patternForEvents("every IndicatorData(indicatorName='"+indicator+"')")
                .addListener(this);
    }

    private double calculateIndex() {
        double sum = 0.0d;
        double weight;

        for (Entry<String, Double> entry: indicatorMap.entrySet()) {
            weight = stockWeight.getStockWeight(entry.getKey());
            sum += entry.getValue() * weight;
        }

        if (debug) {
            System.out.printf("IndicatorIndex: size:%d value:%f\n", indicatorMap.size(), sum);
        }
        
        return sum;
    }
    
    public void update(EventBean[] ebs, EventBean[] ebs1) {

        for (int i=0; i < ebs.length;i++) {

            if (ebs[i].getUnderlying() instanceof IndicatorData) {
                IndicatorData data = (IndicatorData)ebs[i].getUnderlying();
                if (data==null) {
                    continue;
                }
                indicatorMap.put(data.getStockName(), data.getIndicatorValue());
            } else if (ebs[i].getUnderlying() instanceof StockTick) {
                StockTick data = (StockTick)ebs[i].getUnderlying();
                if (data==null) {
                    continue;
                }
                indicatorMap.put(data.getStockName(), data.getLatestPrice());
            }
            
            
        }

        IndicatorData data = new IndicatorData();
        data.setStockName("Index"+indicator);
        data.setIndicatorValue(calculateIndex());
        data.setIndicatorName(getName());
        data.type = IndicatorData.DrawType.STANDALONE_INDICATOR_TABLE;
        getEngine().sendEvent(data);
    }

    public String getName() {
        return "Index"+indicator;
    }

    public String getListnerInfo() {
        return "Modified: Sum of all values in stock list for particular indicator \n";
    }
}
