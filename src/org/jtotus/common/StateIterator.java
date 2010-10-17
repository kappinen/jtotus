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
package org.jtotus.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evgeni Kappinen
 */
public class StateIterator {

    public final static int DATES_CONSUMED = 0;
    public final static int END_STATE = 1;
    public final static int COUNTINUE_STATE = 2;
    public boolean needs_reset = false;
    /*Represents Paramater for a given value*/
    private ArrayList<NumberRangeIter<Double>> numberParameter = null;
    private DateIterator dateRange = null;

    public StateIterator() {
        numberParameter = new ArrayList<NumberRangeIter<Double>>();
    }

    //Syntax Type[RangeStart-RangeEnd]{scale}
    public StateIterator addParam(String name, String rangeAndType) {
        String type = null;

        String rangeType = rangeAndType.replace(" ", "");

        if (rangeType.lastIndexOf("[") != -1) {
            type = rangeType.substring(0, rangeType.lastIndexOf("["));
        } else {
            type = "Double"; //Default type for paramater
        }

        if (type.compareTo("int") == 0
                || type.compareTo("Integer") == 0
                || type.compareTo("Float") == 0
                || type.compareTo("Double") == 0) {
            NumberRangeIter<Double> val = new NumberRangeIter<Double>(name);
            val.setRange(rangeAndType);
            numberParameter.add(val);
        } else if (type.compareTo("Date") == 0) {
            if (dateRange != null) {
                System.err.printf("Warning: date range is set for state iterator\n");
            }

            DateFormat startingDate = new SimpleDateFormat("dd.MM.yyyy");
            DateFormat endingDate = new SimpleDateFormat("dd.MM.yyyy");

            Date startDate = null;
            Date endDate = null;

            try {
                String range = rangeType.substring(rangeType.lastIndexOf("[") + 1,
                        rangeType.lastIndexOf("]"));
                String dateSplit[] = range.split("-");
                System.out.printf("String date:%s ending date:%s\n", dateSplit[0], dateSplit[1]);
                startDate = startingDate.parse(dateSplit[0]);
                endDate = endingDate.parse(dateSplit[1]);
            } catch (ParseException ex) {
                Logger.getLogger(StateIterator.class.getName()).log(Level.SEVERE, null, ex);
            }

            dateRange = new DateIterator(startDate, endDate);

            //FIXME:check return value


            String step = rangeType.substring(rangeType.lastIndexOf("{") + 1,
                    rangeType.lastIndexOf("}"));
            Integer dateStep = new Integer(step);
            dateRange.setStep(dateStep.intValue());

        } else {
            System.err.printf("Error: Unknown type %s for %s\n", type, name);
        }

        return this;
    }

    public void nextState() {


        if (dateRange != null && !dateRange.hasNext()) {
            dateRange.reset();
        }

        //go to upper parameters
        int last_index = numberParameter.size() - 1;
        for (; last_index >= 0; last_index--) {
            NumberRangeIter<Double> iter = numberParameter.get(last_index);
            if (!iter.hasNext()) {
                if (last_index == 0) {
                    return;
                }
                if (!needs_reset) {
                    iter.reset();
                }

                if (dateRange == null) {
                    continue;
                }

            } else {
                iter.next();
                break;
            }

            return;
        }

        if (dateRange != null) {
            dateRange.next();
        }

        return;
    }

    public int hasNext() {

        //go to upper parameters
        int last_index = numberParameter.size() - 1;
        for (; last_index >= 0; last_index--) {
            NumberRangeIter<Double> iter = numberParameter.get(last_index);
            if (!iter.hasNext()) {
                if (last_index == 0) {
                    if (needs_reset) {
                        return this.END_STATE;
                    }
                    needs_reset = true;
                    break;
                }
            } else {
                break;
            }

            if (dateRange != null && !dateRange.hasNext()) {
                return this.DATES_CONSUMED;
            }

        }

        return this.COUNTINUE_STATE;

    }

    //TODO:to templete
    public int nextInt(String paramName) {
        return this.nextDouble(paramName).intValue();
    }

    public Integer nextInteger(String paramName) {
        Double retDouble = this.nextDouble(paramName);
        Integer nextInt = new Integer(retDouble.intValue());
        return nextInt;
    }

    public Double nextDouble(String paramName) {
        Iterator<NumberRangeIter<Double>> numIter = numberParameter.listIterator();
        while (numIter.hasNext()) {
            NumberRangeIter<Double> val = numIter.next();
            //System.out.printf("Parameters in array:%s\n", val.getName());
            if (paramName.compareTo(val.getName()) == 0) {
                return val.getCurrent();
            }
        }

        System.err.printf("Parameter not found:%s\n", paramName);
        return new Double("0");
    }

    public Date nextDate() {
        return dateRange.getCurrent();
    }
}
