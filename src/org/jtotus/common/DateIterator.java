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

import java.util.Date;
import java.util.Iterator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author Evgeni Kappinen
 */
public class DateIterator implements Iterator<Date>, Iterable<Date> {

    private int step = 1;
    private DateTime end = new DateTime();
    private DateTime start = new DateTime();
    private DateTime current = new DateTime();

    // Starts with past date(start) and going towards ending date
    public DateIterator(Date tmpStart, Date tmpEnd) {
        end = new DateTime(tmpEnd);
        start = new DateTime(tmpStart).plusDays(1);

        if (end.compareTo(start) < 0) {
            System.err.printf("Warning startin date is afte ending date! Reversing dates("
                    + start.toDate() + ":" + end.toDate() + "\n");
            DateTime tmp = start.toDateTime();
            start = end;
            end = tmp;
            System.err.printf("New time startin date is afte ending date! Reversing dates("
                    + start.toDate() + ":" + end.toDate() + "\n");
        }

//        DateTimeFormatter formater = DateTimeFormat.forPattern("dd-MM-yyyy");
//        System.err.printf("Assigned start2:%s, end:%s\n", formater.print(start), formater.print(end));

        current = start.toDateTime();
    }

    // Starts with past date(start) and going towards ending date
    public DateIterator(DateTime tmpStart, DateTime tmpEnd) {
        end = tmpEnd.toDateTime();
        start = tmpStart.toDateTime().minusDays(1);

        if (end.compareTo(start) < 0) {
            System.err.printf("Warning startin date is afte ending date! Reversing dates("
                    + start.toDate() + ":" + end.toDate() + "\n");
            DateTime tmp = start.toDateTime();
            start = end;
            end = tmp;

            System.err.printf("New time startin date is afte ending date! Reversing dates("
                    + start.toDate() + ":" + end.toDate() + "\n");
        }

        current = start.toDateTime();
    }

    public DateIterator(Date start) {
        end = new DateTime().minusDays(1);
        current = new DateTime(start.getTime());
    }

    public void setStep(int stepSize) {
        step = stepSize;
    }

    public boolean hasNext() {
        DateTime rangeCheck = current.toDateTime().plusDays(step);

        //Skip weekends
        while (DayisHoliday.isHoliday(rangeCheck)) {
            rangeCheck = rangeCheck.plusDays(1);
        }

        //return rangeCheck.before(end);
        return rangeCheck.compareTo(end) < 0;
    }

    public DateTime nextInCalendar() {
        current = current.plusDays(step);

        //Skip weekends
        while (DayisHoliday.isHoliday(current)) {
            current = current.plusDays(1);
        }

        return current.toDateTime();
    }

    public Date next() {
        return nextInCalendar().toDate();
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove");
    }

    public void reset() {
        //current.setTime(start.getTime());
        current = start.toDateTime();
    }

    public Date getCurrent() {
        //return current.getTime();
        return current.toDate();
    }

    public void move(int i) {
        for (int jump = 0; this.hasNext() && jump <= i; jump++) {
            this.next();
        }
    }

    public Iterator<Date> iterator() {
        //current.setTime(start.getTime());
        current = start.toDateTime();
        return this;
    }
}
