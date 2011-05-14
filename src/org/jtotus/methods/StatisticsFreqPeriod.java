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
 *

 */

package org.jtotus.methods;

import java.math.BigDecimal;
import org.jtotus.common.Helper;
import org.jtotus.common.MethodResults;
import org.jtotus.common.StockType;

/**
 * @author Evgeni Kappinen
 */
public class StatisticsFreqPeriod extends TaLibAbstract implements MethodEntry {
    private int maxPeriod = 20;
    private boolean debug = false;
    private static final int POSITIVE = 0;
    private static final int NEGATIVE = 1;
    private static final int STILL = 2;
    private StockType stockType = null;

    public String getMethName() {
        return "StatisticsFreqPeriod";
    }

    private int normilize(double tmp) {

        if (tmp > 0) {
            return 1;
        } else if (tmp < 0) {
            return -1;
        }

        return 0;
    }

    public int lastTrend(String stockName) {
        BigDecimal data = null;
        double table[] = null;
        int mainDirection = 0;
        int strikes = 0;

        if (stockType == null) {
            stockType = new StockType();
        }

        stockType.setStockName(stockName);

        for (int i = 0; i < this.getMaxPeriod(); i++) {

            data = stockType.fetchPastDayClosingPrice(i);
            if (data == null) {
                continue;
            }

            table = Helper.putAsLastToArray(table, data.doubleValue());

            if (table.length < 2) {
                continue;
            }
            strikes = 0;
            mainDirection = normilize(table[1] - table[0]);
            for (int y = 1; y < table.length; y++) {
                double delta = table[y] - table[y - 1];

                if (mainDirection != normilize(delta)) {
                    return -1 * mainDirection * strikes;
                }
                strikes++;
            }
        }

        return 0;
    }

    public int lastTrend(double []table) {
        int mainDirection = 0;
        int strikes = 0;

        if (table.length < 2) {
            System.err.printf("Warning: no data available for %s\n", this.getClass().getSimpleName());
            return Integer.MIN_VALUE;
        }

        for (int i = 1; i < this.getMaxPeriod() + 1; i++) {
            strikes = 0;
            mainDirection = normilize(table[table.length - i] - table[table.length - i - 1]);
            for (int y = table.length - 1; y >= 0 ; y--) {
                double delta = table[y] - table[y - 1];

                if (mainDirection != normilize(delta)) {
                    return mainDirection * strikes;
                }
                strikes++;
            }
        }
        return 0;
    }



    @Override
    public MethodResults performMethod(String stockName, double[] output) {
        int trendInDays = 0;
        int marketStat[][] = null;
        int direct = 0;
        MethodResults results = new MethodResults(this.getMethName());

        marketStat = statisticsForFreq(output);
        if (debug) {
            printResultsToStdout(stockName, marketStat, output.length - 1);
        }

        trendInDays = lastTrend(output);

        if (debug) {
            System.out.printf("Last trend for :%s is : %d\n", output, trendInDays);
        }

        double value = 0.0;
        if (trendInDays > 0) {
            value = marketStat[POSITIVE][Math.abs(trendInDays) + 1];
            direct = 1;
        } else if (trendInDays < 0) {
            value = marketStat[NEGATIVE][Math.abs(trendInDays) + 1];
            direct = -1;
        } else {
            value = marketStat[STILL][Math.abs(trendInDays) + 1];
            direct = 0;
        }

        if (debug) {
            System.out.printf("%s last trend:%d:%d marketstat:%f prop:%f\n",
                    stockName, trendInDays, Math.abs(trendInDays), value,
                    value / (double) output.length);
        }

        methodResults.putResult(stockName, direct * (value / (double) (output.length - 1)));

        return results;
    }

    /**
     * @param output market data
     */

    public int[][] statisticsForFreq(double output[]) {
        int mainDirection = 0;
        int strikes = 0;
        int[][] marketStat = new int[3][getMaxPeriod()];

        //Set fist flow
        if (output.length < 2) {
            return null;
        }

        mainDirection = normilize(output[1] - output[0]); //First direction

        for (int i = 1; i < output.length; i++) {
            double delta = output[i] - output[i - 1];

            if (debug) {
                System.out.printf("[%f:%f] delta:%f direct:%d deltaDirection:%d strikes:%d \n",
                        output[i - 1], output[i], output[i] - output[i - 1], mainDirection, normilize(delta), strikes);
            }

            if (mainDirection != normilize(delta)) {
                if (mainDirection > 0) {
                    marketStat[this.POSITIVE][strikes]++;
                } else if (mainDirection < 0) {
                    marketStat[this.NEGATIVE][strikes]++;
                } else {
                    marketStat[this.STILL][strikes]++;
                }

                mainDirection = normilize(delta);
                strikes = 1;
            } else {
                if (strikes < getMaxPeriod()) {
                    strikes++;
                }
            }
        }

        if (mainDirection > 0) {
            marketStat[this.POSITIVE][strikes]++;
        } else if (mainDirection < 0) {
            marketStat[this.NEGATIVE][strikes]++;
        } else {
            marketStat[this.STILL][strikes]++;
        }

        if (debug) {
            printResultsToStdout("Testing..", marketStat, output.length - 1);
        }

        return marketStat;
    }

    private float pros(int share, float total) {
        return ((float) share / total) * 100;
    }

    private void printResultsToStdout(String stockName, int[][] marketData, int total_days) {
        float total_pos = 0.0f;
        float total_neg = 0.0f;
        float total_neu = 0.0f;
        int rowSum = 0;

        for (int i = 1; i < getMaxPeriod(); i++) {
            total_pos += marketData[this.POSITIVE][i];
            total_neg += marketData[this.NEGATIVE][i];
            total_neu += marketData[this.STILL][i];
        }

        System.out.printf("Data for %s\n", stockName);
        for (int i = 1; i < getMaxPeriod(); i++) {
            if (marketData[this.POSITIVE][i] != 0 || marketData[this.NEGATIVE][i] != 0 || marketData[this.STILL][i] != 0) {
                rowSum = marketData[this.POSITIVE][i] + marketData[this.NEGATIVE][i] + marketData[this.STILL][i];
                System.out.printf("Up: %d (%.2f of total) (%.2f of Ups) [%.2f of Row] |"
                        + " Down: %d (%.2f) [%.2f] |"
                        + " Still: %d (%.2f) [%.2f] -> %d days in a row\n",
                        marketData[this.POSITIVE][i],
                        pros(marketData[this.POSITIVE][i], total_days),
                        pros(marketData[this.POSITIVE][i], total_pos),
                        pros(marketData[this.POSITIVE][i], (float) rowSum),
                        marketData[this.NEGATIVE][i],
                        pros(marketData[this.NEGATIVE][i], total_neg),
                        pros(marketData[this.NEGATIVE][i], (float) rowSum),
                        marketData[this.STILL][i],
                        pros(marketData[this.STILL][i], total_neu),
                        pros(marketData[this.STILL][i], (float) rowSum),
                        i);
            }

        }
        System.out.printf("total days:%d \n", total_days);
    }

    public boolean isCallable() {
        return true;
    }


    /**
     * @return the maxPeriod
     */
    public int getMaxPeriod() {
        return maxPeriod;
    }

    /**
     * @param maxPeriod the maxPeriod to set
     */
    public void setMaxPeriod(int maxPeriod) {
        this.maxPeriod = maxPeriod;
    }
}
