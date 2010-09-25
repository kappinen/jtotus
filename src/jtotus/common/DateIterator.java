/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author house
 */
public class DateIterator implements Iterator<Date>, Iterable<Date>
{

    private Calendar end = Calendar.getInstance();
    private Calendar current = Calendar.getInstance();

    public DateIterator(Date tmpStart, Date tmpEnd)
    {
        end.setTime(tmpEnd);
        end.add(Calendar.DATE, -1);
        current.setTime(tmpStart);
        current.add(Calendar.DATE, -1);
    }

    public DateIterator(Date start)
    {
        end.setTime(Calendar.getInstance().getTime());
        end.add(Calendar.DATE, -1);
        current.setTime(start);
        current.add(Calendar.DATE, -1);
    }

    public boolean hasNext()
    {
        return !current.after(end);
    }


    public Date next()
    {
        current.add(Calendar.DATE, 1);
        return current.getTime();
    }

    public void remove()
    {
        throw new UnsupportedOperationException(
           "Cannot remove");
   }

   public Iterator<Date> iterator()
    {
        return this;
   }
}

