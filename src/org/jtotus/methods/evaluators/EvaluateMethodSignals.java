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
package org.jtotus.methods.evaluators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import org.jtotus.gui.graph.GraphSender;

/**
 *
 * @author Evgeni Kappinen



    TODO:all data in cents!
 */
public class EvaluateMethodSignals {

    private BigDecimal currentCapital = null;
    private BigDecimal assumedCapital = null;
    private BigDecimal stockCount = null;
    private BigDecimal previousCapital = null;
    private GraphSender graphSender = null;
    private double numberOfWinningTrades = 0;
    private double numberOfLosingTrades = 0;
    private boolean newBestIsFound = false;
    //Best state
    private GraphSender bestResultsGraph = null;
    private BigDecimal currentBestCapital = null;
    private double bestNumberOfWinningTrades = 0;
    private double bestNumberOfLosingTrades = 0;
    
    /* Initialization function, should be run
     * for each state for StateIterator.
     *
     * @param reviewTarget Current stock name
     * @param seriesName   Method related series name
     * @param originalCapital Assumed Capital from portofolio
     * @param stockGraph   Stores values for each iteration of StateIterator.class
     *
     * @return boolean  Currently, Always returns true
     *
     */

    //FIXME: recheck stockGraph usage??
    public boolean initialize(String reviewTarget,
                              String seriesName,
                              Double originalCapital,
                              GraphSender stockGraph) {

        setCurrentCapital(BigDecimal.valueOf(originalCapital));
        assumedCapital = BigDecimal.valueOf(originalCapital);
        
        if(this.getCurrentBestCapital() == null){
            this.setCurrentBestCapital(BigDecimal.valueOf(0.0));
        }
        stockCount = BigDecimal.valueOf(0.0);
        
        /*All points, which lead to signal are stored
           in Graph container. 
         */
        graphSender = new GraphSender(reviewTarget);
        graphSender.setSeriesName(seriesName);
        this.numberOfLosingTrades = 0.0;
        this.numberOfWinningTrades = 0.0;
        newBestIsFound = false;

        return true;
    }

    public BigDecimal brockerExpensePerAction(BigDecimal tradingVolume) {
        //Nordent State 3 -> 0.15% /Min. 7
        BigDecimal payment = tradingVolume.setScale(5, RoundingMode.HALF_UP)
                                          .divide(BigDecimal.valueOf(100.0))
                                          .multiply(BigDecimal.valueOf(0.15));

        if (payment.compareTo(BigDecimal.valueOf(7)) > 0) {
            return payment;
        }

        return BigDecimal.valueOf(7.00);
    }

    public EvaluateMethodSignals buy(double price, int amount) {

        if (getCurrentCapital().subtract(this.brockerExpensePerAction(getCurrentCapital()))
                         .compareTo(BigDecimal.valueOf(0.0)) <= 0) {
           // System.err.printf("There is no money left\n");
            return null;
        }

        if (amount == -1) {//ALL-in
            stockCount = getCurrentCapital().subtract(this.brockerExpensePerAction(getCurrentCapital()))
                                      .divide(BigDecimal.valueOf(price), 3, RoundingMode.HALF_DOWN);

            setPreviousCapital(getCurrentCapital());
            setCurrentCapital(BigDecimal.valueOf(0.0));
            
        } else {
            //TODO: implement
        }

        return this;
    }

    public EvaluateMethodSignals buy(double price, int amount, Date date) {
        this.buy(price, amount);

        String annotation = "Buy";
        graphSender.addForSending(date, price, annotation);
        
        return this;
    }


    public EvaluateMethodSignals sell(double price, int amount) {

        if(stockCount.compareTo(BigDecimal.valueOf(0.0))<=0) {
            return this;
        }
        
        if (amount == -1) {//ALL-in
            setCurrentCapital(stockCount.multiply(BigDecimal.valueOf(price)));
            setCurrentCapital(getCurrentCapital().subtract(this.brockerExpensePerAction(getCurrentCapital())));

            if(getPreviousCapital().compareTo(getCurrentCapital()) >= 0) {
                this.numberOfLosingTrades++;
            }else {
                this.numberOfWinningTrades++;
            }
            
            //Best Capital is found
            if (getCurrentBestCapital().compareTo(getCurrentCapital()) < 0) {
                setCurrentBestCapital(getCurrentCapital());
                bestResultsGraph = graphSender;
                bestNumberOfWinningTrades = numberOfWinningTrades;
                bestNumberOfLosingTrades = numberOfLosingTrades;
                newBestIsFound = true;                
            }

            stockCount = BigDecimal.valueOf(0.0);
        }
        return this;
    }


        public EvaluateMethodSignals sell(double price, int amount, Date date) {
            
            this.sell(price,amount);

            String annotation = "Sell";
            graphSender.addForSending(date, price, annotation);

        return this;
    }

    public BigDecimal getCurrentBestCapital() {
        return currentBestCapital;
    }

    /**
     * @param currentBestCapital the currentBestCapital to set
     */
    public void setCurrentBestCapital(BigDecimal currentBestCapital) {
        this.currentBestCapital = currentBestCapital;
    }


    public void printBestResults() {
        bestResultsGraph.sendAllStored();
    }



    public Double getProfitInProcents() {
        Double tmp =  currentBestCapital.doubleValue();
        return ((tmp / this.assumedCapital.doubleValue()) -1) * 100;
    }

    /**
     * @return the currentCapital
     */
    public BigDecimal getCurrentCapital() {
        return currentCapital;
    }

    /**
     * @param currentCapital the currentCapital to set
     */
    public void setCurrentCapital(BigDecimal currentCapital) {
        this.currentCapital = currentCapital;
    }

    /**
     * @return the previousCapital
     */
    public BigDecimal getPreviousCapital() {
        return previousCapital;
    }

    /**
     * @param previousCapital the previousCapital to set
     */
    public void setPreviousCapital(BigDecimal previousCapital) {
        this.previousCapital = previousCapital;
    }


    public double getWinRatio() {
        return this.bestNumberOfWinningTrades / this.bestNumberOfLosingTrades;

    }

    public void dumpResults() {

        System.out.printf("[----------\n");
        System.out.printf("%s : BestCapital:%f WinRatio:%f\n",
                graphSender == null ? "None" : graphSender.getMainReviewTarget(),
                currentBestCapital.doubleValue(),
                this.getWinRatio());

        System.out.printf("wining trades:%f losing trades:%f\n",
                this.bestNumberOfWinningTrades,
                this.bestNumberOfLosingTrades);

        System.out.printf("-----------]\n");
    }


    public boolean newBest() {
        return newBestIsFound;
    }
}
