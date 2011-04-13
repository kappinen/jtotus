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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.common.MethodResults;
import java.util.Calendar;
import java.util.List;
import org.jtotus.common.DateIterator;
import org.jtotus.common.Helper;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphPacket;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.MainMethodConfig;
import org.jtotus.methods.utils.Normalizer;

/**
 *
 * @author Evgeni Kappinen
 */
public abstract class TaLibAbstract {

    /*Stock list */
    private Helper help = Helper.getInstance();
    private Double avgSuccessRate = null;
    private int totalStocksAnalyzed = 0;
    GraphPacket packet = null;
    GraphSender sender = null;
    //TODO: staring date, ending date aka period
    //INPUTS TO METHOD:
    //Portofolio
    public String inputPortofolio = "OMXHelsinki";
    public String inpuPortfolio = null;
    //General inputs
    public Calendar inputEndingDate = null;
    //Modes
    public boolean inputPrintResults = true;
    public boolean inputPerfomDecision = true;
    protected MethodResults methodResults = null;
    protected StockType stockType = null;
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

    public List<Double> createClosingPriceList(String stockName, Calendar start, Calendar end) {

         List<Double> closingPrices = new ArrayList<Double>(2000);

         DateIterator dateIter = new DateIterator(start.getTime(),
                                                  end.getTime());

         if (stockType==null) {
             stockType = new StockType();
         }
         //Guranteed to be created by call()
         stockType.setStockName(stockName);

        //Filling input data with Closing price for days
        while (dateIter.hasNext()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateIter.next());
            BigDecimal closDay = stockType.fetchClosingPrice(cal);
            if (closDay != null) {
                closingPrices.add(closDay.doubleValue());
            }
        }
         return closingPrices;
    }

    //To override
    public MethodResults performMethod(String stockName) {
        throw new RuntimeException("This methods should be overritten");
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

        avgSuccessRate = new Double(0.0f);
        packet = new GraphPacket();
        stockType = new StockType();
        methodResults = new MethodResults(this.getMethName());

        System.out.printf("inputListOfStocks len:%d\n", portfolioConfig.inputListOfStocks.length);

        for (int stockCount = 0; stockCount < portfolioConfig.inputListOfStocks.length; stockCount++) {
            this.performMethod(portfolioConfig.inputListOfStocks[stockCount]);
        }

        if (child_config != null && child_config.inputNormilizerType != null) {
            Normalizer norm = new Normalizer();
            return norm.perform(child_config.inputNormilizerType, methodResults);
        }

        return methodResults;

    }
}
