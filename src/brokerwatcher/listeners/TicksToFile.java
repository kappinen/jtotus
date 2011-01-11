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

package brokerwatcher.listeners;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import brokerwatcher.eventtypes.StockTick;

/**
 *
 * @author Evgeni Kappinen
 */
public class TicksToFile implements UpdateListener{

    private PrintWriter writer = null;

    public PrintWriter getWriter() {

        if (writer == null) {
            try {
                File file = new File("TicksToFile.txt");
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
         getWriter().write(tick+"\n");
         getWriter().flush();
    }


}
