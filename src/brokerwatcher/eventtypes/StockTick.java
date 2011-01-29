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
public class StockTick {
    private String stockName = null;

    private double latestPrice = 0.0f;
    private double latestBuy = 0.0f;
    private double latestSell = 0.0f;
    private double latestHighest = 0.0f;
    private double latestLowest = 0.0f;
    private double volume = 0.0f;
    private double revenue = 0.0f;
    private String time = null;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public double getLatestBuy() {
        return latestBuy;
    }

    public void setLatestBuy(double latestBuy) {
        this.latestBuy = latestBuy;
    }

    public double getLatestHighest() {
        return latestHighest;
    }

    public void setLatestHighest(double latestHighest) {
        this.latestHighest = latestHighest;
    }

    public double getLatestLowest() {
        return latestLowest;
    }

    public void setLatestLowest(double latestLowest) {
        this.latestLowest = latestLowest;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public double getLatestSell() {
        return latestSell;
    }

    public void setLatestSell(double latestSell) {
        this.latestSell = latestSell;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public double getTradesSum() {
        return revenue;
    }

    public void setTradesSum(double tradesSum) {
        this.revenue = tradesSum;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    
    @Override
    public String toString() {
        return "StockName=" + this.getStockName() +
                ", GetLastestBuy="+this.getLatestBuy() +
                ", GetLastestHighest="+this.getLatestHighest() +
                ", GetLastestLowest="+this.getLatestLowest() +
                ", GetLastestPrice="+this.getLatestPrice() +
                ", GetLastestSell="+this.getLatestSell() +
                ", Volume="+this.getVolume() +
                ", TradesSum="+this.getTradesSum() +
                ", Time="+this.getTime();
    }
}
