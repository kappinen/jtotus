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

package jtotus.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Evgeni Kappinen
 */
public class MethodResults {
    private String methodName = null;
    private HashMap<String,Double> results = null;

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

}
