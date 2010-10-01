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

package jtotus.methods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.math.BigDecimal;
import jtotus.common.DateIterator;
import jtotus.config.MethodConfig;
import jtotus.common.StockType;

/**
 *
 * @author house
 */
public class StatisticsFreqPeriod implements MethodEntry{
    private String stockName = null;
    private int maxPeriod = 20;
    private int total_days = 0;
    private int []posCurrent = new int[maxPeriod]; // Possitive current
    private int []negCurrent = new int[maxPeriod]; // Negative current
    private int []neuCurrent = new int[maxPeriod]; // Neutral current




    public StatisticsFreqPeriod() {
    }

    
    public StatisticsFreqPeriod(String tmpName) {
        stockName = tmpName;
        
    }

    public String getMethName() {
        return "StatisticsFreqPeriod";
    }

    private int normilize(double tmp) {

        if (tmp > 0)
            return(1);
        else if(tmp < 0)
            return(-1);
        
       return 0;
    }



public void run() {


    MethodConfig config = new MethodConfig();

    ArrayList<String> stockList = new ArrayList<String>();


    if (stockName == null) {//Stock Name is not provided, lets use config file
        String []tempList = config.fetchStockName();

        for (int i = 0; i < tempList.length;i++)
            stockList.add(tempList[i]);
        String []stockNames = config.fetchStockName();
    }
    else {
        stockList.add(stockName);
    }
    
    StatisticsForFreqPeriod(stockList,config);
    
    
}


private void StatisticsForFreqPeriod(ArrayList<String> stockList,
                                     MethodConfig config) {


    Iterator<String> list = stockList.iterator();

    while (list.hasNext()) {
        StockType stock = new StockType(list.next());

               
        BigDecimal previousDay = null;
        BigDecimal searchDady = null;
        SimpleDateFormat dayFormat = new SimpleDateFormat();

        int mainCurrent = 0;
        double localCurrent = 0;
        int strikes = 0;

        Date startDate = config.getStartTime();
        Date endDate = config.getEndTime();

        Iterator<Date> dateIter = new DateIterator(startDate, endDate);

        System.err.print(startDate + ":"+ endDate + "\n");

        while(dateIter.hasNext()) {

            if (previousDay == null) {
                dayFormat.format(dateIter.next());
                previousDay = stock.fetchClosingPrice(dayFormat);
                    if (previousDay == null) {
                    continue;
                    }
                total_days++;
            }

  

            while(dateIter.hasNext()){

                dayFormat.format(dateIter.next());
                searchDady = stock.fetchClosingPrice(dayFormat);
                if (searchDady != null) {
                    total_days++;
                    break;
                }
            }
            
            if (searchDady == null) {
                break;
            }


              
             localCurrent = searchDady.doubleValue() - previousDay.doubleValue();
//             System.err.printf("The previous;%f and search;%f\n",
//                      previousDay, searchDady);

             if (mainCurrent != normilize(localCurrent)) {
                 if (mainCurrent > 0) {
                     posCurrent[strikes]++;
                 }
                 else if (mainCurrent<0) {
                     negCurrent[strikes]++;
                 }
                 else {
                     neuCurrent[strikes]++;
                     }

                    mainCurrent = normilize(localCurrent);
                    strikes = 1;
                }
                else {
                    if (strikes<maxPeriod)
                        strikes++;
                }
             
             previousDay = searchDady;

        }


        

            if (mainCurrent > 0) {
               posCurrent[strikes]++;
            }
            else if (mainCurrent<0) {
                negCurrent[strikes]++;
            }
            else {
                neuCurrent[strikes]++;
            }
    }
        //results
        printResultsToOur();
}
    
    
    
    
private float pros(int share, float total) {
    return ((float)share / total)*100;
}



private void printResultsToOur()
{
    float total_pos = 0.0f;
    float total_neg = 0.0f;
    float total_neu = 0.0f;
    int rowSum = 0;

    for (int i = 1; i<maxPeriod; i++) {
        total_pos += posCurrent[i];
        total_neg += negCurrent[i];
        total_neu += neuCurrent[i];
    }


    
    for (int i = 1; i<maxPeriod; i++) {
       if (posCurrent[i] != 0 || negCurrent[i] != 0 || neuCurrent[i] != 0)
        {
         rowSum = posCurrent[i] + negCurrent[i] + neuCurrent[i];
         System.out.printf("Raises: %d (%.2f) [%.2f] |"
                 + " Drops: %d (%.2f) [%.2f] |"
                 + " Still: %d (%.2f) [%.2f] -> %d days in a row\n",
                 posCurrent[i], pros(posCurrent[i],total_pos), pros(posCurrent[i],(float)rowSum),
                 negCurrent[i], pros(negCurrent[i],total_neg), pros(negCurrent[i],(float)rowSum),
                 neuCurrent[i], pros(neuCurrent[i],total_neu), pros(neuCurrent[i],(float)rowSum),
                 i);
        }

    }
    System.out.printf("total days:%d \n", total_days);
}

    public boolean isCallable() {
        return false;
    }
    
}
