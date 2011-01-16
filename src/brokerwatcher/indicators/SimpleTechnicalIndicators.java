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
package brokerwatcher.indicators;

import brokerwatcher.eventtypes.StockTick;

/**
 *
 * @author Evgeni Kappinen
 */
public class SimpleTechnicalIndicators extends StockIndicator<StockTick> {

    public SimpleTechnicalIndicators() {
        super();
    }

    public double VOLUME(int i) {
        return super.getTick(i).getVolume();
    }

    public double LATESTPRICE(int i) {
        return super.getTick(i).getLatestPrice();
    }

    // Volume Rate of Change (VROC)
    // Source: http://ta.mql4.com/indicators/volumes/rate_of_change
    public double vroc(int iIndex, int n) {
        if (iIndex - n <= 0) {
            return 0;
        }

        return ((VOLUME(iIndex) - VOLUME(iIndex - n)) / VOLUME(iIndex - n)) * 100;
    }

    public double vrocMultPrice(int iIndex, int n) {
        if (iIndex - n <= 0) {
            return 0;
        }

        double volume = ((VOLUME(iIndex) - VOLUME(iIndex - n)) / VOLUME(iIndex - n)) * 100;
        double curPrice = ((LATESTPRICE(iIndex) - LATESTPRICE(iIndex - n)) / LATESTPRICE(iIndex - n)) * 100;
        return (volume * curPrice);
    }

    public static double vrocVolume(double ithVoluome, double iNthVoluome) {
        return ((ithVoluome - iNthVoluome) / iNthVoluome) * 100;
    }

    // Price and Volume Trend, for dayily data
    // Source: http://ta.mql4.com/indicators/volumes/price_trend
    public double pvtRecursive(int ithIndex) {
        if (ithIndex - 1 < 0) {
            return 0.0d;
        }

        return ((LATESTPRICE(ithIndex) - LATESTPRICE(ithIndex - 1)) / LATESTPRICE(ithIndex - 1)) / VOLUME(ithIndex) + pvtRecursive(ithIndex - 1);
    }

}
