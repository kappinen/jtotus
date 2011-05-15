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
 *
 *
 *
 * An example of fetcher.
 */

package org.jtotus.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.BasicConfigurator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jtotus.common.StockNames;



/**
 *
 * @author Evgeni Kappinen
 */


public class NetworkGoogle implements InterfaceDataBase {
    private static final String url = "http://www.google.com/finance/historical";
    private static final String timePatternForWrite = "MMMM+d'%2C'+Y";
    private static final String timePatternForRead = "dd-MMMM-yy";
    private DefaultHttpClient client = new DefaultHttpClient();
    private static final StockNames names = new StockNames();
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
    
    public NetworkGoogle (){
//        BasicConfigurator.configure();
    }

    public BigDecimal fetchClosingPrice(String stockName, DateTime calendar) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigDecimal fetchAveragePrice(String stockName, DateTime time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigDecimal fetchVolume(String stockName, DateTime date) {
       throw new UnsupportedOperationException("Not supported yet.");
    }

    public void storeClosingPrice(String stockName, DateTime date, BigDecimal value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void storeVolume(String stockName, DateTime date, BigDecimal value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HashMap<String, Double> fetchPeriodAsMap(String stockName, DateTime startDate, DateTime endDate) {
        HashMap<String, Double> retMap = new HashMap<String, Double>(500);
        try {
            DateTimeFormatter formatterOUT = DateTimeFormat.forPattern(timePatternForWrite);
            DateTimeFormatter formatterIN = DateTimeFormat.forPattern(timePatternForRead);
            
            String query = url + "?q=" + names.getHexName(stockName) + "&"
                               + "startdate=" + formatterOUT.print(startDate) + "&"
                               + "enddate=" + formatterOUT.print(endDate) + "&"
                               + "output=csv&num=30&start=0";
            
            System.out.printf("HttpGet:%s : date:%s\n", query, formatterOUT.print(startDate));
            HttpGet httpGet = new HttpGet(query);
            
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            
//            Date, Open,High,Low,Close,Volume
            String line = reader.readLine(); //Header
            while ((line = reader.readLine()) != null) {
                String []values = line.split(",");
                DateTime date = formatterIN.parseDateTime(values[0]);
                double value = Double.parseDouble(values[4]);
                retMap.put(formatter.print(date), value);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(NetworkGoogle.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return retMap;
    }

}
