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

/**
 *
 * @author Evgeni Kappinen
 */
public class StockNames {
    private Map<String, IndexMarketData> stockMap = new HashMap<String, IndexMarketData>();

    public StockNames() {

        //Aliases
        //http://en.wikipedia.org/wiki/OMX_Helsinki_25
                                                          // Weights are %
        stockMap.put("Cargotec Oyj", new IndexMarketData("CGCBV.HSE", 1.0));
        stockMap.put("Elisa Oyj", new IndexMarketData("ELI1V.HSE", 3.2));
        stockMap.put("Fortum Oyj", new IndexMarketData("FUM1V.HE", 9.8));
        stockMap.put("Fortum Oyj", new IndexMarketData("FUM1V.HSE", 9.8));
        stockMap.put("Kemira Oyj", new IndexMarketData("KRA1V.HSE", 1.7));
        stockMap.put("KONE Oyj", new IndexMarketData("KNEBV.HSE", 7.9));
        stockMap.put("Konecranes Oyj", new IndexMarketData("KCR1V.HSE", 2.0));
        stockMap.put("Metso Oyj", new IndexMarketData("MEO1V.HSE", 4.4));
        stockMap.put("Neste Oil", new IndexMarketData("NES1V.HSE", 2.1));
        stockMap.put("Nokia Oyj", new IndexMarketData("NOK1V.HSE", 9.8));
        stockMap.put("Nokian Renkaat Oyj", new IndexMarketData("NRE1V.HSE", 3.3));
        stockMap.put("Nordea Bank AB", new IndexMarketData("NDA1V.HSE", 4.4));
        stockMap.put("Outokumpu Oyj", new IndexMarketData("OUT1V.HSE", 2.4));
        stockMap.put("Outotec Oyj", new IndexMarketData("OTE1V.HSE", 1.6));
        stockMap.put("Pohjola Bank A", new IndexMarketData("POH1S.HSE", 2.2));
        stockMap.put("Rautaruukki Oyj", new IndexMarketData("RTRKS.HSE", 1.8));
        stockMap.put("Pohjola Bank A", new IndexMarketData("POH1S.HSE", 2.2));
        stockMap.put("Sampo Oyj A", new IndexMarketData("SAMAS.HSE", 10.0));
        stockMap.put("Sanoma Oyj", new IndexMarketData("SAA1V.HSE", 2.2));
        //FIXME:!! Stora Enso Oyj A -> Stora Enso Oyj R stock!
        stockMap.put("Stora Enso Oyj A", new IndexMarketData("STEAV.HSE", 3.9));
        stockMap.put("TeliaSonera AB", new IndexMarketData("TLS1V.HSE",6.4));
        stockMap.put("Tieto Oyj", new IndexMarketData("TIE1V.HSE", 1.5));
        stockMap.put("UPM-Kymmene Oyj", new IndexMarketData("UPM1V.HSE", 6.4));
        stockMap.put("Wärtsilä Corporation", new IndexMarketData("WRT1V.HSE", 4.0));
        stockMap.put("YIT Oyj", new IndexMarketData("YTY1V.HSE", 2.6));
        
        
        
        
        
        stockMap.put("NIKKEI225", new IndexMarketData("INDEXNIKKEI:NI225", 1.0));
        stockMap.put("IP", new IndexMarketData("NYSE:IP", 1.0));
        

    }

    private class IndexMarketData {

        public IndexMarketData(String shortName, double indxWeight) {
            this.shortName = shortName;
            this.indxWeight = indxWeight;
        }

        String shortName;
        double indxWeight;
    }

    public Iterator iterator() {
       return stockMap.entrySet().iterator();
    }


    public String getHexName(String name) {
        IndexMarketData index = stockMap.get(name);
        if (index== null) {
            System.err.printf("Error: could not locate name for:%s\n", name);
        }
        return index.shortName;
    }

    public double getStockWeight(String name) {
        IndexMarketData index = stockMap.get(name);
        return index.indxWeight;
    }

    public String[]getNames() {
        return stockMap.keySet().toArray(new String [0]);
    }

    
}
