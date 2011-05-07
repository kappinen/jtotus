package brokerwatcher.strategy;

import brokerwatcher.eventtypes.MarketSignal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.jtotus.common.MethodResults;
import org.jtotus.database.DataFetcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jtotus.common.DayisHoliday;


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
* Date: 5/3/11
* Time: 7:48 PM
*/
public class SimpleIndicatorsOnlyStrategy implements DecisionStrategy {
    double bestRsi = Double.MAX_VALUE;
    String stockToBuy = null;
    DataFetcher fetcher = new DataFetcher();



    @Override
    public MarketSignal makeDecision(HashMap<String, MethodResults> inputs) {
        bestRsi = Double.MIN_VALUE;
        MethodResults res = inputs.get("TaLibRSI");
        MarketSignal signal = new MarketSignal();

        HashMap<String, Double> indResults = res.getResults();
        for (Map.Entry<String, Double> entry :  indResults.entrySet()) {

            if (entry.getKey().compareTo("Nokia Oyj") == 0)
                continue;
            if (bestRsi < entry.getValue()) {
                bestRsi = entry.getValue();
                stockToBuy = entry.getKey();
            }
        }

        if (bestRsi > 30) {
            return null;
        }

        signal.setStockName(stockToBuy);
        Calendar dateToFetch = res.getDate();
        dateToFetch.add(Calendar.DATE, 1);
        while(DayisHoliday.isHoliday(dateToFetch)) {
            dateToFetch.add(Calendar.DATE, 1);
        }
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        System.out.printf("price to buy for date %s '%s'\n", format.format(dateToFetch.getTime()), stockToBuy);
        //fixme:ensure that day corresponds to processing day!
        double price  = fetcher.fetchClosingPrice(stockToBuy, dateToFetch)
                               .doubleValue();
        signal.setPriceToBuy(price);


        return signal;
    }
}
