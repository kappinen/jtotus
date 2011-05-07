package brokerwatcher.training;

import brokerwatcher.BrokerWatcher;
import brokerwatcher.eventtypes.MarketSignal;
import brokerwatcher.strategy.DecisionStrategy;
import brokerwatcher.strategy.SimpleIndicatorsOnlyStrategy;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.jtotus.common.DayisHoliday;
import org.jtotus.common.MethodResults;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.ConfTrainWithLongTermIndicators;
import org.jtotus.config.ConfigLoader;
import org.jtotus.database.DataFetcher;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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
* Date: 5/2/11
* Time: 6:38 PM
*/
public class TrainManager implements UpdateListener {
    private HashMap<String, MethodResults> inputs = new HashMap<String, MethodResults>();
    private ArrayList<String> waitForLongTermIndicators = null;
    private final ConfPortfolio portfolio = ConfPortfolio.getPortfolioConfig();
    private final DecisionStrategy strategy = new SimpleIndicatorsOnlyStrategy();
    private final EPRuntime epRuntime = BrokerWatcher.getMainEngine().getEPRuntime();
    private final DataFetcher fetcher = new DataFetcher();
    private Calendar currentDate = null;


    public TrainManager() {
        currentDate = portfolio.inputStartingDate;
        EPServiceProvider cep = BrokerWatcher.getMainEngine();
        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement eps = cepAdm.createEPL("select * from MethodResults");
        eps.addListener(this);
    }

    private boolean isAutoStarted(String indicator) {

        if (waitForLongTermIndicators == null) {
            waitForLongTermIndicators = (ArrayList<String>) portfolio.autoStartedMethods.clone();
        }

        return waitForLongTermIndicators.contains(indicator);
    }

    private void train(MethodResults results) {

        if (!inputs.containsKey(results.getMethodName()) && isAutoStarted(results.getMethodName())) {
            inputs.put(results.getMethodName(), results);

            if (waitForLongTermIndicators.size() == inputs.size()) {
                //TODO: all inputs are available, perform test
                Calendar endDay = performStrategyTest();
                if (endDay != null) {
                    inputs.clear();
                    fetcher.sendMarketData(portfolio.inputListOfStocks, portfolio.inputStartIndicatorDate, endDay);
                }
            }

        } else {
            System.err.println("BUG: all inputs did not arrived on time !");
        }
    }

    private Calendar performStrategyTest() {
        boolean sold = false;
        MarketSignal signal = strategy.makeDecision(inputs);

        if (signal != null) {
            epRuntime.sendEvent(signal); // Buying

            BigDecimal maxWin = BigDecimal.valueOf(signal.getPriceToBuy()).multiply(BigDecimal.valueOf(1.015));
            BigDecimal maxLoss = BigDecimal.valueOf(signal.getPriceToBuy()).multiply(BigDecimal.valueOf(0.97));

            while (!sold) {
                currentDate.add(Calendar.DATE, 1);
                if (currentDate.after(portfolio.inputEndingDate)) {
                    System.out.printf("%s is done!\n", this.getClass().getSimpleName());
                    return null;
                }
                
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                System.out.printf("Train manager %s\n", format.format(currentDate.getTime()));
                BigDecimal price = fetcher.fetchClosingPrice(signal.getStockName(), currentDate);

                if (price == null) {
                    continue;
                }

                if (price.compareTo(maxWin) > 0 || price.compareTo(maxLoss) <= 0) {
                    sold = true;
                    signal.setPriceToSell(price.doubleValue());
                    epRuntime.sendEvent(signal); // Buying
                }
            }
        }

        //TODO: fetcher.sendMarketData(portfolio.inputListOfStocks, )
        currentDate.add(Calendar.DATE, 1);
        return currentDate;
    }


    public void startTraining() {
    ConfigLoader<ConfTrainWithLongTermIndicators> loader
            = new ConfigLoader<ConfTrainWithLongTermIndicators>("ConfTrainWithLongTermIndicators");
        ConfTrainWithLongTermIndicators config = loader.getConfig();
        if (config ==null) {
            config = new ConfTrainWithLongTermIndicators();
            loader.storeConfig(config);
        }

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, -1*config.indicatorPeriodLength);

        fetcher.sendMarketData(portfolio.inputListOfStocks, startDate, currentDate);
    }

    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {

        for (EventBean bean : eventBeans) {
            if (bean.getUnderlying() instanceof MethodResults) {

                MethodResults results = (MethodResults)bean.getUnderlying();

                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                System.out.printf("%s processing : %s - %s dates Method:%s date:%s\n",
                        this.getClass().getSimpleName(),
                        format.format(currentDate.getTime()), 
                        format.format(portfolio.inputEndingDate.getTime()),
                        results.getMethodName(),
                        format.format(results.getDate().getTime()));
                if (currentDate.before(portfolio.inputEndingDate)) {
                    this.train(results);
                } else {
                    System.out.printf("Data is not available for testing\n");
                }
            }
        }
    }
}
