

package org.jlucrum.realtime.analytics;

import org.jlucrum.realtime.indicators.SimpleTechnicalIndicators;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.jtotus.common.StockNames;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.database.DataFetcher;
import org.jtotus.database.StockMerger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 *
 * @author Evgeni Kappinen
 */
public class AnalyticsTester {

    public static void test1() {
        ConfPortfolio config = ConfPortfolio.getPortfolioConfig();
        DateTime time = new DateTime().minusDays(100);
        DateTime timeEnd = new DateTime();

        HashMap<String, Double> abCorr = new HashMap<String, Double>();
        HashMap<String, Double> abLags = new HashMap<String, Double>();
        HashMap<String, HashSet<String>> abLink = new HashMap<String, HashSet<String>>();

//        double[] aValues = fetcher.fetchClosingPricePeriod("Neste Oil", time, timeEnd);
//        double[] bValues = fetcher.fetchClosingPricePeriod("Pohjola Bank A", time, timeEnd);
//        System.out.printf("len:%d - len:%d\n", aValues.length, bValues.length);
//        SimpleTechnicalIndicators.executeR("ccf(na.omit(SMA(a, 8)), na.omit(SMA(b, 8)));", aValues, bValues);
//        SimpleTechnicalIndicators.executeR("stl(log(a), \"per\");", aValues, bValues);


        
        StockNames names = new StockNames();
        for (String a : names.getNames()) {
//            for (String b : names.getNames()) {
            for (String b : names.getExternals()) {
                if (a.compareTo(b) == 0) {
                    continue;
                }

                StockMerger merge = new StockMerger();
                double [][]values = merge.mergedPeriods(a, b, time, timeEnd);
                System.out.printf("AnalyticsTester : %s -> %s have: %d \n", a, b, values[0].length);
                double[] res = SimpleTechnicalIndicators.crossCorrelation(values[0], values[1], a, b);

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

    public static void testVolume() {
        DateTime time = new DateTime().minusDays(100);
        DateTime timeEnd = new DateTime();
        String b = "Volume";
        DataFetcher fetcher = new DataFetcher();
        HashMap<String, Double> abCorr = new HashMap<String, Double>();
        HashMap<String, Double> abLags = new HashMap<String, Double>();
        HashMap<String, HashSet<String>> abLink = new HashMap<String, HashSet<String>>();

        StockNames names = new StockNames();
        for (String a : names.getNames()) {
            double[] close = fetcher.fetchClosingPricePeriod(a, time, timeEnd);
            double[] volume = fetcher.fetchVolumePeriod(a, time, timeEnd);

            double[] res = SimpleTechnicalIndicators.crossCorrelation(close, volume, a, b);
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
    
    public static void test2() {
        try {
            ConfPortfolio config = ConfPortfolio.getPortfolioConfig();
            DateTime time = new DateTime().minusDays(100);
            DateTime timeEnd = new DateTime();
            DataFetcher fetcher = new DataFetcher();
            
            StockMerger merge = new StockMerger();
//            double[][] values2 = merge.mergedPeriods("Neste Oil", "Tieto Oyj", time, timeEnd);
//            double[][] values = merge.mergedPeriods("Outotec Oyj","Metso Oyj", time, timeEnd);
            
            double[] svalues = fetcher.fetchClosingPricePeriod("Metso Oyj", time, timeEnd);
            double[] svvalues = fetcher.fetchVolumePeriod("Metso Oyj", time, timeEnd);
            
            
//            SimpleTechnicalIndicators.execute("a<-%d;plot(density(diff(log(a))), type=\"l\")", values);
//            SimpleTechnicalIndicators.execute("a<-%d;plot(diff(a), col=1, type=\"l\")", values);
//            SimpleTechnicalIndicators.execute("lines(diff(a,2), col=\"red\");");
//            System.out.printf("Got:%d\n", values[1].length);
//            double vo = SimpleTechnicalIndicators.execute("a<-%d; b<-%d; ab <- ccf(diff(a),diff(b)); max(ab$acf)", values[0], values[1])
//                    .asDouble();
//            double vo1 = SimpleTechnicalIndicators.execute("a<-%d; b<-%d; ab <- ccf(diff(a),diff(b)); max(ab$acf)", values2[0], values2[1])
//                    .asDouble();
            double vo = SimpleTechnicalIndicators.execute("a<-%d; b<-%d; ab <- ccf(diff(a), diff(b)); max(ab$acf)", svalues, svvalues)
                    .asDouble();


            System.out.printf("Estimator:%f and \n", vo);
//            SimpleTechnicalIndicators.plotAB(values[0], values[1]);
        }
        catch (REngineException ex) {
            Logger.getLogger(AnalyticsTester.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (REXPMismatchException ex) {
            Logger.getLogger(AnalyticsTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
