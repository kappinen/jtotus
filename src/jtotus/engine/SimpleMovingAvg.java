/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import jtotus.common.Helper;
import jtotus.common.MethodConfig;
import jtotus.threads.Dispatcher;
import jtotus.threads.VoterThread;

/**
 *
 * @author kappiev
 */



public class SimpleMovingAvg implements VoterThread{
    private Dispatcher dispatch = null;
    private MethodConfig config = null;
    private String methodName = "Simple Moving Avg";
    private Helper help = null;
    
    public SimpleMovingAvg(Dispatcher tmp){
        dispatch = tmp;
        help = Helper.getInstance();
    }


    public String getMethName() {
       return methodName;
    }


    
    public void run() {

        config = dispatch.fetchConfig(methodName);

        analyzeFromNowToFrequency();
        return;
    }





    private void analyzeFromNowToFrequency()
    {
        Float avr = new Float(0.0f);
        Float tmp = null;
        int count=0;


        help.debug(4, "%s:%s\n", methodName, help.getTimeNow());

        String []stocks = config.StockNames;
       for (int i=stocks.length-1;i>=0;i--){

           for(int y=0;y<=config.day_frequncy-1;y++){

               tmp = dispatch.fetchPrice(stocks[i],
                        help.dateReduction(help.getTimeNow(), y));
               if (tmp != 0.0f)
               {
                   avr +=tmp;
                    count++;
               }
           }
           avr /= count;
       }

        help.debug(4, "%s:%.2f\n", methodName, avr.floatValue());
        
    }


    
}
