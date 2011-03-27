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

/* Only for monitoring and GUI*/
public class IndicatorData {
    
    public String stockName = null;
    public String indicatorName = null;
    public double indicatorValue = 0.0;
    public DrawType type = DrawType.PORTFOLIO_TABLE;

    public static enum DrawType {
        PORTFOLIO_TABLE,
        STANDALONE_INDICATOR_TABLE
    }


    public String getIndicatorName() {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName) {
        this.indicatorName = indicatorName;
    }

    public double getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(double indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

}
