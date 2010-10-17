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
package org.jtotus.methods.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.jtotus.common.MethodResults;

/**
 *
 * @author Evgeni Kappinen
 */
public class Normalizer {

    protected NormilizerType normType = null;

    enum NormilizerType {

        SIMPLEMAX_WINS,
        SIMPLEMIN_WINS
    }

    private MethodResults simpleMinWins(MethodResults result) {

        HashMap<String, Double> resultMap = result.getResults();

        Double[] results = resultMap.values().toArray(new Double[0]);
        Arrays.sort(results);

        Set<Entry<String, Double>> set = resultMap.entrySet();
        

        for (int i = results.length - 1; i >= 0; i--) {
            Iterator<Entry<String, Double>> entryIter = set.iterator();
            
            while (entryIter.hasNext()) {
                Entry<String, Double> tmpEntry = entryIter.next();
                
                if (tmpEntry.getValue() == results[i]) {
                    //System.out.printf("Hope:%d  is  %d\n", tmpEntry.getValue().intValue(), results.length - 1 - i);
                    tmpEntry.setValue(Double.valueOf(results.length - 1 - i));
                }
            }
        }

        return result;
    }

    private MethodResults simpleMaxWins(MethodResults result) {

        HashMap<String, Double> resultMap = result.getResults();

        Double[] results = resultMap.values().toArray(new Double[0]);
        Arrays.sort(results);

        Set<Entry<String, Double>> set = resultMap.entrySet();

        for (int i = 0; i < results.length; i++) {
            Iterator<Entry<String, Double>> entryIter = set.iterator();
            while (entryIter.hasNext()) {
                Entry<String, Double> tmpEntry = entryIter.next();

                if (tmpEntry.getValue() == results[i]) {
                    tmpEntry.setValue(Double.valueOf(i));
                }
            }
        }

        return result;
    }

    public MethodResults perform(NormilizerType type, MethodResults result) {
        MethodResults results = null;


        switch (type) {
            case SIMPLEMAX_WINS:
                return simpleMaxWins(result);
            case SIMPLEMIN_WINS:
                return simpleMinWins(result);
        }

        return null;
    }

    public MethodResults perform(String type, MethodResults result) {
        NormilizerType typeNum = null;

        if (type.compareTo("SimpleMaxWins") == 0) {
            typeNum = NormilizerType.SIMPLEMAX_WINS;
        } else if (type.compareTo("SimpleMinWins") == 0) {
            typeNum = NormilizerType.SIMPLEMIN_WINS;
        }else {
            return null;
        }

        switch (typeNum) {
            case SIMPLEMAX_WINS:
                return simpleMaxWins(result);
            case SIMPLEMIN_WINS:
                return simpleMinWins(result);
        }

        return null;
    }
}
