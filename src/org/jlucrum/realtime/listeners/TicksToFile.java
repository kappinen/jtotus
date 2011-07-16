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

package org.jlucrum.realtime.listeners;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlucrum.realtime.eventtypes.StockTick;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Evgeni Kappinen
 */
public class TicksToFile implements UpdateListener{
    private final String fileWithTicks = "TicksToFile.txt";
    private PrintWriter writer = null;
    private boolean headerWritten = false;


    private File fileName() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat todayFormat = new SimpleDateFormat("dd_MM_yyyy");

        String format = todayFormat.format(today.getTime());
        File file = new File(format+"_"+fileWithTicks);
        if (file.exists()) {
            for (int i = 0; i <= 1000; i++) {
                file = new File(format+"_"+fileWithTicks+"."+i);
                if (!file.exists()) {
                    break;
                }
                if (i==1000) {
                    System.err.printf("Failure to write ticks to file\n");
                    return null;
                }
            }
        }

        System.out.printf("Writting ticks to file:%s\n", file.getPath());
        return file;
    }
    public PrintWriter getWriter() {

        if (writer == null) {
            try {
                File file = fileName();
                writer = new PrintWriter(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TicksToFile.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return writer;
    }


    public void update(EventBean[] ebs, EventBean[] ebs1) {
         StockTick tick = (StockTick) ebs[0].getUnderlying();
         if (!headerWritten) {
             getWriter().write(tick.getHeader()+"\n");
             headerWritten = true;
         }
         getWriter().write(tick.toString()+"\n");
         getWriter().flush();
    }


}
