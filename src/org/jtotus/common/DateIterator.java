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

/**
 *
 * @author Evgeni Kappinen
 */
public class DateIterator implements Iterator<Date>, Iterable<Date> {

    private int step = 1;
    private DateTime toDate;
    private DateTime fromDate;
    private DateTime current;

    // Starts with past date(fromDate) and going towards ending date
    public DateIterator(Date tmpStart, Date tmpEnd) {
        fromDate = new DateTime(tmpEnd).plusDays(1);
        toDate = new DateTime(tmpStart);

        if (toDate.compareTo(fromDate) < 0) {
            System.err.printf("Warning startin date is afte ending date! Reversing dates("
                    + fromDate.toDate() + ":" + toDate.toDate() + "\n");
            DateTime tmp = fromDate.toDateTime();
            fromDate = toDate;
            toDate = tmp;
            System.err.printf("New time startin date is afte ending date! Reversing dates("
                    + fromDate.toDate() + ":" + toDate.toDate() + "\n");
        }

//        DateTimeFormatter formater = DateTimeFormat.forPattern("dd-MM-yyyy");
//        System.err.printf("Assigned start2:%s, toDate:%s\n", formater.print(fromDate), formater.print(toDate));

        current = fromDate.toDateTime();
    }

    // Starts with past date(fromDate) and going towards ending date
    public DateIterator(DateTime startDate, DateTime endDate) {
        fromDate = startDate.toDateTime();
        toDate = endDate.toDateTime();

        if (!toDate.isAfter(fromDate)) {
            System.err.printf("Warning startin date is afte ending date! Reversing dates("
                    + fromDate.toDate() + ":" + toDate.toDate() + "\n");
            DateTime tmp = fromDate.toDateTime();
            fromDate = toDate;
            toDate = tmp;

            System.err.printf("New time startin date is afte ending date! Reversing dates("
                    + fromDate.toDate() + ":" + toDate.toDate() + "\n");
        }

        current = fromDate.toDateTime();
    }

    public DateIterator(Date start) {
        toDate = new DateTime();
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

        System.out.printf("nextInCalendar:%s \n", current.toString());
        return rangeCheck.isBefore(toDate);
    }

    public DateTime nextInCalendar() {
        current = current.plusDays(step);

        //Skip weekends
        while (DayisHoliday.isHoliday(current)) {
            current = current.plusDays(1);
        }

        System.out.printf("nextInCalendar:%s \n", current.toString());
        return current.toDateTime();
    }

    public Date next() {
        return nextInCalendar().toDate();
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove");
    }

    public void reset() {
        current = fromDate.toDateTime();
    }

    public Date getCurrent() {
        return current.toDate();
    }
    
    public DateTime getCurrentAsCalendar() {
        return current.toDateTime();
    }

    public void move(int i) {
        for (int jump = 0; this.hasNext() && jump <= i; jump++) {
            this.next();
        }
    }

    public Iterator<Date> iterator() {
        current = fromDate.toDateTime();
        return this;
    }
}
