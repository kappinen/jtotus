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
package org.jtotus.engine;

import brokerwatcher.BrokerWatcher;
import brokerwatcher.broker.MarketBrokerSimulator;
import brokerwatcher.generators.AccdistGenerator;
import brokerwatcher.generators.IndicatorIndexGenerator;
import brokerwatcher.generators.RsiGenerator;
import brokerwatcher.generators.TickInterface;
import brokerwatcher.generators.VPTGenerator;
import brokerwatcher.generators.VrocGenerator;
import brokerwatcher.listeners.TicksToFile;
import org.jtotus.methods.MethodEntry;
import org.jtotus.methods.DummyMethod;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.gui.JtotusView;
import org.jtotus.database.AutoUpdateStocks;
import org.jtotus.gui.MethodResultsPrinter;
import org.jtotus.methods.GroovyScipts;
import org.jtotus.methods.PotentialWithIn;
import org.jtotus.methods.SpearmanCorrelation;
import org.jtotus.methods.StatisticsFreqPeriod;
import org.jtotus.methods.TaLibEMA;
import org.jtotus.methods.TaLibMACD;
import org.jtotus.methods.TaLibMOM;
import org.jtotus.methods.TaLibRSI;
import org.jtotus.methods.TaLibSMA;
import org.jtotus.threads.*;

/**
 *
 * @author Evgeni Kappinen
 */
public class Engine {
    private static final Engine singleton = new Engine();
    private PortfolioDecision portfolioDecision = null;
    private JtotusView mainWindow = null;
    private ConfPortfolio portfolioConfig = null;
    //GenearatorName, StatementString, Object
    private HashMap<String, HashMap<String, TickInterface>> listOfGenerators = null;
    private final static Log log = LogFactory.getLog(Engine.class);
    private MethodResultsPrinter resultsPrinter = null;
    private BrokerWatcher watcher = null;

    private Engine() {

        watcher = new BrokerWatcher();
        this.prepareMethodsList();
    }


    public HashMap<String, HashMap<String, TickInterface>> getListOfGenerators() {
        return listOfGenerators;
    }

    private void prepareMethodsList() {
        // Available methods

        listOfGenerators = new HashMap<String, HashMap<String, TickInterface>>();
        LinkedList<MethodEntry> listOfLongTermIndicators = new LinkedList<MethodEntry>();
        listOfLongTermIndicators.add(new DummyMethod());
        listOfLongTermIndicators.add(new PotentialWithIn());
        listOfLongTermIndicators.add(new TaLibRSI());
        listOfLongTermIndicators.add(new TaLibSMA());
        listOfLongTermIndicators.add(new TaLibEMA());
        listOfLongTermIndicators.add(new TaLibMOM());
        listOfLongTermIndicators.add(new TaLibMACD());
        listOfLongTermIndicators.add(new SpearmanCorrelation());
        listOfLongTermIndicators.add(new StatisticsFreqPeriod());

        try {
            Class groovyClass = Class.forName("org.jtotus.methods.DecisionScript");
            GroovyScipts scripts = (GroovyScipts) groovyClass.newInstance();
            scripts.loadScripts(listOfLongTermIndicators);

        } catch (InstantiationException ex) {
            log.info("GroovyScipt is disabled : InstantiationException");
        } catch (IllegalAccessException ex) {
            log.info("GroovyScipt is disabled : IllegalAccessException");
        } catch (ClassNotFoundException ex) {
            log.info("GroovyScipt is disabled");
        }

        portfolioDecision = new PortfolioDecision(listOfLongTermIndicators);
    }

    public synchronized static Engine getInstance() {
        return singleton;
    }

    public void setGUI(JtotusView tempView) {
        mainWindow = tempView;
    }

    public synchronized LinkedList<MethodEntry> getMethods() {
        return portfolioDecision.getMethodList();
    }

    public void run() {

        log.info("Engine is started");
        portfolioDecision.checkForAutoStartIndicators();
        //Update market data
        portfolioConfig = ConfPortfolio.getPortfolioConfig();
        String[] stocks = portfolioConfig.inputListOfStocks;
        for (int i = stocks.length - 1; i >= 0; i--) {
            Thread updateThread = new Thread(new AutoUpdateStocks(stocks[i]));
            updateThread.start();
        }

        testRun();
    }

    private void addGeneratorToList(String stmt, TickInterface ticker) {
        HashMap<String, TickInterface> tickerMap = new HashMap<String, TickInterface>();
        tickerMap.put(stmt, ticker);
        listOfGenerators.put(ticker.getName(), tickerMap);
    }

    private void testRun() {

        //TickListenerPrinter printer = new TickListenerPrinter();

        //printer.sendEventsToGui();
        //watcher.addPattern("every tick=StockTick(stockName='Kemira')", printer);
        //watcher.addStatement("select * from StockTick", new TickListenerPrinter());
        //watcher.addStatement("select * from EsperEventRsi", new TickListenerPrinter());


        //addGeneratorToList("select * from StockTick", new ListenerRsiIndicator());
        addGeneratorToList("select * from StockTick", new VrocGenerator());
        addGeneratorToList("select * from StockTick", new AccdistGenerator());
        addGeneratorToList("select * from StockTick", new VPTGenerator());
        addGeneratorToList("select * from StockTick", new RsiGenerator());
        addGeneratorToList("select * from StockTick", new IndicatorIndexGenerator());
        watcher.addStatement("select * from StockTick", new TicksToFile());
        watcher.addStatement("select * from MarketSignal", new MarketBrokerSimulator());

        mainWindow.fetchGeneratorList();
        watcher.call();
    }

    public void train() {
        LinkedList<String> methodNames = mainWindow.getMethodList();
        portfolioDecision.startLongTermMethods(methodNames);
    }

    public synchronized void registerResultsPrinter(MethodResultsPrinter printer) {
        System.out.printf("Registering result printer\n");
        resultsPrinter = printer;

        return;
    }

    public synchronized MethodResultsPrinter getResultsPrinter() {
        return resultsPrinter;
    }

    public void startHistorySimulator() {
        watcher.startHistoryGenerator();
    }

    public void startMarketTicker() {
        watcher.startTicker();
    }
}
