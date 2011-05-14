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

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import org.jtotus.common.Helper;
import org.apache.log4j.BasicConfigurator;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author Evgeni Kappinen
 */


public class NetworkGoogle implements InterfaceDataBase {
    Helper help = null;
    public String urlName="http://www.google.com/finance/historical?q=PINK:FOJCF";
    public String patternString="MMM dd, yyyy";

    
    public NetworkGoogle (){
        help = Helper.getInstance();
        BasicConfigurator.configure();
    }


    public BigDecimal fetchClosingPrice(String stockName, DateTime calendar) {
        BigDecimal result = null;
        URL url;

        help.debug("NetworkGoogle", "fetchClosingPrice(%s,%s)\n",stockName, calendar.toString());

        
        try {
            url = new URL(urlName);
            Document doc = Jsoup.parse(url, 3*1000);

                String title  = doc.title();
                Elements elems = doc.select("td");

                Iterator <Element>iter = elems.iterator();
                while(iter.hasNext()){
                    Element elem = iter.next();
                    String data = elem.html();

                    SimpleDateFormat trueDate = new SimpleDateFormat(patternString);
                    trueDate.setCalendar(calendar.toGregorianCalendar());
                    
                    if(data.compareTo(help.dateToString(trueDate)) == 0)
                    {

                        for(int i=0;i<4;i++) {
                            elem = iter.next();
                        }


                        data = elem.html();
                        System.out.printf("Closing Price:%s\n", data);
                        
                    }
                   

                }

                System.out.printf("The host title:%s found:%d\n", title, elems.size());



            
        } catch (IOException ex) {
            Logger.getLogger(NetworkGoogle.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return result;
    }

    public BigDecimal fetchAveragePrice(String stockName, DateTime time) {
        return null;
    }

    public BigDecimal fetchVolume(String stockName, DateTime date) {
       return null;
    }

    public void storeClosingPrice(String stockName, DateTime date, BigDecimal value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void storeVolume(String stockName, DateTime date, BigDecimal value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }




}
