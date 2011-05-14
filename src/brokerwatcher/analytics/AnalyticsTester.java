

package brokerwatcher.analytics;

import brokerwatcher.indicators.SimpleTechnicalIndicators;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.database.DataFetcher;

/**
 *
 * @author Evgeni Kappinen
 */
public class AnalyticsTester {

    public static void test1() {
        ConfPortfolio config = ConfPortfolio.getPortfolioConfig();
        DataFetcher fetcher = new DataFetcher();
//        portfolioConfig.inputListOfStocks
        DateTime time = new DateTime().minusDays(900);
        DateTime timeEnd = new DateTime();

        HashMap<String, Double> abCorr = new HashMap<String, Double>();
        HashMap<String, Double> abLags = new HashMap<String, Double>();
        HashMap<String, HashSet<String>> abLink = new HashMap<String, HashSet<String>>();

//        double[] aValues = fetcher.fetchClosingPricePeriod("Neste Oil", time, timeEnd);
//        double[] bValues = fetcher.fetchClosingPricePeriod("Pohjola Bank A", time, timeEnd);
//        System.out.printf("len:%d - len:%d\n", aValues.length, bValues.length);
//        SimpleTechnicalIndicators.executeR("ccf(na.omit(SMA(a, 8)), na.omit(SMA(b, 8)));", aValues, bValues);
//        SimpleTechnicalIndicators.executeR("stl(log(a), \"per\");", aValues, bValues);


        for (String a : config.inputListOfStocks) {
            for (String b : config.inputListOfStocks) {
                if (a.compareTo(b) == 0) {
                    continue;
                }

                double[] aValues = fetcher.fetchClosingPricePeriod(a, time, timeEnd);
                double[] bValues = fetcher.fetchClosingPricePeriod(b, time, timeEnd);
                System.out.printf("%s -> %s have: ", a, b);
                double[] res = SimpleTechnicalIndicators.crossCorrelation(aValues, bValues, a, b);

                if (res[0] > 0.8f) {
                    HashSet<String> links = null;
                    if (abLink.containsKey(a)) {
                        links = abLink.get(a);
                    } else {
                        links = new HashSet<String>();
                    }
                    links.add(b);
                    abLink.put(a, links);
                }

                if (abCorr.containsKey(a + "-" + b)) {
                    Double ret = abCorr.get(a + "-" + b);
                    if (ret < res[0]) {
                        abCorr.put(a + "-" + b, res[0]);
                        abLags.put(a + "-" + b, res[1]);
                    }
                } else if (abCorr.containsKey(b + "-" + a)) {
                    Double ret = abCorr.get(b + "-" + a);
                    if (ret < res[0]) {
                        abCorr.put(b + "-" + a, res[0]);
                        abLags.put(b + "-" + a, res[1]);
                    }
                } else {
                    abCorr.put(a + "-" + b, res[0]);
                    abLags.put(a + "-" + b, res[1]);
                }
            }
        }

        System.out.println("StockMarket Links:");
        for (Entry<String, HashSet<String>> entry : abLink.entrySet()) {
            System.out.printf("%s has (%d): ", entry.getKey(), entry.getValue().size());
            for (String depStocks : entry.getValue()) {
                System.out.printf(" %s", depStocks);
            }
            System.out.printf("\n");
            System.out.flush();
        }

        System.out.println("StockMarket cross-correlations:");
        for (Entry<String, Double> entry : abCorr.entrySet()) {
            Double lag = abLags.get(entry.getKey());
            System.out.printf("%s - %f - lag: %f\n", entry.getKey(), entry.getValue(), lag);
        }
    }
}
