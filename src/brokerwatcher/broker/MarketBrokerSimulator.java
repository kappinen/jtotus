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

package brokerwatcher.broker;

import brokerwatcher.eventtypes.MarketSignal;
import brokerwatcher.eventtypes.StockTick;
import brokerwatcher.generators.TickAnalyzer;
import com.espertech.esper.client.EventBean;
import java.util.ArrayList;
import java.util.HashMap;
import org.jtotus.methods.evaluators.EvaluateMethodSignals;

/**
 * Simplified version of Market Broker
 * 
 * @author Evgeni Kappinen
 */
public class MarketBrokerSimulator extends TickAnalyzer implements MarketBroker {
    //<StockName, Signal>
    private HashMap<String, ArrayList<MarketSignal>> marketSignals = null;
    private HashMap<String, StockTick> latestMarketData = null;
    private EvaluateMethodSignals buySellBroker = new EvaluateMethodSignals();
    private boolean debug = true;

    private static enum SignalStrategy {
        DROP,
        ACCEPT,
        IGNORE_STORE
    }

    public MarketBrokerSimulator() {
        super();
        marketSignals = new HashMap<String, ArrayList<MarketSignal>>();
        latestMarketData = new HashMap<String, StockTick>();
    }

    public String getListnerInfo() {
        return "Market Broker Simulator";
    }

    private SignalStrategy signalStrategy(MarketSignal signal) {
        //TODO:logic
        if (signal.isBuySignal() && buySellBroker.getStockCount() == 0) {
            return SignalStrategy.ACCEPT;
        } else if (signal.isSellSignal() && buySellBroker.getStockCount() != 0) {
            return SignalStrategy.ACCEPT;
        }
        return SignalStrategy.DROP;
    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {
        ArrayList<MarketSignal> signals;
        
        for (EventBean eb : ebs) {
            if (eb instanceof StockTick) {
                StockTick tick = (StockTick) eb.getUnderlying();
                if (tick != null) {
                    //TODO:Stop-Loss
                    latestMarketData.put(tick.getStockName(), tick);
                }
            } else if (eb.getUnderlying() instanceof MarketSignal) {
                MarketSignal signal = (MarketSignal) eb.getUnderlying();
                
                switch(signalStrategy(signal)) {
                    case ACCEPT:
                        break;
                    case IGNORE_STORE:
                        signals = marketSignals.get(signal.getStockName());
                        signals.add(signal);
                    case DROP:
                    default:
                        continue;
                }

                switch(signal.getType()) {
                    case BUY:
                        buySellBroker.buy(signal.getPriceToBuy(), signal.getNumOfStocksForAction());
                        if (debug) {
                            System.out.printf("%s buy %s : %.4f\n",
                                    this.getClass().getSimpleName(),
                                    signal.getStockName(), signal.getPriceToBuy());
                        }
                        break;
                    case SELL:
                        buySellBroker.sell(signal.getPriceToSell(), signal.getNumOfStocksForAction());
                        if (debug) {
                            System.out.printf("%s sell %s : %.4f\n",
                                    this.getClass().getSimpleName(),
                                    signal.getStockName(), signal.getPriceToSell());
                        }
                        break;
                    default:
                        continue;
                }

                signal.setExecuted(true);
                if (!marketSignals.containsKey(signal.getStockName())) {
                    signals = new ArrayList<MarketSignal>();
                } else {
                    signals = marketSignals.get(signal.getStockName());
                }

                signals.add(signal);
                marketSignals.put(signal.getStockName(), signals);

                System.out.printf("Budget:%d\n", buySellBroker.getCurrentCapital().intValue());
            }
        }
    }

}
