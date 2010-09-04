/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import jtotus.common.Helper;
import jtotus.common.StockType;

/**
 *
 * @author house
 */
public class AutoUpdateStocks implements Runnable {
    private String stockName = null;
    private Helper help = Helper.getInstance();
    private int maxSearch = 30; //Maximum search of moth
    private LocalJavaDB javadb = null;

    public AutoUpdateStocks(String tempName) {
        stockName = tempName;

    }
    


    private int updateClosingPrice(StockType stock, LocalJavaDB javadb){
       int counter = 0;

       final int failureLimit = -8;
       final int foundLimit = 8;
        for (int i = 0;(failureLimit < i) && (i < foundLimit) ;i++) {
            if (javadb.fetchClosingPrice(stockName,
                    help.dateReduction(help.getTimeNow(), i)) != null){
                // Found in database
                counter++;
                continue;
            }
            else {
                if (stock.fetchClosingPrice(
                        help.dateReduction(help.getTimeNow(), i)) != null){
                    // Found somewhere in resources..
                    // Database should be updated already.
                    counter = 0;
                    continue;
                }
                else {
                    // Not found int local database
                    //nor in other resources.
                    // Could be holiday or data simply is not available
                    counter--;
                }
            }
            


        }

       
       return 1;
    }



    
    public void run() {

           javadb = new LocalJavaDB();
            if (stockName == null && javadb.initialize() != 0) {
                System.err.printf("Error autoupdator failure.\n");
                return;
            }

            StockType stock = new StockType(stockName);


            updateClosingPrice(stock,javadb);
            return;
    }
    

}
