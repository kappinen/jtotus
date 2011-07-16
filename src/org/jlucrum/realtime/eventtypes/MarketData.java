package org.jlucrum.realtime.eventtypes;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;

/**
 * This file is part of JTotus.
 * <p/>
 * jTotus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * jTotus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
* Created by IntelliJ IDEA.
* Author: Evgeni Kappinen
* Date: 4/30/11
* Time: 5:29 PM
*/
public class MarketData {
    //StockName, Values
    public Map<String, double[]> data;
    public MarketDataType type = MarketDataType.NotSet;
    public DateTime date;

    public MarketData() {
        data = Collections.synchronizedMap(new HashMap<String, double[]>());
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date.toDateTime();
    }

    static private enum MarketDataType {
        NotSet,
        ClossingPrice,
        Volume
    }

    public MarketData setAsClosingPrice() {
        type = MarketData.MarketDataType.ClossingPrice;
        return this;
    }

    public boolean isClosingPrice() {
        return (type == MarketData.MarketDataType.ClossingPrice);
    }

}
