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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Evgeni Kappinen
 */
public class StockNames {
    private Map<String,String> stockMap = new HashMap<String,String>();
    private Iterator mapIter = null;

    public StockNames() {

        //Aliases
        //http://en.wikipedia.org/wiki/OMX_Helsinki_25

        stockMap.put("Cargotec Oyj","CGCBV.HSE");
        stockMap.put("Elisa Oyj","ELI1V.HSE");
        stockMap.put("Fortum Oyj", "FUM1V.HE");
        stockMap.put("Fortum Oyj", "FUM1V.HSE");
        stockMap.put("Kemira Oyj","KRA1V.HSE");
        stockMap.put("KONE Oyj","KNEBV.HSE");
        stockMap.put("Konecranes Oyj","KCR1V.HSE");
        stockMap.put("Metso Oyj","MEO1V.HSE");
        stockMap.put("Neste Oil","NES1V.HSE");
        stockMap.put("Nokia Oyj", "NOK1V.HSE");
        stockMap.put("Nokian Renkaat Oyj","NRE1V.HSE");
        stockMap.put("Nordea Bank AB","NDA1V.HSE");
        stockMap.put("Outokumpu Oyj","OUT1V.HSE");
        stockMap.put("Outotec Oyj","OTE1V.HSE");
        stockMap.put("Pohjola Bank A","POH1S.HSE");
        stockMap.put("Rautaruukki Oyj","RTRKS.HSE");
        stockMap.put("Pohjola Bank A","POH1S.HSE");
        stockMap.put("Sampo Oyj A","SAMAS.HSE");
        stockMap.put("Sanoma Oyj","SAA1V.HSE");
        stockMap.put("Stora Enso Oyj A","STEAV.HSE");
        stockMap.put("TeliaSonera AB","TLS1V.HSE");
        stockMap.put("Tieto Oyj","TIE1V.HSE");
        stockMap.put("UPM-Kymmene Oyj","UPM1V.HSE");
        stockMap.put("Wärtsilä Corporation","WRT1V.HSE");
        stockMap.put("YIT Oyj","YTY1V.HSE");

    }

    public Iterator iterator() {
       Set entries = stockMap.entrySet();
       mapIter = entries.iterator();
       return mapIter;
    }


    public String getHexName(String name) {
        return stockMap.get(name);
    }

    public String[]getNames() {
        return stockMap.keySet().toArray(new String [0]);
    }

    
}
