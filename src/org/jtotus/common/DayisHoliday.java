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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evgeni Kappinen
 */
public class DayisHoliday {
    private  ArrayList<Calendar> holidays = new ArrayList<Calendar>();

    public DayisHoliday() {
        DateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");

        try {//Finnish Holidays
            String[] holidayList = {
                                    "01.01.2010",
                                    "06.01.2010",
                                    "02.04.2010",
                                    "05.04.2010",
                                    "13.05.2010",
                                    "06.12.2010",
                                    "25.06.2010",
                                    //2009 Year
                                    "10.04.2009",
                                    "13.04.2009",
                                    "01.05.2009",
                                    "21.05.2009",
                                    "24.12.2009",
                                    "25.12.2009",
                                    "31.12.2009"
                                   };


            for (int i = 0; i < holidayList.length; i++) {
                Date dateTmp = dayFormat.parse(holidayList[i]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateTmp);
                holidays.add(cal);
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(DayisHoliday.class.getName()).log(Level.SEVERE, null, ex);
        }




    }

     public boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public boolean isHoliday(Calendar date) {
        Iterator <Calendar>dateIter = holidays.iterator();
        while(dateIter.hasNext()) {
            Calendar holidayDay = dateIter.next();

            if(this.isSameDay(date, holidayDay)) {
               //  System.out.printf("MATCH:"+date+" "+holidayDay.getTime()+"\n");
                return true;
            }
        }
        return false;
    }

    

}
