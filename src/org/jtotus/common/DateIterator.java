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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author Evgeni Kappinen
 */
public class DateIterator implements Iterator<Date>, Iterable<Date> {

    private int step = 1;
    private Calendar end = Calendar.getInstance();
    private Calendar start = Calendar.getInstance();
    private Calendar current = Calendar.getInstance();

    // Starts with past date(start) and going towards ending date
    public DateIterator(Date tmpStart, Date tmpEnd) {
        end.setTime(tmpEnd);

        start.setTime(tmpStart);
        start.add(Calendar.DATE, -1);

        if (!end.after(start)) {
            System.err.printf("Warning startin date is afte ending date! Reversing dates("
                    + start.getTime() + ":" + end.getTime() + "\n");
            Calendar tmp = Calendar.getInstance();

            tmp.setTime(start.getTime());
            start.setTime(end.getTime());
            end.setTime(tmp.getTime());
            System.err.printf("New time startin date is afte ending date! Reversing dates("
                    + start.getTime() + ":" + end.getTime() + "\n");
        }

        current.setTime(start.getTime());
    }

    // Starts with past date(start) and going towards ending date
    public DateIterator(Calendar tmpStart, Calendar tmpEnd) {
        end = (Calendar) tmpEnd.clone();

        start = (Calendar) tmpStart.clone();
        start.add(Calendar.DATE, -1);

        if (!end.after(start)) {
            System.err.printf("Warning startin date is afte ending date! Reversing dates("
                    + start.getTime() + ":" + end.getTime() + "\n");
            Calendar tmp = Calendar.getInstance();

            tmp.setTime(start.getTime());
            start.setTime(end.getTime());
            end.setTime(tmp.getTime());
            System.err.printf("New time startin date is afte ending date! Reversing dates("
                    + start.getTime() + ":" + end.getTime() + "\n");
        }

        current.setTime(start.getTime());
    }

    public DateIterator(Date start) {
        end.setTime(Calendar.getInstance().getTime());
        end.add(Calendar.DATE, -1);
        current.setTime(start);
    }

    public void setStep(int stepSize) {
        step = stepSize;
    }

    public boolean hasNext() {
        Calendar rangeCheck = Calendar.getInstance();

        rangeCheck.setTime(current.getTime());
        rangeCheck.add(Calendar.DATE, step);

        //Skip weekends
        while (DayisHoliday.isHoliday(rangeCheck)) {
            rangeCheck.add(Calendar.DATE, 1);
        }

        return !rangeCheck.after(end);
    }

    public Calendar nextInCalendar() {
        current.add(Calendar.DATE, step);

        //Skip weekends
        while (DayisHoliday.isHoliday(current)) {
            current.add(Calendar.DATE, 1);
        }

        return current;
    }

    public Date next() {
        return nextInCalendar().getTime();
    }

    public void remove() {
        throw new UnsupportedOperationException(
                "Cannot remove");
    }

    public void reset() {
        current.setTime(start.getTime());
    }

    public Date getCurrent() {
        return current.getTime();
    }

    public void move(int i) {
        for (int jump = 0; this.hasNext() && jump <= i; jump++) {
            this.next();
        }
    }

    public Iterator<Date> iterator() {
        current.setTime(start.getTime());
        return this;
    }
}
