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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlucrum.realtime.BrokerWatcher;
import org.jlucrum.realtime.eventtypes.MarketData;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.jtotus.common.MethodResults;
import org.joda.time.DateTime;
import org.jtotus.network.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.MainMethodConfig;
import org.jtotus.methods.utils.Normalizer;

/**
 * @author Evgeni Kappinen
 */
public abstract class TaLibAbstract implements UpdateListener {

    /*Stock list */
    private int totalStocksAnalyzed = 0;
    private EPRuntime runtime = null;
    GraphSender sender = null;
    //INPUTS TO METHOD:
    protected MethodResults methodResults = null;
    protected MainMethodConfig child_config = null;
    protected ConfPortfolio portfolioConfig = null;
    private DateTime startTime = null;
    private DateTime endTime = null;
    private MarketData marketData = null;


    public void setMarketData(MarketData data) {
        this.marketData = data;
    }
    
    public String getMethName() {
        return this.getClass().getSimpleName();
    }

    public boolean isCallable() {
        return true;
    }

    public void loadPortofolioInputs() {
        //FIXME: set it in PortfolioDecision

        ConfigLoader<ConfPortfolio> configPortfolio =
                new ConfigLoader<ConfPortfolio>("OMXHelsinki");

        portfolioConfig = configPortfolio.getConfig();
        if (portfolioConfig == null) {
            //Load default values
            portfolioConfig = new ConfPortfolio();
            configPortfolio.storeConfig(portfolioConfig);
        }

        //Get stock names
        configPortfolio.applyInputsToObject(this);
    }

    public double[] createClosingPriceList(String stockName, DateTime start, DateTime end) {

        StockType stockType = new StockType(stockName);
        return stockType.fetchClosingPricePeriod(stockName, start, end);

//        List<Double> closingPrices = new ArrayList<Double>(2000);
//
//        DateIterator dateIter = new DateIterator(start.getTime(),
//                end.getTime());
//
//        //Filling input data with Closing price for days
//        while (dateIter.hasNext()) {
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(dateIter.next());
//            BigDecimal closDay = stockType.fetchClosingPrice(cal);
//            if (closDay != null) {
//                closingPrices.add(closDay.doubleValue());
//            }
//        }
//
//        return ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));
    }

    //To override
    public MethodResults performMethod(String stockName, double[] input) {
        throw new RuntimeException("This methods should be overwritten");
    }

    public void run() {
        try {
            this.call();
        } catch (Exception ex) {
            Logger.getLogger(TaLibAbstract.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MethodResults call() throws Exception {

        if (this.marketData != null) {
            MethodResults ret = runCalculation(this.marketData);
            this.marketData = null;
            return ret;
        }

        this.loadPortofolioInputs();

        methodResults = new MethodResults(this.getMethName());

        for (String stockName : portfolioConfig.inputListOfStocks) {
            double[] input = this.createClosingPriceList(stockName,
                               startTime == null ?portfolioConfig.inputStartingDate : startTime,
                               endTime == null ? portfolioConfig.inputEndingDate : endTime);

            this.performMethod(stockName, input);
        }

        if (child_config != null && child_config.inputNormilizerType != null) {
            Normalizer norm = new Normalizer();
            return norm.perform(child_config.inputNormilizerType, methodResults);
        }

        return methodResults;

    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {
        this.loadPortofolioInputs();

        //Update list of the price
        for (EventBean eb : ebs) {
            if (eb.getUnderlying() instanceof MarketData) {
                final MarketData data = (MarketData) eb.getUnderlying();
                runCalculation(data);
                if (runtime == null) {
                    runtime = BrokerWatcher.getMainEngine().getEPRuntime();
                }
                runtime.sendEvent(methodResults);
            }
        }
    }

    public MethodResults runCalculation() {
        this.loadPortofolioInputs();
        try {
            return call();
        } catch (Exception ex) {
            Logger.getLogger(TaLibRSI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public MethodResults runCalculation(MarketData data) {
        this.loadPortofolioInputs();
        
        methodResults = new MethodResults(this.getMethName());
        for (Map.Entry<String, double[]> stockData : data.data.entrySet()) {
            //System.out.printf("Handeling : %s - > %d\n", stockData.getKey(), ebs.length);
            this.performMethod(stockData.getKey(), stockData.getValue());
        }

        if (child_config != null && child_config.inputNormilizerType != null) {
            Normalizer norm = new Normalizer();
            methodResults = norm.perform(child_config.inputNormilizerType, methodResults);
        }

        methodResults.setDate(data.getDate());
        return methodResults;
    }
}
