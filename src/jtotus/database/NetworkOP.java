/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 *
 *
 *
 *
 *
 * An example of fetcher.
 */

package jtotus.database;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.common.Helper;


import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author kappiev
 */
public class NetworkOP implements InterfaceDataBase {
    Helper help = null;
    public String urlName="https://www.op.fi/op?sym=FUM1V.HSE&id=32455&srcpl=8";
    public String patternString="yyyy-MM-dd";


    public NetworkOP (){
        help = Helper.getInstance();
        BasicConfigurator.configure();
    }

    public Float fetchClosingPrice(String stockName, SimpleDateFormat time) {
        Float result = 0.0f;
        URL url;

        help.debug(this.getClass().getName(), "fetchClosingPrice(%s,%s)\n",stockName, help.dateToString(time));


        try {
            url = new URL(urlName);
            Document doc = Jsoup.parse(url, 3*1000);

                String title  = doc.title();
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

                        for(int i=0;i<1;i++) {
                            elem = iter.next();
                        }
                        
                        data = elem.text();
                        String fdata = data.replace(',', '.');
                        System.out.printf("Closing Price:%f for:%s\n", Float.valueOf(fdata).floatValue(), help.dateToString(trueDate));
                        return Float.valueOf(fdata);
                    }


                }

                System.out.printf("The host title:%s found:%d\n", title, elems.size());




        } catch (IOException ex) {
            Logger.getLogger(NetworkGoogle.class.getName()).log(Level.SEVERE, null, ex);
        }




        return result;
    }


}
