/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author kappiev
 */
public class StockName implements Iterator{
    private String stockName=null;
    private Map<String,String> stockMap = new HashMap<String,String>();
    Iterator mapIter = null;


    public StockName(String name) {
        stockName = name;

        
        stockMap.put("Fortum Oyj", "FUM1V.HE");
        stockMap.put("Fortum Oyj", "FUM1V.HSE");


        stockMap.put("Nokia Oyj", "NOK1V.HSE");

       Set entries = stockMap.entrySet();
       mapIter = entries.iterator();
        
    }

    public boolean hasNext() {
        return mapIter.hasNext();
    }

    public Object next() {
        Map.Entry entry = (Map.Entry)mapIter.next();
        return (String)entry.getValue();
        
    }

    public String nextValue() {
        Map.Entry entry = (Map.Entry)mapIter.next();
        return (String)entry.getValue();

    }

   public String nextKey() {
        Map.Entry entry = (Map.Entry)mapIter.next();
        return (String)entry.getKey();

    }

    public void remove() {
        mapIter.remove();
    }


    public String getHexName(String name) {
        return stockMap.get(name);
    }

    public String getHexName() {
        return stockMap.get(stockName);
    }
    
    
    
}
