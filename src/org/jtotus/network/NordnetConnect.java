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
package org.jtotus.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jtotus.common.StockTick;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;
import org.jtotus.crypt.JtotusKeyRingPassword;

/**
 *
 * @author Evgeni Kappinen
 */
public class NordnetConnect implements NetworkTickConnector {

    private static final String _LOGIN_URL_ = "https://www.nordnet.fi/mux/login/login.html";
    private static final String _PORTFOLIO_URL_ = "https://www.nordnet.fi/mux/web/depa/mindepa/depaoversikt.html";
    private static final String _STOCK_INFO_URL_ = "https://www.nordnet.fi/mux/web/marknaden/aktiehemsidan/index.html";
    HashMap<String, Integer> stockNameToIndex = null;
    NordnetConnector connector = null;

    // Connects to login page, get seeds for user and password
    // POST data to server, by calling NordnetConnector
    private void fillStockNamesConverter() {
        if (stockNameToIndex != null) {
            return;
        }

        stockNameToIndex = new HashMap<String, Integer>();

        stockNameToIndex.put("Cargotec Oyj", 29983);
        //stockNameToIndex.put("Elisa Oyj","ELI1V.HSE");
        stockNameToIndex.put("Fortum Oyj", 24271);
        stockNameToIndex.put("Kemira Oyj", 24292);
        stockNameToIndex.put("KONE Oyj", 75061);
        stockNameToIndex.put("Konecranes Oyj", 24284);
        stockNameToIndex.put("Metso Oyj", 24302);
        stockNameToIndex.put("Neste Oil", 29375);
        stockNameToIndex.put("Nokia Oyj", 24311);
        //stockNameToIndex.put("Nokian Renkaat Oyj","NRE1V.HSE");
        stockNameToIndex.put("Nordea Bank AB", 24308);
        stockNameToIndex.put("Outokumpu Oyj", 24321);
        stockNameToIndex.put("Outotec Oyj", 36695);
        stockNameToIndex.put("Pohjola Bank A", 24316);
        stockNameToIndex.put("Rautaruukki Oyj", 24342);
        stockNameToIndex.put("Sampo Oyj A", 24346);
        stockNameToIndex.put("Sanoma Oyj", 24366);
        stockNameToIndex.put("Stora Enso Oyj A", 24359);
        stockNameToIndex.put("TeliaSonera AB", 24381);
        stockNameToIndex.put("Tieto Oyj", 24376);
        stockNameToIndex.put("UPM-Kymmene Oyj", 24386);
        stockNameToIndex.put("Wärtsilä Corporation", 24394);
        stockNameToIndex.put("YIT Oyj", 24397);

    }

    public boolean connectAndAuth(String user, String password) {
        boolean result = true;
        ArrayList<String> inputList = new ArrayList();

        connector = new NordnetConnector();

        String loginPage = connector.getPage(_LOGIN_URL_);

        Document doc = Jsoup.parse(loginPage);
        Elements element = doc.select("input");

        Iterator<Element> iter = element.iterator();
        while (iter.hasNext()) {
            Element elem = iter.next();
            inputList.add(elem.attr("name"));
        }


        loginPage = connector.authenticate(_LOGIN_URL_,
                inputList.get(inputList.size() - 2), user,
                inputList.get(inputList.size() - 1), password);


        loginPage = connector.getPage(_PORTFOLIO_URL_);

//            System.out.printf("Data from jsoup:%s last index:%s and :%s\n",
//                        loginPage, inputList.get(inputList.size() - 2),
//                        inputList.get(inputList.size() - 1));

        return result;

    }

    public boolean connect() {
        ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");
        GUIConfig config = loader.getConfig();
        if (config == null) {
            return false;
        }

        this.fillStockNamesConverter();

        return this.connectAndAuth(config.getBrokerLogin(),
                config.getBrokerPassword());

    }

    // http://jsoup.org/apidocs/org/jsoup/select/Selector.html
    public StockTick getTick(String stockName) {
        StockTick tick = null;

        Integer index = stockNameToIndex.get(stockName);
        if (index == null) {
            System.err.printf("Index is not found for :%s\n", stockName);
            return null;
        }

        String infoPage = connector.getPage("%s?identifier=%d&marketid=%d",
                _STOCK_INFO_URL_, index.intValue(), 24);


        Document doc = Jsoup.parse(infoPage);
        Elements elements = doc.select("tr[class=first]");

        doc = Jsoup.parse(elements.html());
        elements = doc.select("td");

        if (elements.size() == 15) { //not authenticated 13
            tick = new StockTick();
            tick.setStockName(stockName);

            Iterator<Element> iter = elements.iterator();
            for (int count = 0; iter.hasNext();count++) {
                Element elem = iter.next();

                //System.out.printf("Element value (%d):%s\n", count, elem.text());
                switch (count) {
                    case 3:
                        if (!elem.text().equalsIgnoreCase("OMX Helsinki")) {
                            System.err.printf("Data corruption in broker site? :%s for: %s\n", elem.text(),stockName);
                            return null;
                        }
                        break;
                    case 4://latest price
                        tick.setLatestPrice(Double.parseDouble(elem.text().replace(",", ".").trim()));
                        break;
                    case 5://latest buy
                        tick.setLatestBuy(Double.parseDouble(elem.text().replace(",", ".").trim()));
                        break;
                    case 6://latest sell
                        tick.setLatestSell(Double.parseDouble(elem.text().replace(",", ".").trim()));
                        break;
                    case 7://latest Highest
                        tick.setLatestHighest(Double.parseDouble(elem.text().replace(",", ".").trim()));
                        break;
                    case 8://latest Lowest
                        tick.setLatestLowest(Double.parseDouble(elem.text().replace(",", ".").trim()));
                        break;
                    case 11://latest Lowest
                        tick.setVolume(Double.parseDouble(elem.text().replace(" ", "").trim()));
                        break;
                    case 12://latest Lowest
                        tick.setTradesSum(Double.parseDouble(elem.text().replace(" ", "").trim()));
                        break;

                    //TODO:currency and time
                    default:
                        break;
                }
            }
            System.out.printf("StockTick:%s\n",tick.toString());
        }else {
            System.err.printf("Data corruption in broker site size of elements? :%d for:%s\n", elements.size(),stockName);
            return null;
        }

        return tick;
    }

    public static void main(String args[]) {

        JtotusKeyRingPassword pass = JtotusKeyRingPassword.getInstance();
        pass.putKeyRingPassword("test");

        NordnetConnect test = new NordnetConnect();
        test.connect();
        test.getTick("Nokia Oyj");


    }
}
