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
*/

package org.jtotus.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Evgeni Kappinen
 */
public class MethodResults implements Iterator, Iterable{
    private String methodName = null;
    private HashMap<String,Double> results = null;
    private Iterator<Entry<String, Double>> iterator=null;
    private Double avrSuccessRate=null;
    

    public MethodResults(String nameOfMethod) {
        methodName = nameOfMethod;
        results = new HashMap<String, Double>();
    }
    
    public MethodResults() {
        results = new HashMap<String, Double>();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String MethodName) {
        methodName = MethodName;
    }

    
    public void putResult(String stockName, Double stockValue) {
        if (results != null) {
            results.put(stockName, stockValue);
        }
    }

    public void putResult(String stockName, Integer stockValue) {
        if (results != null) {
            results.put(stockName, Double.valueOf(stockValue.doubleValue()));
        }
    }

    public HashMap<String,Double>getResults() {
        return results;
    }


    public String getName(){
        String tmpName = this.getClass().getName();
        String trueName = tmpName.substring(tmpName.lastIndexOf(".")+1, tmpName.length());
        return trueName;
    }

    public void printToConsole(){
        Set<Entry<String,Double>> set = results.entrySet();
        Iterator <Entry<String,Double>>entryIter = set.iterator();
        while(entryIter.hasNext()) {
            Entry<String,Double> entry = entryIter.next();

            System.out.printf("%s : Stock:%s value:%.2f\n",
                    this.getName(),entry.getKey(),entry.getValue());
        }
    }


    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Object next() {
        return iterator.next();
    }

    public void remove() {
        iterator.remove();
    }

    public Iterator<Entry<String, Double>> iterator() {
        Set<Entry<String, Double>> entrySet = results.entrySet();
        iterator = entrySet.iterator();
        return iterator;
      
    }

    /**
     * @return the avrSuccessRate
     */
    public Double getAvrSuccessRate() {
        return avrSuccessRate;
    }

    /**
     * @param avrSuccessRate the avrSuccessRate to set
     */
    public void setAvrSuccessRate(Double avrSuccessRate) {
        this.avrSuccessRate = avrSuccessRate;
    }

}
