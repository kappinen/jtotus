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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.TreeMap;
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
    private LinkedList <NumberRangeIter<Float>>FloatValues = null;
    private LinkedList <NumberRangeIter<Integer>>IntegerValues = null;
    private LinkedList <NumberRangeIter<Double>>DoubleValues = null;
    private LinkedList <NumberRangeIter<Integer>>intValues = null;
    private DateIterator dateRange = null;


    public StateIterator() {
        sequence = new LinkedList<String>();
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
        
        if (type.compareTo("int") == 0) {
            NumberRangeIter<Integer> val = new NumberRangeIter<Integer>(name);
            val.setRange(rangeAndType);
            intValues.add(val);
        }else if (type.compareTo("Integer") == 0) {
            NumberRangeIter<Integer> val = new NumberRangeIter<Integer>(name);
            val.setRange(rangeAndType);
            IntegerValues.add(val);
        }else if (type.compareTo("Float") == 0) {
            NumberRangeIter<Float> val = new NumberRangeIter<Float>(name);
            val.setRange(rangeAndType);
            FloatValues.add(val);
        }else if (type.compareTo("Double") == 0) {
            NumberRangeIter<Double> val = new NumberRangeIter<Double>(name);
            val.setRange(rangeAndType);
            DoubleValues.add(val);
        }
        else if (type.compareTo("Date") == 0) {
            if(dateRange != null) {
                System.err.printf("Warning: date range is set for state iterator\n");
            }

            DateFormat startingDate = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat endingDate = new SimpleDateFormat("dd-MM-yyyy");

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

            String step = rangeType.substring(rangeType.lastIndexOf("{")+1,
                                           rangeType.lastIndexOf("}"));
            //FIXME:check return value
            Integer dateStep = new Integer(step);
            dateRange.setStep(dateStep.intValue());

        }
        else {
            System.err.printf("Error: Unknown type %s for %s\n", type, name);
        }
   }



   public int hasNext(){
       Iterator<String> seqIter = sequence.iterator();
       
       if (!dateRange.hasNext()){
           
       }

       while(seqIter.hasNext()){
           
       }


       return this.END_STATE;
   }


   //TODO:to templete
    int nextInt(String paramName) {
    Iterator <NumberRangeIter<Integer>> intValueIter = intValues.iterator();
     while(intValueIter.hasNext()) {
         NumberRangeIter<Integer> val = intValueIter.next();
         if (paramName.compareTo(val.getName()) == 0) {
             if(val.hasNext()){
                return val.next().intValue();
             }
             System.err.printf("BUG:should no happend\n");
             return -1;
         }
     }
    
     System.err.printf("Parameter not found:%s\n", paramName);
     return 0;
    }

    Integer nextInteger(String paramName) {
        Iterator <NumberRangeIter<Integer>> IntegerValuesIter = IntegerValues.iterator();
         while(IntegerValuesIter.hasNext()) {
             NumberRangeIter<Integer> val = IntegerValuesIter.next();
             if (paramName.compareTo(val.getName()) == 0) {
                 if(val.hasNext()){
                    return new Integer(val.next().intValue());
                 }
                 System.err.printf("BUG:should no happend\n");

                 return new Integer("-1");
             }
         }
        
        System.err.printf("Parameter not found:%s\n", paramName);
        return new Integer("0");
    }

    Double nextDouble(String paramName) {
        Iterator <NumberRangeIter<Double>> DoubleValuesIter = DoubleValues.iterator();
         while(DoubleValuesIter.hasNext()) {
             NumberRangeIter<Double> val = DoubleValuesIter.next();
             if (paramName.compareTo(val.getName()) == 0) {
                 if(val.hasNext()){
                    return val.next();
                 }
                 System.err.printf("BUG:should no happend\n");

                 return new Double("-1");
             }
         }
        System.err.printf("Parameter not found:%s\n", paramName);
        return new Double("0");
    }





}
