/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.methods;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import jtotus.common.DateIterator;
import jtotus.common.StockType;

/**
 *
 * @author house
 */
public class PeriodClosingPrice {
    private StockType stock = null;
    private ArrayList<Float> priceList = null;
    private Date endingDate = null;
    private boolean initDone = false;
    public int period = 90;
    
    public PeriodClosingPrice(StockType stockTmp) {
        stock = stockTmp;
        priceList = new ArrayList<Float>();
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

        priceList = new ArrayList<Float>();
        Float closingPrice = null;
        while(iter.hasNext()) {
            closingPrice = stock.fetchClosingPrice(iter.next());
            if (closingPrice != null) {
               priceList.add(closingPrice);
            }
        }

        Collections.sort(priceList);
        initDone = true;
    }

    public float getMaxValue(){
        initList();
        if (priceList != null && priceList.size() != 0) {
            Float ret = priceList.get(priceList.size()-1);
            return ret.floatValue();
        }

        return 0.0f;
    }
    
    public float getMinValue() {
        initList();
        if (priceList != null) {
            return priceList.get(0).floatValue();
        }
        return 0.0f;
    }
    
}
