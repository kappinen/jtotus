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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
    
    private static final StockNames names = new StockNames();
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");


    private static final int dateInd = 0;
    private static final int openInd = 1;
    private static final int highInd = 2;
    private static final int lowhInd = 3;
    private static final int closehInd = 4;
    private static final int volumeInd = 5;

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
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = null;
        try {
            DateTimeFormatter formatterOUT = DateTimeFormat.forPattern(timePatternForWrite);
            DateTimeFormatter formatterIN = DateTimeFormat.forPattern(timePatternForRead);
            
            String query = url + "?q=" + names.getHexName(stockName) + "&"
                               + "startdate=" + formatterOUT.print(startDate) + "&"
                               + "enddate=" + formatterOUT.print(endDate) + "&"
                               + "&num=30&output=csv";
            
            System.out.printf("HttpGet:%s : date:%s\n", query, formatterOUT.print(startDate));
            httpGet = new HttpGet(query);
            
            HttpResponse response = client.execute(httpGet);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new IOException("Invalid response from server: " + status.toString());
            }

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
            System.err.printf("Unable to find market data for: %s - %s\n", names.getHexName(stockName), stockName);
        } catch (IllegalArgumentException ex) {
            System.err.printf("Unable to find market data for: %s - %s\n", names.getHexName(stockName), stockName);
        } finally {
            if (httpGet != null) {
                
            }
        }
        
        System.out.printf("NetworkGoogle fetched : %d values\n", retMap.size());
        return retMap;
    }

    public BigDecimal fetchDataInternal(String stockName, DateTime endDate, int type) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = null;
        DateTime startDate = endDate.minusDays(5);
        BigDecimal retValue = null;
        try {
            DateTimeFormatter formatterOUT = DateTimeFormat.forPattern(timePatternForWrite);
            DateTimeFormatter formatterIN = DateTimeFormat.forPattern(timePatternForRead);

            String query = url + "?q=" + names.getHexName(stockName) + "&"
                               + "startdate=" + formatterOUT.print(startDate) + "&"
                               + "enddate=" + formatterOUT.print(endDate) + "&"
                               + "&num=30&output=csv";

            System.out.printf("HttpGet:%s : date:%s\n", query, formatterOUT.print(startDate));
            httpGet = new HttpGet(query);

            HttpResponse response = client.execute(httpGet);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new IOException("Invalid response from server: " + status.toString());
            }

            HttpEntity entity = response.getEntity();

            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));

//            Date, Open,High,Low,Close,Volume
            String line = reader.readLine(); //Header
            while ((line = reader.readLine()) != null) {
                String []values = line.split(",");
                double value = Double.parseDouble(values[type]);
                retValue = BigDecimal.valueOf(value);
            }

        } catch (IOException ex) {
            System.err.printf("Unable to find market data for: %s - %s\n", names.getHexName(stockName), stockName);
        } catch (IllegalArgumentException ex) {
            System.err.printf("Unable to find market data for: %s - %s\n", names.getHexName(stockName), stockName);
        } finally {
            if (httpGet != null) {
                //FIXME: what to do ?
            }
        }
        
        return retValue;
    }


    //FIXME: impliment
    
    public BigDecimal fetchData(String stockName, DateTime date, String type) {

        if (type.compareTo("OPEN") == 0) {
               return this.fetchDataInternal(stockName, date, openInd);
        } else if (type.compareTo("HIGH") == 0) {
            return this.fetchDataInternal(stockName, date, highInd);
        } else if (type.compareTo("LOW") == 0) {
            return this.fetchDataInternal(stockName, date, lowhInd);
        } else if (type.compareTo("CLOSE") == 0) {
            return this.fetchDataInternal(stockName, date, closehInd);
        } else if (type.compareTo("VOLUME") == 0) {
            return this.fetchDataInternal(stockName, date, volumeInd);
        }
        return null;
    }

    public void storeData(String stockName, DateTime date, BigDecimal value, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] fetchDataPeriod(String stockName, DateTime fromDate, DateTime toDate, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
