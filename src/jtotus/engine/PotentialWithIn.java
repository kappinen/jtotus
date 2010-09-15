/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.util.ArrayList;
import java.util.Iterator;
import jtotus.common.StockType;
import jtotus.config.MethodConfig;
import jtotus.methods.PeriodClosingPrice;
import jtotus.threads.VoterThread;

/**
 *
 * @author house
 */
public class PotentialWithIn implements VoterThread {

    public String getMethName() {
        return this.getClass().getName();
    }

    


    public void run() {
       MethodConfig listOfTasks = new MethodConfig();

       Iterator<String> iter = listOfTasks.iterator();
       PeriodClosingPrice period = null;
       while(iter.hasNext()) {
           StockType stock = new StockType(iter.next());
           period = new PeriodClosingPrice(stock);

           float current = stock.fetchCurrentClosingPrice().floatValue();
           float potential = period.getMaxValue() - current;

           Float procents = ((period.getMaxValue()/current)-1)*100;
           System.out.printf("Current Price for %s: %f potential:%f (%f)\n",
                             stock.getName(), current, potential, procents);

           

       }

    }








}
