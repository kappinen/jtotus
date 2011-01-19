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
import java.util.HashMap;

/**
 *
 * @author Evgeni Kappinen
 */
public class VPTGenerator extends TickAnalyzer{
    private HashMap<String, SimpleTechnicalIndicators> stockIndec = null;


    public VPTGenerator() {
        super();
        stockIndec = new HashMap<String, SimpleTechnicalIndicators>();
    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {
        SimpleTechnicalIndicators indicator = null;

        StockTick tick = (StockTick) ebs[0].getUnderlying();
        indicator = stockIndec.get(tick.getStockName());
        if (indicator == null) {
            indicator = new SimpleTechnicalIndicators();
            stockIndec.put(tick.getStockName(), indicator);
        }

        indicator.pushTick(tick);
        double value = indicator.vptRecursive(indicator.getSize()-1);
        sendEvent(tick.getStockName(), value);
    }

    public String getName(){
        return "VPT";
    }
}
