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
 *
 * http://tutorials.jenkov.com/java-collections/navigableset.html
 */

package jtotus.methods;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import jtotus.common.DateIterator;
import jtotus.common.Helper;
import jtotus.common.StockType;

/**
 *
 * @author Evgeni Kappinen
 */
public class PeriodClosingPrice {
    private StockType stock = null;
    private ArrayList<BigDecimal> priceList = null;
    private ArrayList<BigDecimal> sortedList = null;
    private Date endingDate = null;
    private boolean initDone = false;
    private Helper help = Helper.getInstance();

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
        System.out.printf("Current:%f\n",current.doubleValue());
        BigDecimal max = this.getMaxValue();
        System.out.printf("Current:%f max:%f\n",current.doubleValue(),max.doubleValue() );
        BigDecimal pot = max.subtract(current).abs();
        BigDecimal ret = pot.divide(current,MathContext.DECIMAL64).multiply(BigDecimal.valueOf(100.00));
        help.debug("PeriodClosingPrice",
                "Stock: %s ret:%f - %f =ret:%f\n",
                stock.getName(),max.floatValue(),current.floatValue(),ret.floatValue());
        return ret;
    }


    public BigDecimal getLowPotential(){
        initList();
       
        BigDecimal current = stock.fetchCurrentClosingPrice();
        BigDecimal min = getMinValue();
        BigDecimal lowPot = min.subtract(current).abs();
        BigDecimal ret = lowPot.divide(current,MathContext.DECIMAL64).multiply(BigDecimal.valueOf(100.00));
        help.debug("PeriodClosingPrice",
                "Stock: %s ret:%f - %f =ret:%f\n",
                stock.getName(),min.floatValue(),current.floatValue(),ret.floatValue());
        return ret;
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
        if (sortedList != null && !sortedList.isEmpty()) {
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
