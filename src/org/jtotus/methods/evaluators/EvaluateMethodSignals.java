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

/**
 *
 * @author Evgeni Kappinen



    TODO:all data in cents!
 */
public class EvaluateMethodSignals {

    private BigDecimal currentBudjet = null;
    private BigDecimal currentBestBudjet = null;
    private BigDecimal stockCount = null;
    private long statActions = 0;
    
    public void initialize(Double assumedBudjet) {
        currentBudjet = BigDecimal.valueOf(assumedBudjet);
        setCurrentBestBudjet(BigDecimal.valueOf(0.0));
        stockCount = BigDecimal.valueOf(0.0);
        setStatActions(0);
    }

    public BigDecimal brockerExpensePerAction(BigDecimal tradingVolume) {
        //Nordent State 3 -> 0.15% /Min. 7
        BigDecimal payment = tradingVolume.setScale(5, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(100.0)).multiply(BigDecimal.valueOf(0.15));

        if (payment.compareTo(BigDecimal.valueOf(7)) > 0) {
            return payment;
        }

        return BigDecimal.valueOf(7.00);
    }

    public EvaluateMethodSignals buy(double price, int amount) {

        if (currentBudjet.subtract(this.brockerExpensePerAction(currentBudjet))
                         .compareTo(BigDecimal.valueOf(0.0)) <= 0) {
           // System.err.printf("There is no money left\n");
            return null;
        }

        if (amount == -1) {//ALL-in
            stockCount = currentBudjet.subtract(this.brockerExpensePerAction(currentBudjet))
                                      .divide(BigDecimal.valueOf(price), 3, RoundingMode.HALF_DOWN);

            currentBudjet = BigDecimal.valueOf(0.0);
            setStatActions(getStatActions() + 1);
        } else {
            
        }

        return this;
    }

    public EvaluateMethodSignals sell(double price, int amount) {

        if (amount == -1) {//ALL-in
            currentBudjet = stockCount.multiply(BigDecimal.valueOf(price));
            currentBudjet = currentBudjet.subtract(this.brockerExpensePerAction(currentBudjet));

            if (getCurrentBestBudjet().compareTo(currentBudjet) < 0) {
                setCurrentBestBudjet(currentBudjet);
            }

            stockCount = BigDecimal.valueOf(0.0);
        }
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


}
