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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import brokerwatcher.eventtypes.StockTick;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;
import org.jtotus.engine.StartUpLoader;
import org.jtotus.network.BrokerConnector.ConnectorState;

/**
 *
 * @author Evgeni Kappinen
 */
public class NordnetConnect implements NetworkTickConnector {
    ConnectorState state = BrokerConnector.state.INITIAL;
    //private static final String _LOGIN_URL_ = "https://www.nordnet.fi/mux/login/login.html";
    //private static final String _LOGIN_URL_ = "https://www.nordnet.fi/mux/login/start.html";
    private static final String _LOGIN_URL_ = "https://www.nordnet.fi/mux/login/startFI.html";
    private static final String _LOGININPUT_URL_ = "https://www.nordnet.fi//mux/login/login.html";
    //private static final String _PORTFOLIO_URL_ = "https://www.nordnet.fi/mux/web/depa/mindepa/depaoversikt.html";
    private static final String _PORTFOLIO_URL_ = "https://www.nordnet.fi/mux/web/user/overview.html";
    private static final String _STOCK_INFO_URL_ = "https://www.nordnet.fi/mux/web/marknaden/aktiehemsidan/index.html";
    private static final String _ECRYPT_JS_ = "https://www.nordnet.fi/now/js/encrypt.js";

    private HashMap<String, Integer> stockNameToIndex = null;
    private BrokerConnector connector = null;
    private final static Log log = LogFactory.getLog( NordnetConnect.class );
    

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

    public boolean authenticated() {
        String loginPage = null;

        if (connector == null) {
            System.err.printf("Failure connector is empty\n");
            return false;
        }

        loginPage = connector.getPage(_PORTFOLIO_URL_);
        if (loginPage == null) {
            System.err.printf("Failure unable to fetch portfolio\n");
            return false;
        }

        Document doc = Jsoup.parse(loginPage);
        Elements elements = doc.select("title");

        //FIXME: UTF-8 for httpclient!
        if (elements.html().equals("Yleisn&auml;kym&auml; - Nordnet")) {
            return true;
        } else {
            System.err.printf("Failure in match for : %s \n", elements.html());
        }

        return false;
    }

    public String fetchEncryptedPassword(String encryptJS, String pass, String pubKey, String sessionId) {
        String password = null;

        StartUpLoader loader = StartUpLoader.getInstance();

        //ScriptEngineManager mgr = loader.getLoadedScriptManager();
        //         Bindings bindings = mgr.getBindings();

         ScriptEngine engine =  loader.getLoadedEngine();
         Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);

         try {
             StringBuilder strBuild = new StringBuilder();
             strBuild.append(encryptJS);

             strBuild.append(" \n var keyObj = RSA.getPublicKey(\'"+pubKey+"\');\n"
                            + "  var encryptedPass = RSA.encrypt(\'"+pass+"\', keyObj, \'"+sessionId+"\');\n");

             engine.eval(strBuild.toString(), bindings);

             password = (String)bindings.get("encryptedPass");
             
        } catch (ScriptException ex) {
            Logger.getLogger(NordnetConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

         log.info("JavaScript engine loaded:" + engine.NAME);

         return password;
    }
        


    private String fetchEncryptionScript(String filename) {
        String script = null;
        String line = null;
        
        BufferedReader input = null;
        StringBuilder data = new StringBuilder();


        try {
            input = new BufferedReader(new FileReader(filename));

            while ((line = input.readLine()) != null) {
                data.append(line);
                data.append(System.getProperty("line.separator"));
            }
            
            script =  data.toString();

        } catch (IOException ex) {
            Logger.getLogger(NordnetConnect.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(NordnetConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return script;
    }


    private boolean connectAndAuth(String user, String password) {
        ArrayList<String> inputList = new ArrayList();

        connector = new NordnetConnector();

        String encryptJS = fetchEncryptionScript("./lib/encrypt.js");
        if (encryptJS == null) {
            encryptJS = connector.getPage(_ECRYPT_JS_);
            if (encryptJS == null) {
                System.err.printf("Failed to get encrypt javascript\n");
                return false;
            }
        }
        
        String loginPage = connector.getPage(_LOGIN_URL_);
        if (loginPage == null) {
            System.err.printf("Failed to get login page\n");
            return false;
        }

        Document doc = Jsoup.parse(loginPage);
        Elements elements = doc.select("input");

        Iterator<Element> iter = elements.iterator();
        while (iter.hasNext()) {
            Element elem = iter.next();
            inputList.add(elem.attr("name"));
        }

        if (inputList.size()<2) {
            System.err.printf("Failure: \n %s \n", loginPage);
            return false;
        }

        elements = doc.select("script");
        if (elements.size() < 4) {
            System.err.printf("Incorrect size of script elements\n");
            return false;
        }
        Element elem = elements.get(4);

        
        String []data = elem.data().split("'");
        if (data.length < 8) {
            System.err.printf("Incorrect size of splitted elements for pass and login tokens\n");
            return false;
        }
        log.info("Got element: data:"+data[7]+" html:" + data[5]);

        String encryptedPassword = fetchEncryptedPassword(encryptJS,
                                                 password,
                                                 data[5].trim() /*pubKey*/,
                                                 data[7].trim() /*sessionId*/);

        loginPage = connector.authenticate(_LOGININPUT_URL_,
                                           inputList.get(3), user,
                                           inputList.get(5), encryptedPassword);

        System.err.printf("login: %s = %s pass: %s = %s\n", inputList.get(3), user, inputList.get(5), encryptedPassword);

        if (loginPage == null) {
            System.err.printf("Failed to get authenticate\n");
            return false;
        }

        if (!authenticated()) {
            return false;
        }
        
        return true;
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



    private StockTick parseAuthenticatedStream(String infoPage, String stockName) {
        StockTick tick = null;

        Document doc = Jsoup.parse(infoPage);
        Elements elements = doc.select("tr[class=first]");

        doc = Jsoup.parse(elements.html());
        elements = doc.select("td");

        if (elements.size() != 15) { //not authenticated 13
            return tick;
        }
        tick = new StockTick();
        tick.setStockName(stockName);

        Iterator<Element> iter = elements.iterator();
        for (int count = 0; iter.hasNext(); count++) {
            Element elem = iter.next();

            log.info("Element value ("+count+"):"+elem.text());
            switch (count) {
                case 3:
                    if (!elem.text().equalsIgnoreCase("OMX Helsinki")) {
                        System.err.printf("Data corruption in broker site? :%s for: %s\n", elem.text(), stockName);
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
                case 14://Time
                    tick.setTime(elem.text().trim());
                    break;

                //TODO:currency and time
                default:
                    log.info("Not matched(" +count+ ") = " + elem.text());
                    break;
            }
        }
        log.info("StockTick:" + tick.toString());

        return tick;
    }



    private StockTick parseNonAuthenticatedStream(String infoPage, String stockName) {
        StockTick tick = null;

        Document doc = Jsoup.parse(infoPage);
        Elements elements = doc.select("tr[class=first]");

        doc = Jsoup.parse(elements.html());
        elements = doc.select("td");

        if (elements.size() != 13) { //not authenticated 13
            return tick;
        }
        tick = new StockTick();
        tick.setStockName(stockName);

        Iterator<Element> iter = elements.iterator();
        for (int count = 0; iter.hasNext(); count++) {
            Element elem = iter.next();

            System.out.printf("Non-Auth Element value (%d):%s for:%s\n", count, elem.text(), stockName);
            switch (count) {
                case 1:
                    if (!elem.text().equalsIgnoreCase("OMX Helsinki")) {
                        System.err.printf("Data corruption in broker site? :%s for: %s\n", elem.text(), stockName);
                        return null;
                    }
                    break;
                case 2://latest price
                    tick.setLatestPrice(Double.parseDouble(elem.text().replace(",", ".").trim()));
                    break;
                case 3://latest buy
                    tick.setLatestBuy(Double.parseDouble(elem.text().replace(",", ".").trim()));
                    break;
                case 4://latest sell
                    tick.setLatestSell(Double.parseDouble(elem.text().replace(",", ".").trim()));
                    break;
                case 5://latest Highest
                    tick.setLatestHighest(Double.parseDouble(elem.text().replace(",", ".").trim()));
                    break;
                case 6://latest Lowest
                    tick.setLatestLowest(Double.parseDouble(elem.text().replace(",", ".").trim()));
                    break;
                case 9://Volume
                    tick.setVolume(Double.parseDouble(elem.text().replace(" ", "").trim()));
                    break;
                case 10://Trade Sum
                    tick.setTradesSum(Double.parseDouble(elem.text().replace(" ", "").trim()));
                    break;
                case 12://Time
                    tick.setTime(elem.text().trim());
                    break;

                //TODO:currency and time
                default:
                    System.out.printf("Not matched(%d) = %s \n", count, elem.text());
                    break;
            }
        }
        System.out.printf("StockTick:%s\n", tick.toString());

        return tick;
    }



    // http://jsoup.org/apidocs/org/jsoup/select/Selector.html
    public StockTick getTick(String stockName) {

        Integer index = stockNameToIndex.get(stockName);
        if (index == null) {
            System.err.printf("Index is not found for :%s\n", stockName);
            return null;
        }

        String infoPage = connector.getPage("%s?identifier=%d&marketid=%d",
                                            _STOCK_INFO_URL_, index.intValue(), 24);

        //Try to reconnect to server once.
        if (infoPage == null) {
            if (!this.connect()) {
                return null;
            } else {
                infoPage = connector.getPage("%s?identifier=%d&marketid=%d",
                                            _STOCK_INFO_URL_, index.intValue(), 24);
                if (infoPage == null) {
                    return null;
                }
            }
        }

        if(authenticated()) {
            return parseAuthenticatedStream(infoPage, stockName);
        }else {
            return parseNonAuthenticatedStream(infoPage, stockName);
        }

    }

}
