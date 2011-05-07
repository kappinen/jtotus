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

package org.jtotus.methods;

import java.util.HashMap;
import java.util.Iterator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import com.espertech.esper.client.EventBean;
import org.jtotus.common.Helper;
import org.jtotus.common.MethodResults;
import org.jtotus.common.StockNames;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfPortfolio;

/**
 *
 * @author Evgeni Kappinen
 */
public class PotentialWithIn implements MethodEntry {
    private HashMap<String,Integer> voteCounter = null;
    private ArrayList<PeriodClosingPrice> periodList = null;
    private Helper help = Helper.getInstance();

    public String getMethName() {
        String tmp = this.getClass().getName();
        return tmp.substring(tmp.lastIndexOf(".")+1,tmp.length());
    }

    public void dumpVotes(HashMap<String,Integer> list){
        Set<Entry<String,Integer>> set = list.entrySet();
        Iterator <Entry<String,Integer>>entryIter = set.iterator();


        System.out.printf("[start>]-------------------\n");
        while(entryIter.hasNext()) {
            Entry<String,Integer> entry = entryIter.next();
            System.out.printf("StockNames:%s votes:%d\n",
                    entry.getKey(),entry.getValue());
        }
        System.out.printf("[>ends]-------------------\n");

    }

    public void run() {
        ConfPortfolio portfolioConfig = ConfPortfolio.getPortfolioConfig();
        voteCounter = new HashMap<String, Integer>();
        periodList = new ArrayList<PeriodClosingPrice>();


        String[] stockNames = portfolioConfig.inputListOfStocks;
        //Build period history for stock
        for (String stockName : stockNames) {
            StockType stock = new StockType(stockName);
            periodList.add(new PeriodClosingPrice(stock));
            help.debug(this.getClass().getName(),
                    "StockName for period:%s\n", stock.getStockName());
       }

       //  Find out which Stock has most potentials
       Iterator<PeriodClosingPrice> iterPer = periodList.iterator();
       ArrayList <BigDecimal> listOfPrices = new ArrayList<BigDecimal>();
       while(iterPer.hasNext()) {
           PeriodClosingPrice stockPeriod = iterPer.next();
           BigDecimal value = stockPeriod.getPotential();
           listOfPrices.add(value);
           help.debug(this.getClass().getName(),
                   "Assiging for:%s : %f\n",stockPeriod.getStockName(),value.doubleValue());
        }

        //Sort list
       Collections.sort(listOfPrices);


       //Potential 
       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {
           PeriodClosingPrice stockPer = iterPer.next();
           BigDecimal max = stockPer.getPotential();
           for(int i=0;i<listOfPrices.size();i++) {
           
               help.debug(this.getClass().getName(),
                   "Value for(%s):%f == %f\n",
                   stockPer.getStockName(),max.floatValue(), listOfPrices.get(i).floatValue());

               if (max.doubleValue() == listOfPrices.get(i).doubleValue()) {
                   Integer votes = voteCounter.get(stockPer.getStockName());
                   if (votes == null) {
                       voteCounter.put(stockPer.getStockName(), Integer.valueOf(i*i));
                       help.debug(this.getClass().getName(),
                               "Stock:%s max:%f votes:%d\n",
                               stockPer.getStockName(), max.floatValue(), i);
                   }
                   else{
                       votes+=Integer.valueOf(i*i);
                       voteCounter.put(stockPer.getStockName(), votes);
                       help.debug(this.getClass().getName(),
                               "Stock:%s max:%f votes:%d\n",
                               stockPer.getStockName(), max.floatValue(), i);
                   }
                   break;
               }
           }
        }

       //IF current price is the minimum of the period
       //substract its  position in the table

        listOfPrices.clear();
        //  Find out which Stock has most potentials
       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {

           PeriodClosingPrice stockPeriod = iterPer.next();
           BigDecimal value = stockPeriod.getLowPotential();
           listOfPrices.add(value);
           help.debug(this.getClass().getName(),
                   "Assiging for:%s : %f\n",stockPeriod.getStockName(), value.doubleValue());
        }

       Collections.sort(listOfPrices);

       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {

           PeriodClosingPrice stockPer = iterPer.next();
           BigDecimal min = stockPer.getLowPotential();
           
           help.debug(this.getClass().getName(),
                    "Minimim value:%f for %s\n",
                    min.floatValue(),stockPer.getStockName());

           for(int i=listOfPrices.size()-1;i>0;i--) {

               help.debug(this.getClass().getName(),
                   "Value for(%s):%f == %f\n", 
                   stockPer.getStockName(),min.floatValue(), listOfPrices.get(i).floatValue());
           
               if (min.doubleValue() == listOfPrices.get(i).doubleValue()) {
                   Integer votes = voteCounter.get(stockPer.getStockName());
                   if (votes == null) {
                       voteCounter.put(stockPer.getStockName(), Integer.valueOf(i*i));
                       System.out.printf("Stock:%s min:%f votes:%d\n",
                               stockPer.getStockName(), min.floatValue(), i);
                   }
                   else{
                       votes-=Integer.valueOf(i*i);
                       voteCounter.put(stockPer.getStockName(), votes);
                       System.out.printf("Stock:%s min:%f votes:%d\n",
                               stockPer.getStockName(), min.floatValue(), i);
                   }
                   break;
               }
           }
        }

       listOfPrices.clear();
       //  Find out which Stock has most potentials
       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {
           PeriodClosingPrice stockPeriod = iterPer.next();
           BigDecimal value = new BigDecimal(stockPeriod.raises);
           listOfPrices.add(value);
           help.debug("PotentialWithIn",
                   "Assiging for:%s : %f\n",stockPeriod.getStockName(), value.doubleValue());
        }

       Collections.sort(listOfPrices);

       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {
           PeriodClosingPrice stockPer = iterPer.next();
           BigDecimal max = new BigDecimal(stockPer.raises);
           for(int i=0;i<listOfPrices.size();i++) {

               help.debug("PotentialWithIn",
                   "Value for(%s):%f == %f\n",
                   stockPer.getStockName(),max.floatValue(), listOfPrices.get(i).floatValue());

               if (max.doubleValue() == listOfPrices.get(i).doubleValue()) {
                   Integer votes = voteCounter.get(stockPer.getStockName());
                   if (votes == null) {
                       voteCounter.put(stockPer.getStockName(), Integer.valueOf(i*i));
                       help.debug(this.getClass().getName(),
                               "Stock1:%s raises:%f votes:%d total dates:%d\n",
                               stockPer.getStockName(), max.floatValue(), i, stockPer.total_dates);
                   }
                   else{
                       votes+=Integer.valueOf(i*i);
                       voteCounter.put(stockPer.getStockName(), votes);
                       help.debug(this.getClass().getName(),
                               "Stock2:%s raises:%f votes:%d total dates:%d\n",
                               stockPer.getStockName(), max.floatValue(), i, stockPer.total_dates);
                   }
                   break;
               }
           }
        }

       listOfPrices.clear();
       //  Find out which Stock has most potentials
       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {
           PeriodClosingPrice stockPeriod = iterPer.next();
           Integer votes = voteCounter.get(stockPeriod.getStockName());
           BigDecimal value = new BigDecimal(votes.intValue());
           listOfPrices.add(value);
           help.debug("PotentialWithIn",
                   "Assiging for:%s : %f\n",stockPeriod.getStockName(), value.doubleValue());
        }

       Collections.sort(listOfPrices);

       iterPer = periodList.iterator();
       while(iterPer.hasNext()) {
           PeriodClosingPrice stockPer = iterPer.next();
           Integer votesOrig = voteCounter.get(stockPer.getStockName());
           BigDecimal max = new BigDecimal(votesOrig.intValue());
           for(int i=0;i<listOfPrices.size();i++) {

               help.debug("PotentialWithIn",
                   "Value for(%s):%f == %f\n",
                   stockPer.getStockName(),max.floatValue(), listOfPrices.get(i).floatValue());

               if (max.doubleValue() == listOfPrices.get(i).doubleValue()) {
                   Integer votes = voteCounter.get(stockPer.getStockName());
                   if (votes == null) {
                       voteCounter.put(stockPer.getStockName(), Integer.valueOf(i));
                       System.out.printf(
                               "Stock1:%s raises:%f votes:%d total dates:%d\n",
                               stockPer.getStockName(), max.floatValue(), i, stockPer.total_dates);
                   }
                   else{
                       votes=Integer.valueOf(i);
                       voteCounter.put(stockPer.getStockName(), votes);
                       System.out.printf(
                               "Stock2:%s raises:%f votes:%d total dates:%d\n",
                               stockPer.getStockName(), max.floatValue(), i, stockPer.total_dates);
                   }
                   break;
               }
            }
        }
        dumpVotes(voteCounter);
    }

    public MethodResults call() throws Exception {
        MethodResults results = new MethodResults();

        results.setMethodName(this.getMethName());

        this.run();

        for (Entry<String, Integer> entry : voteCounter.entrySet()) {
            System.out.printf("Results for %s:%s votes:%d\n",
                    this.getMethName(), entry.getKey(), entry.getValue());

            results.putResult(entry.getKey(), entry.getValue());
        }

        return results;
    }

    public boolean isCallable() {
        return true;
    }

    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {
        return;
    }
}
