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
import java.net.URL;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
public class FetcherOsuusPankki implements MarketFetcher {

    public static final String urlName = "https://www.op.fi/op?sym=";
    public static final String patternString = "yyyy-MM-dd";
    private static final String startDateFormat = "'&from_year='yyyy'&from_month='MM'&from_day='dd";
    private static final String endDateFormat = "'&to_year='yyyy'&to_month='MM'&to_day='dd";
    
    public String urlParam = "&id=32455&srcpl=3";
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private boolean debug = false;
    private static int dataMap[] = { 1, 2, -1, 4};
    
    
    private String buildRequest(final DateTime fromDate, final DateTime toDate, final String stockName) {
        DateTimeFormatter startingDate = DateTimeFormat.forPattern(this.startDateFormat);
        DateTimeFormatter endingDate = DateTimeFormat.forPattern(this.endDateFormat);

        return  this.urlName
                + stockName
                + this.urlParam
                + startingDate.print(fromDate)
                + endingDate.print(toDate);
    }

    public Double fetchData(String stockName, DateTime date, int type) {
        URL url;

        int intType = dataMap[type];
        
        try {
            url = new URL(this.buildRequest(date, date, stockName));

            Document doc = Jsoup.parse(url, 2 * 1000);

            Elements elems = doc.select("td");

            Iterator<Element> iter = elems.iterator();
            while (iter.hasNext()) {
                Element elem = iter.next();
                String data = elem.html();

                String datePattern = dateFormatter.print(date);

                if (data.indexOf(datePattern) != -1) {

                    for (int i = 0; i < intType; i++) {
                        elem = iter.next();
                    }
                    
                    data = elem.text();
                    String fdata = data.replace(',', '.');

                    if (debug) {
                        System.out.printf("Fetched value from OP bank ->:%s for date:%s\n", fdata, datePattern);
                    }

                    return Double.valueOf(fdata).doubleValue();
                }
            }

        } catch (IOException ex) {
            System.out.printf("Failure in :%s message:%s\n", FetcherOsuusPankki.class.getCanonicalName(), ex.getMessage());
            return null;
        }

        return null;
    }
    
    @Override
    public Map<String, Double> fetchDataPeriod(String name, DateTime fromDate, DateTime toDate, int type) {
        HashMap<String, Double> retMap = new HashMap<String, Double>();
        URL url = null;

        int intType = dataMap[type];

        try {
            url = new URL(this.buildRequest(fromDate, toDate, name));

            Document doc = Jsoup.parse(url, 2 * 1000);

            Elements elems = doc.select("td");

            DateIterator dateIter = new DateIterator(fromDate, toDate);
            while (dateIter.hasNext()) {
                Iterator<Element> iter = elems.iterator();
                String datePattern = dateFormatter.print(dateIter.nextInCalendar());

                while (iter.hasNext()) {
                    Element elem = iter.next();
                    String data = elem.html();

                    if (data.indexOf(datePattern) != -1) {

                        for (int i = 0; i < intType; i++) {
                            elem = iter.next();
                        }

                        data = elem.text();
                        String fdata = data.replace(',', '.');


                        retMap.put(datePattern, Double.valueOf(fdata));
                        break;
                    }
                }
            }

        } catch (IOException ex) {
            System.out.printf("Failure in :%s message:%s\n", FetcherOsuusPankki.class.getCanonicalName(), ex.getMessage());
            return null;
        }

        return retMap;
    }

    
    public static void main(String []av) {
        String patternString = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(patternString);
        System.out.println("fetchDataPeriod");
        String name = "KNEBV.HSE";

        DateTime fromDate = formatter.parseDateTime("2011-05-01");
        DateTime toDate = formatter.parseDateTime("2011-05-05");;
        int type = 0;
        FetcherOsuusPankki instance = new FetcherOsuusPankki();
        
        
        Map<String,Double> result = instance.fetchDataPeriod(name, fromDate, toDate, type);
        
        for (Entry<String,Double> entry:result.entrySet()) {
            System.out.printf("%s, %f\n", entry.getKey(), entry.getValue());
        }
    }
}
