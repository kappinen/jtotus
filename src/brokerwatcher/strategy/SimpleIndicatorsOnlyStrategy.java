package brokerwatcher.strategy;

import brokerwatcher.eventtypes.MarketSignal;
import org.jtotus.common.MethodResults;
import org.jtotus.database.DataFetcher;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
    double bestRsi = Double.MIN_VALUE;
    String stockToBuy = null;
    DataFetcher fetcher = new DataFetcher();



    @Override
    public MarketSignal makeDecision(HashMap<String, MethodResults> inputs) {
        bestRsi = Double.MAX_VALUE;
        MethodResults res = inputs.get("TaLibRSI");
        

        HashMap<String, Double> indResults = res.getResults();
        for (Map.Entry<String, Double> entry :  indResults.entrySet()) {

            if (entry.getKey().compareTo("Nokia Oyj") == 0)
                continue;

            double rsi = entry.getValue();
            if (rsi > 40)
                continue;
            double freq = inputs.get("StatisticsFreqPeriod").getResults().get(entry.getKey());
            if (freq < -0.04f || freq >= 0f) {
                System.out.printf("rejected freq for:%s is :%f\n", entry.getKey(), freq);
                continue;
            }

            if (bestRsi > freq * rsi) {
                System.out.printf("Accpeted : rsi:%f  freq: %f\n", rsi, freq);
                bestRsi = freq * rsi;
                stockToBuy = entry.getKey();
            }
        }

        if (bestRsi == Double.MIN_VALUE) {
            return null;
        }

        DateTime dateToFetch = res.getDate().plusDays(1);
        while(DayisHoliday.isHoliday(dateToFetch)) {
            dateToFetch = dateToFetch.plusDays(1);
        }
        if (res.getDate().isBefore(dateToFetch.plusDays(1))) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
        System.out.printf("price to buy for date %s '%s'\n", formatter.print(dateToFetch), stockToBuy);
        //fixme:ensure that day corresponds to processing day!
        double price = fetcher.fetchClosingPrice(stockToBuy, dateToFetch).doubleValue();

        final MarketSignal signal = new MarketSignal();
        signal.setStockName(stockToBuy);
        signal.setPriceToBuy(price);

        return signal;
    }
}
