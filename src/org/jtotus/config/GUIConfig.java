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

package org.jtotus.config;

import java.util.LinkedList;
import org.jtotus.common.StockNames;
import org.jtotus.engine.Engine;
import org.jtotus.methods.MethodEntry;

/**
 *
 * @author Evgeni Kappinen
 */
public class GUIConfig {
    public StockNames names = null;
    public String []StockNames = null;
    public String gmailLogin = null;
    public String gmailPassword = null;
    public int day_period = 5;

    public GUIConfig(){
        if (names==null)
            names = new StockNames();
        
        StockNames = names.getNames();
    }

    public String []fetchStockName() {
        if (names==null)
            names = new StockNames();
        
        return names.getNames();
    }

    public LinkedList <MethodEntry> getSupportedMethodsList() {
        Engine engine = Engine.getInstance();
        return  engine.getMethods();
    }

}

