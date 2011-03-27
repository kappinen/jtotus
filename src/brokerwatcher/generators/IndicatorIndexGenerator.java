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

import brokerwatcher.eventtypes.IndicatorData;
import com.espertech.esper.client.EventBean;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Evgeni Kappinen
 */
public class IndicatorIndexGenerator extends TickAnalyzer {
    private String indicator =  "Vroc";
    private HashMap <String, Double>indicatorMap = new HashMap <String, Double>();


    public IndicatorIndexGenerator() {
        super();
    }


    private void init(){
        super.patternForEvents("every IndicatorData(indicatorName='"+indicator+"')")
                .addListener(this);
    }

    private double calculateIndex() {
        double sum = 0.0d;
        Iterator<String> indKeys = indicatorMap.keySet().iterator();
        while(indKeys.hasNext()) {
            double value = indicatorMap.get(indKeys.next());
            sum += value;
        }

        System.out.printf("IndicatorIndex: size:%d value:%f\n", indicatorMap.size(), sum);
        return sum;
    }
    
    public void update(EventBean[] ebs, EventBean[] ebs1) {

        for (int i=0; i < ebs.length;i++) {
            IndicatorData data = (IndicatorData)ebs[i].getUnderlying();
            if (data==null) {
                continue;
            }
            indicatorMap.put(data.getStockName(), data.getIndicatorValue());
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
