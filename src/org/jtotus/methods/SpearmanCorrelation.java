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

package org.jtotus.methods;

import brokerwatcher.eventtypes.MarketData;
import brokerwatcher.indicators.SimpleTechnicalIndicators;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.common.MethodResults;
import org.jtotus.methods.utils.Normalizer;

/**
 *
 * @author Evgeni Kappinen
 */
public class SpearmanCorrelation extends TaLibAbstract implements MethodEntry {
    private String []stockList = null;
    private HashMap<String, double[]> marketData;
    private HashMap<String, HashMap<String, Double>> corMap;

    public SpearmanCorrelation() {
        super();
        marketData = new HashMap<String, double[]>();
        
    }

    public String getMethName() {
        return this.getClass().getSimpleName();
    }

    public boolean isCallable() {
        return true;
    }

    public void run() {
        try {
            this.call();
        } catch (Exception ex) {
            Logger.getLogger(SpearmanCorrelation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private double []fetchStockData(String stockName) {
        List<Double> closingPrices = null;

        double output[] =  marketData.get(stockName);

        if (output == null) {
            output = super.createClosingPriceList(stockName,
                                                  portfolioConfig.inputStartingDate,
                                                  portfolioConfig.inputEndingDate);

            marketData.put(stockName, output);
        }

        return output;
    }

    //TODO:implement for BrokerWatcher as well...
    private void putResults(String from, String to, double correlation) {
        if (corMap==null) {
            corMap = new HashMap<String, HashMap<String, Double>>();
        }

        if(corMap.containsKey(from)) {
            if(!corMap.get(from).containsKey(to)) {
                corMap.get(from).put(to, correlation);
                
            } else {
                System.out.printf("Duplication:%s-%s old:%f new:%f\n", from,to, corMap.get(from).get(to), correlation);
            }
        } else if (corMap.containsKey(to)) {
            if(!corMap.get(to).containsKey(from)) {
                corMap.get(to).put(from, correlation);
            } else {
                if (corMap.get(to).get(from) !=  correlation){
                    System.out.printf("Duplication:%s-%s old:%f new:%f\n", from, to, corMap.get(to).get(from), correlation);
                }
            }
        } else {
            HashMap<String, Double> newEntry = new HashMap<String, Double>();
            newEntry.put(to, correlation);
            corMap.put(from, newEntry);
            return;
        }
    }


    public void dumpResults() {

        for (Map.Entry<String, HashMap<String, Double>> entry: corMap.entrySet()) {
            for (Map.Entry<String, Double> corValue: entry.getValue().entrySet()){
                if (corValue.getValue() > 0.4)
                System.out.printf("%s - %s -> %f \n", entry.getKey(), corValue.getKey(), corValue.getValue());
            }
        }
        
    }

    @Override
    public MethodResults call() throws Exception {
        this.loadPortofolioInputs();
        double cor=0.0;
        
        double bestCor=0.0;
        String bestFrom=null;
        String bestTo=null;

        for (int stockCount = 0; stockCount < portfolioConfig.inputListOfStocks.length; stockCount++) {
            for (int nextStock = 0; nextStock < portfolioConfig.inputListOfStocks.length; nextStock++) {
                if (stockCount == nextStock) {
                    continue;
                }

                double []a = fetchStockData(portfolioConfig.inputListOfStocks[stockCount]);
                double []b = fetchStockData(portfolioConfig.inputListOfStocks[nextStock]);
                cor=SimpleTechnicalIndicators.correlation(a,b);

                if (Math.pow(cor, 2) >bestCor) {
                    bestCor = Math.pow(cor, 2);
                    bestFrom = portfolioConfig.inputListOfStocks[stockCount];
                    bestTo = portfolioConfig.inputListOfStocks[nextStock];
                }
                putResults(portfolioConfig.inputListOfStocks[stockCount],
                           portfolioConfig.inputListOfStocks[nextStock], 
                           cor);
//       
//                System.out.printf("%s - %s -> %f (ticks:%d:%d)\n",
//                                  portfolioConfig.inputListOfStocks[stockCount],
//                                  portfolioConfig.inputListOfStocks[nextStock],
//                                  cor, a.length,b.length);
            }
            //TODO: Others
        }

        dumpResults();
        System.out.printf("Best results: %s - %s -> %f\n", bestFrom, bestTo, cor);

        if (child_config != null && child_config.inputNormilizerType != null) {
            Normalizer norm = new Normalizer();
            //return norm.perform(child_config.inputNormilizerType, methodResults);
        }

        return methodResults;
        
    }

    /**
     * @return the stockList
     */
    public String[] getStockList() {
        return stockList;
    }

    /**
     * @param stockList the stockList to set
     */
    public void setStockList(String[] stockList) {
        this.stockList = stockList;
    }

    public MethodResults runCalculation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MethodResults runCalculation(MarketData data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
