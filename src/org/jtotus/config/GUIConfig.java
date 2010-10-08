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
import org.jtotus.engine.Engine;
import org.jtotus.methods.MethodEntry;

/**
 *
 * @author kappiev
 */
public class GUIConfig {

    public final String []StockNames = { "Fortum Oyj",
                                         "Nokia Oyj",
                                         "UPM-Kymmene Oyj",
                                         "Metso Oyj",
                                         "Kemira Oyj",
                                         "Konecranes Oyj",
                                         "KONE Oyj",
                                         "Rautaruukki Oyj",
                                         "Sanoma Oyj",
                                         "Tieto Oyj",
                                         "Uponor Oyj",
                                         "Stora Enso Oyj A"
                                          };
    public final int day_period = 5;

    public String []fetchStockName() {
        return StockNames;
    }

    public LinkedList <MethodEntry> getSupportedMethodsList() {
        Engine engine = Engine.getInstance();
        return  engine.getMethods();
    }

}

