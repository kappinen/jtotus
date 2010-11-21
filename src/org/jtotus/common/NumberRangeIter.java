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

import java.util.Iterator;

/**
 *
 * @author Evgeni Kappinen
 */
public class NumberRangeIter<T extends Number> implements InterfaceStateIterator {

    private String paramName = null;
    private Double step = null;
    private Double start = null;
    private Double end = null;
    private Double current = null;

    public NumberRangeIter(String name) {
        paramName = name;
    }

    public NumberRangeIter(String name, T starts, T ends) {
        current = new Double(starts.doubleValue());
        start = new Double(starts.doubleValue());
        end = new Double(ends.doubleValue());
        paramName = name;

    }

    public void setRange(T starts, T ends, T stepSize) {
        current = starts.doubleValue();
        step = stepSize.doubleValue();
        start = starts.doubleValue();
        end = ends.doubleValue();
    }

    public int setRange(String range) {

        String startString = range.substring(range.lastIndexOf("[") + 1,
                range.lastIndexOf("-"));

        String endString = range.substring(range.lastIndexOf("-") + 1,
                range.lastIndexOf("]"));

        String stepString = range.substring(range.lastIndexOf("{") + 1,
                range.lastIndexOf("}"));
        if (stepString == null) {
            stepString = "1";
        }

        start = new Double(startString);
        current = new Double(startString);
        end = new Double(endString);
        step = new Double(stepString);

        return 1;
    }

    public void setName(String name) {
        paramName = name;
    }

    public String getName() {
        return paramName;
    }

    public void reset() {
        current = start.doubleValue();
    }

    public Iterator<T> iterator() {
        this.reset();
        return this;
    }

    public boolean hasNext() {

        //System.out.printf("end:%f current:%f step%f\n", end.floatValue(), current.floatValue(), step.floatValue());
        if (end.doubleValue() >= (current.doubleValue() + step.doubleValue())) {
            return true;
        }

        return false;
    }

    public Double next() {
        if (end.doubleValue() >= (current + step.doubleValue())) {
            current += step.doubleValue();
        }
        return current;
    }

    public Double getCurrent() {
        return current;
    }

    public void remove() {
        //FIXME:
    }
}
