/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.methods;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import jtotus.common.DateIterator;
import jtotus.common.StockType;

/**
 *
 * @author house
 */
public class PeriodClosingPrice {
    private StockType stock = null;
    private ArrayList<BigDecimal> priceList = null;
    private ArrayList<BigDecimal> sortedList = null;
    private Date endingDate = null;
    private boolean initDone = false;

    //Helps to identify, which stock period
    private String stockName = null;
    public int period = 90;
    
    public PeriodClosingPrice(StockType stockTmp) {
        stock = stockTmp;
        priceList = new ArrayList<BigDecimal>();
    }

    public PeriodClosingPrice(StockType stockTmp, String stockTemp) {
        stock = stockTmp;
        stockName = stockTemp;
        priceList = new ArrayList<BigDecimal>();
    }

    public void setStockName(String stockTemp) {
        stockName = stockTemp;
    }

    public String getStockName() {
        if (stockName == null && stock != null) {
            stockName =  stock.getName();
        }
        return stockName;
    }

    public StockType getStockType() {
        return stock;
    }

    public BigDecimal getPotential() {
        initList();
        
//        BigDecimal current = stock.fetchClosingPrice(endingDate);
        //FIXME !! loop (ending date)--
        BigDecimal current = stock.fetchCurrentClosingPrice();
        BigDecimal potential = getMaxValue();
        return potential.subtract(current);

    }



    public void setStartDate(Date endDate) {
        endingDate = endDate;
        return;
    }
    
    private void initList(){
        //Get values
        if (initDone) {
            return;
        }
        
        if (endingDate == null) {
            endingDate = Calendar.getInstance().getTime();
        }

        Calendar endCal = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();

        endCal.setTime(endingDate);
        startCal.setTime(endingDate);
        startCal.add(Calendar.DATE, -1*period);

        DateIterator iter = new DateIterator(startCal.getTime(),
                                             endCal.getTime());

        priceList = new ArrayList<BigDecimal>();
        BigDecimal closingPrice = null;
        while(iter.hasNext()) {
            closingPrice = stock.fetchClosingPrice(iter.next());
            if (closingPrice != null) {
               priceList.add(closingPrice);
            }
        }

        sortedList = priceList;
        Collections.sort(sortedList);
        initDone = true;
    }

    public BigDecimal getMaxValue(){
        initList();
        if (sortedList != null && sortedList.size() != 0) {
            return  sortedList.get(sortedList.size()-1);
        }

        return null;
    }
    
    public BigDecimal getMinValue() {
        initList();
        if (sortedList != null) {
            return sortedList.get(0);
        }
        return null;
    }
    
}
