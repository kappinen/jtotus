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

package jtotus.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
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


    private LinkedList <String>sequence = null;
    /*Represents Paramater for a given value*/
    private ArrayList <NumberRangeIter<Double>>numberParameter = null;

    private DateIterator dateRange = null;


    public StateIterator() {
        sequence = new LinkedList<String>();
        numberParameter = new ArrayList<NumberRangeIter<Double>>();
    }



    //Syntax Type[RangeStart-RangeEnd]{scale}
   public void addParam(String name, String rangeAndType) {

        String rangeType = rangeAndType.replace(" ", "");
        String type = rangeType.substring(0,rangeType.lastIndexOf("["));
        String range = rangeType.substring(rangeType.lastIndexOf("[")+1,
                                           rangeType.lastIndexOf("]"));
        
        System.out.printf("Name:%s type:%s range:%s\n",
                          name, type, range);

        sequence.add(name);
        
        if (type.compareTo("int") == 0||
            type.compareTo("Integer") == 0 ||
            type.compareTo("Float") == 0 ||
            type.compareTo("Double") == 0) {
            NumberRangeIter<Double> val = new NumberRangeIter<Double>(name);
            val.setRange(rangeAndType);
            numberParameter.add(val);
        } else if (type.compareTo("Date") == 0) {
            if(dateRange != null) {
                System.err.printf("Warning: date range is set for state iterator\n");
            }

            DateFormat startingDate = new SimpleDateFormat("dd.MM.yyyy");
            DateFormat endingDate = new SimpleDateFormat("dd.MM.yyyy");

            Date startDate=null;
            Date endDate=null;
            try {

                String dateSplit[] = range.split("-");
                System.out.printf("String date:%s ending date:%s\n", dateSplit[0], dateSplit[1]);
                startDate = startingDate.parse(dateSplit[0]);
                endDate = endingDate.parse(dateSplit[1]);
            } catch (ParseException ex) {
                Logger.getLogger(StateIterator.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            dateRange = new DateIterator(startDate ,endDate);

            //FIXME:check return value
            
            
            String step = rangeType.substring(rangeType.lastIndexOf("{")+1,
                                              rangeType.lastIndexOf("}"));
            Integer dateStep = new Integer(step);
            dateRange.setStep(dateStep.intValue());

        }
        else {
            System.err.printf("Error: Unknown type %s for %s\n", type, name);
        }
   }



   public int hasNext(){
       
       if (!dateRange.hasNext()){
            dateRange.reset();

            //go to upper parameters
           int last_index = numberParameter.size()-1;
           for (;!numberParameter.get(last_index).hasNext();last_index--) {
               if (0>=last_index) {
                   //Nothing to reset, states are consumed
                   return this.END_STATE;
               }
               numberParameter.get(last_index).reset();
           }

           return this.DATES_CONSUMED;
       }

      dateRange.next();
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
        Iterator <NumberRangeIter<Double>> numIter = numberParameter.iterator();
         while(numIter.hasNext()) {
             NumberRangeIter<Double> val = numIter.next();
             if (paramName.compareTo(val.getName()) == 0) {
                 if(val.hasNext()){
                    return val.getCurrent();
                 }
                 System.err.printf("BUG:should no happend\n");
                 return new Double("-1");
             }
         }

         System.err.printf("Parameter not found:%s\n", paramName);
         return new Double("0");
    }

    public Date nextDate() {
        return dateRange.getCurrent();
    }



}
