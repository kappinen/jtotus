/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import jtotus.common.StockType;
import jtotus.config.MethodConfig;
import jtotus.methods.PeriodClosingPrice;
import jtotus.threads.VoterThread;

/**
 *
 * @author house
 */
public class PotentialWithIn implements VoterThread {
    private HashMap<String,Integer> voteCounter = null;
    private ArrayList<PeriodClosingPrice> periodList = null;

    public String getMethName() {
        return this.getClass().getName();
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
       MethodConfig listOfTasks = new MethodConfig();

       voteCounter = new HashMap<String, Integer>();
       periodList = new ArrayList<PeriodClosingPrice>();
       
       Iterator<String> iter = listOfTasks.iterator();
       //Build period history for stock
       while(iter.hasNext()) {
           StockType stock = new StockType(iter.next());
            periodList.add(new PeriodClosingPrice(stock));
       }

       System.out.printf("Fin:%d\n", 1);
       //  Find out which Stock has most potentials
       Iterator<PeriodClosingPrice> iterPer = periodList.iterator();
       ArrayList <BigDecimal> listOfPrices = new ArrayList<BigDecimal>();
       while(iterPer.hasNext()) {
           
           PeriodClosingPrice stockPeriod = iterPer.next();
           BigDecimal value = stockPeriod.getPotential();
           listOfPrices.add(value);
           System.out.printf("Done for:%s : %f\n",stockPeriod.getStockName(),value.floatValue());
        }

        //Sort list
       Collections.sort(listOfPrices);


       iterPer = periodList.iterator();
//       System.out.printf("Fin:%d\n", listOfPrices.size());
       while(iterPer.hasNext()) {
           
           PeriodClosingPrice stockPer = iterPer.next();
           BigDecimal max = stockPer.getPotential();
           for(int i=0;i<listOfPrices.size()-1;i++) {
//           System.out.printf("Value:%f == %f\n", max.floatValue(), listOfPrices.get(i).floatValue());
               if (max.doubleValue() == listOfPrices.get(i).doubleValue()) {
                   Integer votes = voteCounter.get(stockPer.getStockName());
                   if (votes == null) {
                       voteCounter.put(stockPer.getStockName(), Integer.valueOf(i*i));
//                       System.out.printf("Stock:%s max:%f votes:%d\n",
//                               stockPer.getStockName(), max.floatValue(), i);
                   }
                   else{
                       votes+=Integer.valueOf(i*i);
//                        System.out.printf("Stock2:%s max:%f votes:%d\n",
//                               stockPer.getStockName(), max.floatValue(), i);
                   }
                   break;
               }
           }
        }

        dumpVotes(voteCounter);
       
           //Find out potentials and sort them

//
//
//           BigDecimal near_buttom = period.getMinValue();
//           near_buttom=near_buttom.subtract(current);
//
//
//           BigDecimal procents = period.getMaxValue();
//
////           System.out.printf("Value2:"+procents+" Value2:"+current+"\n");
//           procents = procents.divide(current, MathContext.DECIMAL32);
//
////           System.out.printf("Value3:"+procents+" Value3:"+current+"\n");
//           procents = procents.subtract(BigDecimal.valueOf(1));
////           System.out.printf("Value4:"+procents+" Value4:"+current+"\n");
//           procents = procents.multiply(BigDecimal.valueOf(100));
//           //BigDecimal procents = ((period.getMaxValue()/current)-1)*100;
//
//           System.out.printf("Current Price for %s: %f potential:%.4f (%f)\n",
//                             stock.getName(), current, potential.floatValue(), procents.floatValue());
//
//
       }









}
