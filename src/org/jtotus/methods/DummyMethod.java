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


package org.jtotus.methods;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.espertech.esper.client.EventBean;
import org.jtotus.common.Helper;
import org.jtotus.common.MethodResults;

/**
 *
 * @author kappiev
 */
public class DummyMethod implements MethodEntry {

    private Helper help = Helper.getInstance();
    private static Random genNum = new Random();
    private String methodName = "DummyMethod";

    public void run() {
        try {
            Thread.sleep(1000+genNum.nextInt(3000));
            help.debug(1, "%s is running\n",methodName);
        } catch (InterruptedException ex) {
            Logger.getLogger(DummyMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMethName() {
        return methodName;
    }

    public boolean isCallable() {
       return false;
    }

    public MethodResults call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {
        throw new RuntimeException("This feature not yet supported.");
    }
}
