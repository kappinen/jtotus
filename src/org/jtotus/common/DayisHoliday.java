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

import com.google.common.collect.HashMultiset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private int[]  days= {1012010, 6012010, 2042010, 5042010,
                          13052010, 6122010, 25062010, 10042009, 13042009,
                          1052009, 21052009, 19062009, 24122009, 25122009,
                          31122009, 24122010, 31122010, 6012011};

    public boolean isHoliday(Calendar date) {

        int toSearch =  date.get(Calendar.DATE)*1000000+(date.get(Calendar.MONTH)+1)*10000+date.get(Calendar.YEAR);
        //System.out.printf("To search:%d == %d:%d:%d\n", toSearch, date.get(Calendar.DATE), date.get(Calendar.MONTH)+1, date.get(Calendar.YEAR));
        for (int i = 0;i < days.length;i++) {
            if (days[i] == toSearch) {
                return true;
            }
        }
        return false;
    }


    public static void main(String []argv) {
        DayisHoliday holiday = new DayisHoliday();
        Calendar cal = Calendar.getInstance();
        //cal.set(2010, 12, 32);
        

        if (holiday.isHoliday(cal)) {
            System.out.printf("Is holiday!\n");
        }else {
            System.out.printf("Is not holiday!\n");
        }
    }
}
