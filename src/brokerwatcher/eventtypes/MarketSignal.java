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

package brokerwatcher.eventtypes;

/**
 *
 * @author Evgeni Kappinen
 */
public class MarketSignal {
    //Analyzer name, which generated signal
    private String analyzerName;
    //Prefered direction of the price
    private int priceDirection;
    //Timestamp, when signal was generated
    private long timestamp;
    //Prefered amount of stocks to perform action, determend by analyzer
    private int numOfStocksForAction =  -1;
    //Preference from analyzer, what is the Stop-Lost price
    private double stopLossPrice;
    private boolean executed = false;

    //Optional: Tick, which triggered signal
    private StockTick tick = null;
    private String stockName = null;
    private double sellBuyPrice = 0.0;
    private SignalType type = SignalType.NOTSET;

    public static enum SignalType {
        NOTSET,
        BUY,
        SELL
    }

    public boolean isBuySignal() {
        if (type == SignalType.BUY) {
            return true;
        }
        return false;
    }
    public boolean isSellSignal() {
        if (type == SignalType.SELL) {
            return true;
        }
        return false;
    }

    public double getPriceToBuy() {
        if (type == SignalType.BUY && sellBuyPrice != 0.0) {
            if (tick != null) {
                return tick.getLatestBuy();
            } else {
                return sellBuyPrice;
            }
        }
        throw new RuntimeException("Incorrect use of getPriceToSBuy");
    }

    public double getPriceToSell() {
        if (type == SignalType.SELL && sellBuyPrice != 0.0) {
            if (tick != null) {
                return tick.getLatestSell();
            } else {
                return sellBuyPrice;
            }
        }
        throw new RuntimeException("Incorrect use of getPriceToSell");
    }

    public void setPriceToBuy(double price) {
        type = SignalType.BUY;
        sellBuyPrice = price;
    }

    public void setPriceToSell(double price) {
        type = SignalType.SELL;
        sellBuyPrice = price;
    }

    public String getStockName() {
        if (tick != null) {
            return tick.getStockName();
        }
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }


    /**
     * @return the executed
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * @param executed the executed to set
     */
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }


    public MarketSignal() {
        timestamp = System.currentTimeMillis();
    }

    /**
     * @return the type
     */
    public SignalType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(SignalType type) {
        this.type = type;
    }

    /**
     * @return the analyzerName
     */
    public String getAnalyzerName() {
        return analyzerName;
    }

    /**
     * @param analyzerName the analyzerName to set
     */
    public void setAnalyzerName(String analyzerName) {
        this.analyzerName = analyzerName;
    }

    /**
     * @return the priceDirection
     */
    public int getPriceDirection() {
        return priceDirection;
    }

    /**
     * @param priceDirection the priceDirection to set
     */
    public void setPriceDirection(int priceDirection) {
        this.priceDirection = priceDirection;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the numOfStocksForAction
     */
    public int getNumOfStocksForAction() {
        return numOfStocksForAction;
    }

    /**
     * @param numOfStocksForAction the numOfStocksForAction to set
     */
    public void setNumOfStocksForAction(int numOfStocksForAction) {
        this.numOfStocksForAction = numOfStocksForAction;
    }

    /**
     * @return the stopLossPrice
     */
    public double getStopLossPrice() {
        return stopLossPrice;
    }

    /**
     * @param stopLossPrice the stopLossPrice to set
     */
    public void setStopLossPrice(double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    /**
     * @return the tick
     */
    public StockTick getTick() {
        return tick;
    }

    /**
     * @param tick the tick to set
     */
    public void setTick(StockTick tick) {
        this.tick = tick;
    }
    

}
