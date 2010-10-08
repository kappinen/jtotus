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
import org.jtotus.common.Helper;
import org.jtotus.common.StockType;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.jtotus.common.StockNames;
import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author Evgeni Kappinen
 */
public class NetworkOP implements InterfaceDataBase {
    Helper help = Helper.getInstance();;
    public String urlName="https://www.op.fi/op?sym=";
    public String urlParam="&id=32455&srcpl=3";
    public String patternString="yyyy-MM-dd";


    public NetworkOP (){
        BasicConfigurator.configure();
    }

   public BigDecimal fetchClosingPrice(String stockName, Calendar calendar) {
        return this.fetchData(stockName, calendar, 1);
    }

   public BigDecimal fetchAveragePrice(String stockName, Calendar calendar){
        return this.fetchData(stockName, calendar, 4);
   }

  private String buildRequest(Calendar calendar, String stockName) {
    //&from_year=2002&from_month=01&from_day=02&to_year=2010&to_month=10&to_day=01
    Calendar fromDate = Calendar.getInstance();
    fromDate.setTime(calendar.getTime());
    fromDate.add(Calendar.DATE, -4);

    SimpleDateFormat startingDate = new SimpleDateFormat("'&from_year='yyyy'&from_month='MM'&from_day='dd");
    SimpleDateFormat endingDate = new SimpleDateFormat("'&to_year='yyyy'&to_month='MM'&to_day='dd");
    
    String request = urlName 
                    +new StockNames().getHexName(stockName)
                    +urlParam
                    +startingDate.format(fromDate.getTime())
                    +endingDate.format(calendar.getTime());
    
    System.out.printf("The full request: %s\n", request);
    return request;
  }


  public BigDecimal fetchData(String stockName, Calendar calendar, int col) {
        BigDecimal result = null;
        URL url;

        
        help.debug("NetworkOP", "fetchClosingPrice(%s,%s)\n",stockName, calendar.toString());
        help.debug("NetworkOP",
                "The value for Stock: %s is :%s\n",stockName,
                new StockType(stockName).getHexName());


        try {
            url = new URL(this.buildRequest(calendar,stockName));

            Document doc = Jsoup.parse(url, 2*1000);

                Elements elems = doc.select("td");

                Iterator <Element>iter = elems.iterator();
                while(iter.hasNext()){
                    Element elem = iter.next();
                    String data = elem.html();


                    SimpleDateFormat trueDate = new SimpleDateFormat(patternString);
                    trueDate.setCalendar(calendar);
                   
                    String formatHttp = "<div class=\"Ensimmainen\">\n"+help.dateToString(trueDate)+"\n</div>";
                   // System.out.printf("Comparing:%s with true date:%s\n", data, formatHttp);
                    if(data.indexOf(help.dateToString(trueDate)) != -1)
                    {

                        for(int i=0;i<col;i++) {
                            elem = iter.next();
                        }
                        
                        data = elem.text();
                        String fdata = data.replace(',', '.');

                        help.debug("NetworkOP",
                                "Closing Price:%f for:%s\n",
                                Float.valueOf(fdata).floatValue(),
                                help.dateToString(trueDate));

                        System.out.printf("!!!!!!!!! form INternet! ->:%s\n",fdata);
                        return BigDecimal.valueOf(Double.valueOf(fdata).doubleValue());
                    }


                }

                //System.out.printf("The host title:%s found:%d\n", title, elems.size());

        } catch (IOException ex) {
            //System.out.printf("Failed in :%s\n","NetworkOP");
            //Logger.getLogger(NetworkGoogle.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public BigDecimal fetchVolume(String stockName, Calendar calendar) {
        
        return this.fetchData(stockName, calendar, 3);
    }


}
