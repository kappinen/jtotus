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

package org.jtotus.methods.evaluators;

import java.util.ArrayList;

/**
 *
 * @author Evgeni Kappinen
 */
public class TimeSeriesCondition {
    private int NULL = 0;
    public int CROSSING = 0;
    private ArrayList<TimeSeriesFunction> funcList = null;

    private int a = 0;
    private int op = 0;
    private int b = 0;


    private boolean previous = false;
    private boolean and = false;

    
    public TimeSeriesCondition() {
        funcList = new ArrayList<TimeSeriesFunction> ();
        
    }

    public TimeSeriesFunction declareFunc(String name, double data[]) {
        TimeSeriesFunction series = new TimeSeriesFunction();
        series.setFuncName(name);
        series.setData(data);
        funcList.add(series);

        return series;
    }

    public boolean crossing(){
        TimeSeriesFunction aFunc = funcList.get(0);
        TimeSeriesFunction bFunc = funcList.get(1);


        if (((aFunc.get(getA()-1) - bFunc.get(getB()-1)) >= 0) &&
            ((aFunc.get(getA()) - bFunc.get(getB())) <= 0)) {
            return true;
        }

        if (((aFunc.get(getA()-1) - bFunc.get(getB()-1)) <= 0) &&
            ((aFunc.get(getA()) - bFunc.get(getB())) >= 0)) {
            return true;
        }
        
        return false;
    }


    public boolean isTrue(){
        boolean result = false;

        switch (getOp()) {
            case 0:
                result = crossing();
                break;
        }

        if (and) {
            result = previous && result;
            and = false;
        }

        return result;
    }

    /**
     * @return the a
     */
    public int getA() {
        return a;
    }

    /**
     * @param a the a to set
     */
    public TimeSeriesCondition setA(int a) {
        this.a = a;
        return this;
    }

    /**
     * @return the op
     */
    public int getOp() {
        return op;
    }

    /**
     * @param op the op to set
     */
    public TimeSeriesCondition setOp(int op) {
        this.op = op;
        return this;
    }

    /**
     * @return the b
     */
    public int getB() {
        return b;
    }

    /**
     * @param b the b to set
     */
    public TimeSeriesCondition setB(int b) {
        this.b = b;
        return this;
    }

    public TimeSeriesCondition crosses() {
        this.op = this.CROSSING;
        return this;
    }

    public TimeSeriesCondition and() {
        this.and = true;
        previous = this.isTrue();

        return this;
    }

    public void reset() {
        funcList.clear();
        a = op = b;
    }

}
