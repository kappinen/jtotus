/*
This file is part of JLucrum.

 JLucrum is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

 JLucrum is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with  JLucrum.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jlucrum.datafetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Evgeni Kappinen
 */
public class FetcherNasdaqOmxNordic implements MarketFetcher {

    private DefaultHttpClient httpclient = null;
    private String url = "http://www.nasdaqomxnordic.com/webproxy/DataFeedProxy.aspx";
    private final Map<String, String> stockMap = new HashMap<String, String>();
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private boolean debug = false;
    private static int dataMap[] = { 4, 6, 2, 3, 5, 7, 8};

    public FetcherNasdaqOmxNordic() {
        //http://en.wikipedia.org/wiki/OMX_Helsinki_25
        stockMap.put("Cargotec Oyj", "HEX29983");
        stockMap.put("Elisa Oyj", "HEX24254");
        stockMap.put("Fortum Oyj", "HEX24271");
        stockMap.put("Kemira Oyj", "HEX24292");
        stockMap.put("KONE Oyj", "HEX29981");
        stockMap.put("Konecranes Oyj", "HEX24284");
        stockMap.put("Metso Oyj", "HEX24302");
        stockMap.put("Neste Oil", "HEX29375");
        stockMap.put("Nokia Oyj", "HEX24311");
        stockMap.put("Nokian Renkaat Oyj", "HEX24312");
        stockMap.put("Nordea Bank AB", "HEX24308");
        stockMap.put("Outokumpu Oyj", "HEX24321");
        stockMap.put("Outotec Oyj", "HEX36695");
        stockMap.put("Pohjola Bank A", "HEX24316");
        stockMap.put("Rautaruukki Oyj", "HEX24342");
        stockMap.put("Sampo Oyj A", "HEX24346");
        stockMap.put("Sanoma Oyj", "HEX24366");
        stockMap.put("Stora Enso Oyj A", "HEX24359");
        stockMap.put("Stora Enso Oyj R", "HEX24360");
        stockMap.put("TeliaSonera AB", "HEX24381");
        stockMap.put("Tieto Oyj", "HEX24376");
        stockMap.put("UPM-Kymmene Oyj", "HEX24386");
        stockMap.put("Wärtsilä Corporation", "HEX24394");
        stockMap.put("YIT Oyj", "HEX24397");
    }

    
    private DefaultHttpClient getClient() {

        if (httpclient != null) {
            return httpclient;
        }

        httpclient = new DefaultHttpClient();

        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, 
                                            CookiePolicy.BROWSER_COMPATIBILITY);

        return httpclient;
    }

    
    
    public Map<String, Double> getData(String name, DateTime fromDate, DateTime toDate, int type) {
        HttpPost httpPost = new HttpPost(this.url);
        HttpResponse response = null;
        HashMap<String, Double> retMap = new HashMap<String, Double>();
        
        httpclient = getClient();
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String fixedName = stockMap.get(name);
        if (fixedName == null) {
            fixedName = name;
        }
        
        nameValuePairs.add(new BasicNameValuePair("xmlquery", "<post> "
                + "<param name=\"SubSystem\" value=\"History\"/> "
                + "<param name=\"Action\" value=\"GetDataSeries\"/>"
                + "<param name=\"AppendIntraDay\" value=\"no\"/>"
                + "<param name=\"Instrument\" value=\"" + fixedName + "\"/>"
                + "<param name=\"FromDate\" value=\"" + dateFormatter.print(fromDate) + "\"/>"
                + "<param name=\"ToDate\" value=\"" + dateFormatter.print(toDate) + "\"/> "
                + "<param name=\"hi__a\" value=\"0,1,2,4,21,8,10,11,12,9\"/> "
                + "<param name=\"ext_xslt\" value=\"/nordicV3/hi_table_shares_adjusted.xsl\"/> "
                + "<param name=\"ext_xslt_options\" value=\",undefined,\"/> "
                + "<param name=\"ext_xslt_lang\" value=\"en\"/> "
                + "<param name=\"ext_xslt_hiddenattrs\" value=\",ip,iv,\"/> "
                + "<param name=\"ext_xslt_tableId\" value=\"historicalTable\"/> "
                + "<param name=\"app\" value=\"/osakkeet/Historialliset_kurssitiedot/\"/> "
                + "</post>"));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String resString = EntityUtils.toString(entity, "UTF-8");
            if (debug) {
                System.out.printf("Respond:%s", resString);
            }
            
            
            Document doc = Jsoup.parse(resString);
            Elements elems = doc.select("tr");
            System.out.printf("tr size:%d\n", elems.size());
            Iterator<Element> iter = elems.iterator();
            iter.next(); //skip head
            while(iter.hasNext()) {
               Element elem = iter.next();
               Elements dataElems = elem.getAllElements();
               /* Output Example:
                    <tr id="historicalTable-">
                      <td>2011-09-08</td>
                      <td>25.29</td>
                      <td>24.38</td>
                      <td>24.93</td>
                      <td>24.92</td>
                      <td>895,389</td>
                      <td>22,298,455</td>
                      <td>5,524</td>
                    </tr>
                */
               Element dateElem = dataElems.get(1);
               Element dataElem = dataElems.get(dataMap[type]);
               if (dateElem.html() == null || dateElem.html().length() == 0||
                   dataElem.html() == null || dataElem.html().length() == 0) {
                   continue;
               }

               retMap.put(dateElem.html(), Double.valueOf(dataElem.html().replaceAll(",", "")));

               if (debug) {
                System.out.printf("Date:%s data:%s\n", dateElem.html(), dataElem.html());
               }
            }

            System.out.printf("Fetched %s/%s from NasdaqOmxNordic:%d\n", name, fixedName, retMap.size());
        } catch (IOException ex) {
            Logger.getLogger(FetcherNasdaqOmxNordic.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return retMap;
    }
    

    @Override
    public Map<String, Double> fetchDataPeriod(String name, DateTime fromDate, DateTime toDate, int type) {
        return this.getData(name, fromDate, toDate, type);
    }

    @Override
    public Double fetchData(String name, DateTime date, int type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    public static void main(String av[]) {
        FetcherNasdaqOmxNordic fetcher = new FetcherNasdaqOmxNordic();
        fetcher.getData("Metso Oyj", new DateTime().minusDays(20), new DateTime(), 0);
    }
}
