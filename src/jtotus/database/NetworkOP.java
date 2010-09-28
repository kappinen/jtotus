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

package jtotus.database;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import jtotus.common.Helper;
import jtotus.common.StockType;
import java.math.BigDecimal;
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
    public String urlParam="&id=32455&srcpl=8";
    public String patternString="yyyy-MM-dd";


    public NetworkOP (){
        BasicConfigurator.configure();
    }



   public BigDecimal fetchClosingPrice(String stockName, SimpleDateFormat time) {
        return fetchData(stockName, time, 1);
    }

   public BigDecimal fetchAveragePrice(String stockName, SimpleDateFormat time){
        return fetchData(stockName, time, 4);
   }

    public BigDecimal fetchData(String stockName, SimpleDateFormat time, int col) {
        BigDecimal result = null;
        URL url;

        
        help.debug("NetworkOP", "fetchClosingPrice(%s,%s)\n",stockName, help.dateToString(time));
        help.debug("NetworkOP",
                "The value for Stock: %s is :%s\n",stockName,
                new StockType(stockName).getHexName());

        try {
            url = new URL(urlName + new StockType(stockName).getHexName() + urlParam);

            Document doc = Jsoup.parse(url, 3*1000);

                Elements elems = doc.select("td");

                Iterator <Element>iter = elems.iterator();
                while(iter.hasNext()){
                    Element elem = iter.next();
                    String data = elem.html();

                    SimpleDateFormat trueDate = (SimpleDateFormat) time.clone();
                    trueDate.applyPattern(patternString);
                   
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


}
