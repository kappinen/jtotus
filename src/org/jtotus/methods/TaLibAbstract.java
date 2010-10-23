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


import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.common.MethodResults;
import java.util.Calendar;
import org.jtotus.common.Helper;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.gui.graph.GraphPacket;
import org.jtotus.gui.graph.GraphSender;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.MainMethodConfig;

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
    public String inputPortofolio = null;
    public String inpuPortfolio = null;
    public Double inputAssumedBudjet = null;
    //General inputs
    public Calendar inputEndingDate = null;
    public String[] inputListOfStocks = null;
    //Modes
    public boolean inputPrintResults = true;
    public boolean inputPerfomDecision = true;
    protected MethodResults methodResults = null;
    protected StockType stockType = null;
    protected MainMethodConfig config = null;


    
    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".") + 1, tmp.length());
    }

    public boolean isCallable() {
        return true;
    }

    public void loadPortofolioInputs() {
        //FIXME: set it in PortfolioDecision
        String portfolio = "OMXHelsinki";

        ConfigLoader<ConfPortfolio> configPortfolio =
                new ConfigLoader<ConfPortfolio>(portfolio);

        if (configPortfolio.getConfig() == null) {
            //Load default values
            ConfPortfolio newPortfolioConfig = new ConfPortfolio();
            configPortfolio.storeConfig(newPortfolioConfig);
        }

        //Get stock names
        configPortfolio.applyInputsToObject(this);
        this.inputPortofolio = portfolio;
    }

    //To override
    public MethodResults performMethod(String stockName) {
        return null;
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

        System.out.printf("inputListOfStocks len:%d\n", inputListOfStocks.length);

        for (int stockCount = 0; stockCount < this.inputListOfStocks.length; stockCount++) {
            this.performMethod(this.inputListOfStocks[stockCount]);
        }

        return methodResults;

    }
}
