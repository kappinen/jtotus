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

    private BigDecimal currentBudjet = null;
    private BigDecimal assumedBudjet = null;
    private BigDecimal currentBestBudjet = null;
    private BigDecimal stockCount = null;
    private long statActions = 0;
    
    private GraphSender bestResultsGraph = null;
    private GraphSender graphSender = null;



    private double statesCount = 0.0f;


    /* Initialization function, should be run
     * for each state for StateIterator.
     *
     * @param reviewTarget Current stock name
     * @param seriesName   Method related series name
     * @param originalBudjet Assumed budjet from portofolio
     * @param stockGraph   Stores values for each iteration of StateIterator.class
     *
     * @return boolean  Currently, Always returns true
     *
     */
    public boolean initialize(String reviewTarget,
                              String seriesName,
                              Double originalBudjet,
                              GraphSender stockGraph) {

        setCurrentBudjet(BigDecimal.valueOf(originalBudjet));
        assumedBudjet = BigDecimal.valueOf(originalBudjet);
        
        if(this.getCurrentBestBudjet() == null){
            this.setCurrentBestBudjet(BigDecimal.valueOf(0.0));
        }
        stockCount = BigDecimal.valueOf(0.0);
        setStatActions(0);
        
        /*All points, which lead to signal are stored
           in Graph container. 
         */
        graphSender = new GraphSender(reviewTarget);
        graphSender.setSeriesName(seriesName);

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

        if (getCurrentBudjet().subtract(this.brockerExpensePerAction(getCurrentBudjet()))
                         .compareTo(BigDecimal.valueOf(0.0)) <= 0) {
           // System.err.printf("There is no money left\n");
            return null;
        }

        if (amount == -1) {//ALL-in
            stockCount = getCurrentBudjet().subtract(this.brockerExpensePerAction(getCurrentBudjet()))
                                      .divide(BigDecimal.valueOf(price), 3, RoundingMode.HALF_DOWN);

            setCurrentBudjet(BigDecimal.valueOf(0.0));
            setStatActions(getStatActions() + 1);
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

        if (amount == -1) {//ALL-in
            setCurrentBudjet(stockCount.multiply(BigDecimal.valueOf(price)));
            setCurrentBudjet(getCurrentBudjet().subtract(this.brockerExpensePerAction(getCurrentBudjet())));

            //Best Budjet is found
            if (getCurrentBestBudjet().compareTo(getCurrentBudjet()) < 0) {
                setCurrentBestBudjet(getCurrentBudjet());
                bestResultsGraph = graphSender;
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

    public BigDecimal getCurrentBestBudjet() {
        return currentBestBudjet;
    }

    /**
     * @param currentBestBudjet the currentBestBudjet to set
     */
    public void setCurrentBestBudjet(BigDecimal currentBestBudjet) {
        this.currentBestBudjet = currentBestBudjet;
    }

    /**
     * @return the statActions
     */
    public long getStatActions() {
        return statActions;
    }

    /**
     * @param statActions the statActions to set
     */
    public void setStatActions(long statActions) {
        this.statActions = statActions;
    }

    public void printBestResults() {
        bestResultsGraph.sendAllStored();
    }



    public Double getProfitInProcents() {
        Double tmp =  currentBestBudjet.doubleValue();
        return ((tmp / this.assumedBudjet.doubleValue()) -1) * 100;
    }

    /**
     * @return the currentBudjet
     */
    public BigDecimal getCurrentBudjet() {
        return currentBudjet;
    }

    /**
     * @param currentBudjet the currentBudjet to set
     */
    public void setCurrentBudjet(BigDecimal currentBudjet) {
        this.currentBudjet = currentBudjet;
    }

}
