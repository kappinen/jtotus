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

/**
 * f(x) = { double[x]
 *          double[maxSize], if x > maxSize
 *         
 * @author Evgeni Kappinen
 */
public class TimeSeriesFunction {
    private String funcName = null;
    private double []data;

    /**
     * @return the funcName
     */
    public String getFuncName() {
        return funcName;
    }

    /**
     * @param funcName the funcName to set
     */
    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    /**
     * @return the data
     */
    public double[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(double[] data) {
        this.data = data;
    }

    public double get(int i) {
        if (i > data.length -1)
            return data[data.length-1];
        return data[i];
    }


    public int size() {
        return data.length - 1;
    }
}
