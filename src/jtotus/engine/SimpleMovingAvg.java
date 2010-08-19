/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

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
    private String methodName = "SimpleMovinAvg";
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

        //FIXME:ensure that period was fetched 
        String []stocks = config.StockNames;
        for (int i=stocks.length-1;i>=0;i--){
            avr = 0.0f; count = 0;

            for(int y=0;y<=(config.day_period-1);y++){
               tmp = dispatch.fetchClosingPrice(stocks[i],
                        help.dateReduction(help.getTimeNow(), y));
               if (tmp != 0.0f){
                   avr +=tmp;
                    count++;
               }

           }
           avr /= count;
           help.debug(methodName, "%s:%.2f\n", stocks[i], avr.floatValue());
       }
    
        
    }


    
}
