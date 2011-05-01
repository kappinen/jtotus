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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import brokerwatcher.eventtypes.MarketData;
import brokerwatcher.eventtypes.StockTick;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.common.MethodResults;
import java.util.Calendar;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphPacket;
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
    GraphSender sender = null;
    //INPUTS TO METHOD:

    protected MethodResults methodResults = null;

    protected MainMethodConfig child_config = null;
    protected ConfPortfolio portfolioConfig = null;


    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".") + 1, tmp.length());
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

    public double[] createClosingPriceList(String stockName, Calendar start, Calendar end) {

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
        this.loadPortofolioInputs();

        methodResults = new MethodResults(this.getMethName());

        for (int stockCount = 0; stockCount < portfolioConfig.inputListOfStocks.length; stockCount++) {
            double[] input = this.createClosingPriceList(portfolioConfig.inputListOfStocks[stockCount],
                    portfolioConfig.inputStartingDate,
                    portfolioConfig.inputEndingDate);


            this.performMethod(portfolioConfig.inputListOfStocks[stockCount], input);
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
        for (int i = 0; i < ebs.length; i++) {
            if (ebs[i].getUnderlying() instanceof MarketData) {
                MarketData data = (MarketData)ebs[i].getUnderlying();
                for (Map.Entry<String,double[]> stockData : data.data.entrySet()) {
                    this.performMethod(stockData.getKey(), stockData.getValue());
                }

                if (child_config != null && child_config.inputNormilizerType != null) {
                    Normalizer norm = new Normalizer();
                    methodResults = norm.perform(child_config.inputNormilizerType, methodResults);
                }
             //TODO: send data as results!
            }
        }
    }


}
