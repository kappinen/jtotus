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
 * An example of fetcher.
 */
package org.jtotus.database;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.jtotus.common.StockType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.jtotus.common.StockNames;
import org.apache.log4j.BasicConfigurator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jtotus.common.DateIterator;

/**
 *
 * @author Evgeni Kappinen
 */
public class NetworkOP implements InterfaceDataBase {

    public String urlName = "https://www.op.fi/op?sym=";
    public String urlParam = "&id=32455&srcpl=3";
    public String patternString = "yyyy-MM-dd";
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private boolean debug = false;
    
    public NetworkOP() {
        BasicConfigurator.configure();
    }

    public BigDecimal fetchClosingPrice(String stockName, DateTime calendar) {
        return this.fetchData(stockName, calendar, 1);
    }

    public BigDecimal fetchAveragePrice(String stockName, DateTime calendar) {
        return this.fetchData(stockName, calendar, 4);
    }

    public BigDecimal fetchVolume(String stockName, DateTime calendar) {
        return this.fetchData(stockName, calendar, 2);
    }

    private String buildRequest(DateTime calendar, String stockName) {
        //&from_year=2002&from_month=01&from_day=02&to_year=2010&to_month=10&to_day=01
        DateTime fromDate = calendar.toDateTime().minusDays(4);

        SimpleDateFormat startingDate = new SimpleDateFormat("'&from_year='yyyy'&from_month='MM'&from_day='dd");
        SimpleDateFormat endingDate = new SimpleDateFormat("'&to_year='yyyy'&to_month='MM'&to_day='dd");

        String request = urlName
                + new StockNames().getHexName(stockName)
                + urlParam
                + startingDate.format(fromDate.toDate())
                + endingDate.format(calendar.toDate());

        if (debug) {
            System.out.printf("(" + calendar.toDate() + ")The full request: %s \n", request);
        }
        
        return request;
    }
    
    private String buildRequest(final DateTime fromDate, final DateTime toDate, final String stockName) {
        //&from_year=2002&from_month=01&from_day=02&to_year=2010&to_month=10&to_day=01
//        SimpleDateFormat startingDate = new SimpleDateFormat("'&from_year='yyyy'&from_month='MM'&from_day='dd");
//        SimpleDateFormat endingDate = new SimpleDateFormat("'&to_year='yyyy'&to_month='MM'&to_day='dd");
        
        DateTimeFormatter startingDate = DateTimeFormat.forPattern("'&from_year='yyyy'&from_month='MM'&from_day='dd");
        DateTimeFormatter endingDate = DateTimeFormat.forPattern("'&to_year='yyyy'&to_month='MM'&to_day='dd");

        String request = urlName
                + new StockNames().getHexName(stockName)
                + urlParam
                + startingDate.print(fromDate)
                + endingDate.print(toDate);

        if (debug) {
            System.out.printf("(" + toDate.toDate() + ")The full request: %s \n", request);
        }
        
        return request;
    }
    

    public BigDecimal fetchData(String stockName, DateTime date, String type) {
        if (type.compareTo("CLOSE") == 0) {
            return this.fetchData(stockName, date, 1);
        } else if (type.compareTo("VOLUME") == 0) {
            return this.fetchData(stockName, date, 2);
        } else if (type.compareTo("AVRG") == 0) {
            return this.fetchData(stockName, date, 4);
        }
        return null;
    }

    public BigDecimal fetchData(String stockName, DateTime date, int col) {
        BigDecimal result = null;
        URL url;

        System.out.printf("NetworkOP fetchData(%s,hex:%s, date:%s col:%d)\n", 
                stockName, new StockType(stockName).getHexName(), date.toString(), col);
        
        try {
            url = new URL(this.buildRequest(date, stockName));

            Document doc = Jsoup.parse(url, 2 * 1000);

            Elements elems = doc.select("td");

            Iterator<Element> iter = elems.iterator();
            while (iter.hasNext()) {
                Element elem = iter.next();
                String data = elem.html();

                String datePattern = dateFormatter.print(date);

                //String formatHttp = "<div class=\"Ensimmainen\">\n" + datePattern + "\n</div>";
                if (data.indexOf(datePattern) != -1) {

                    for (int i = 0; i < col; i++) {
                        elem = iter.next();
                    }
                    
                    data = elem.text();
                    String fdata = data.replace(',', '.');

                    if (debug) {
                        System.out.printf("Fetched value from OP bank ->:%s for date:%s\n", fdata, datePattern);
                    }

                    return BigDecimal.valueOf(Double.valueOf(fdata).doubleValue());
                }
            }

        } catch (IOException ex) {
            System.out.printf("Failed in :%s\n", "NetworkOP");
            //Logger.getLogger(NetworkGoogle.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
    
    public double[] fetchDataPeriod(String stockName, DateTime fromDate, DateTime toDate, String type) {
       if (type.compareTo("CLOSE") == 0) {
            return this.fetchDataPeriod(stockName, fromDate, toDate, 1);
        } else if (type.compareTo("VOLUME") == 0) {
            return this.fetchDataPeriod(stockName, fromDate, toDate, 2);
        } else if (type.compareTo("AVRG") == 0) {
            return this.fetchDataPeriod(stockName, fromDate, toDate, 4);
        }

       return null;
    }
    
    private double[] fetchDataPeriod(String stockName, DateTime fromDate, DateTime toDate, int col) {
        List<Double> values = new ArrayList<Double>();
        URL url;

        System.out.printf("NetworkOP fetchData(%s,hex:%s, date:%s-%s col:%d)\n", 
                stockName, new StockType(stockName).getHexName(), fromDate.toString(), toDate.toString(), col);

        try {
            url = new URL(this.buildRequest(fromDate, toDate, stockName));

            Document doc = Jsoup.parse(url, 2 * 1000);

            Elements elems = doc.select("td");

            DateIterator dateIter = new DateIterator(fromDate, toDate);
            while (dateIter.hasNext()) {
                Iterator<Element> iter = elems.iterator();
                String datePattern = dateFormatter.print(dateIter.nextInCalendar());
                
                while (iter.hasNext()) {
                    Element elem = iter.next();
                    String data = elem.html();
                    
                    //System.out.printf("Fetching.. :%s\n", dateFormatter.print(dateIter.getCurrentAsCalendar()));
                    //String formatHttp = "<div class=\"Ensimmainen\">\n" + datePattern + "\n</div>";
                    if (data.indexOf(datePattern) != -1) {

                        for (int i = 0; i < col; i++) {
                            elem = iter.next();
                        }

                        data = elem.text();
                        String fdata = data.replace(',', '.');

                        if (debug) {
                            System.out.printf("Fetched value from OP bank ->:%s for date:%s\n", fdata, datePattern);
                        }

                        values.add(Double.valueOf(fdata));
                        break;
                    }
                }
            }

        } catch (IOException ex) {
            System.out.printf("Failed in :%s\n", "NetworkOP");
            //Logger.getLogger(NetworkGoogle.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ArrayUtils.toPrimitive(values.toArray(new Double[0]));
    }

    public void storeClosingPrice(String stockName, DateTime date, BigDecimal value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void storeVolume(String stockName, DateTime date, BigDecimal value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void storeData(String stockName, DateTime date, BigDecimal value, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
