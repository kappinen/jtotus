/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import jtotus.common.DateIterator;
import jtotus.common.StockType;
import jtotus.threads.VoterThread;

/**
 *
 * @author house
 */
public class StatisticsFreqPeriod implements VoterThread{
    private String stockName = null;
    private int maxPeriod = 20;
    private int total_days = 0;
    private int []posCurrent = new int[maxPeriod]; // Possitive current
    private int []negCurrent = new int[maxPeriod]; // Negative current
    private int []neuCurrent = new int[maxPeriod]; // Neutral current


    
    public StatisticsFreqPeriod(String tmpName) {
        stockName = tmpName;
    }

    public String getMethName() {
        return this.getClass().getName();
    }

    private int normilize(Float tmp) {
        Float result = 0.0f;
        
        if (tmp > 0)
            result = tmp / tmp;
        else if(tmp <0)
            result = tmp / (-1 * tmp);



        //System.err.printf("Normilize returns;%d\n", result.intValue());

        return result.intValue();

    }



    public void run() {
        if (stockName == null) {
            return;
        }

        StockType stock = new StockType(stockName);

        

        
        Float previousDay = null;
        Float searchDady = null;
        SimpleDateFormat dayFormat = new SimpleDateFormat();

        int mainCurrent = 0;
        Float localCurrent = 0.0f;
        int strikes = 0;

        Calendar calen = Calendar.getInstance();
        //FIXME:get values from config or elsewere!s
        calen.set(2005, 8 -1, 30);

        Iterator<Date> dateIter = new DateIterator(calen.getTime());

        System.err.print(calen.getTime());

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


              
             localCurrent = searchDady - previousDay;
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
    
}
