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

import java.util.ArrayList;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Evgeni Kappinen
 */
public class NordnetConnect {
    private static final String _LOGIN_URL_ = "https://www.nordnet.fi/mux/login/login.html";
    private static final String _PORTFOLIO_URL_ = "https://www.nordnet.fi/mux/web/depa/mindepa/depaoversikt.html";

    
    // Connects to login page, get seeds for user and password
    // POST data to server, by calling NordnetConnector

    public void connectAndAuth(String user, String password) {
        ArrayList <String>inputList = new ArrayList();

        NordnetConnector connector = new NordnetConnector();
        
            String loginPage = connector.getPage(_LOGIN_URL_);
            Document doc = Jsoup.parse(loginPage);
            Elements element  = doc.select("input");

            Iterator <Element>iter = element.iterator();
                while(iter.hasNext()){
                    Element elem =  iter.next();
                    inputList.add(elem.attr("name"));
                }
            
            
            loginPage = connector.authenticate(_LOGIN_URL_,
                            inputList.get(inputList.size() - 2), user,
                            inputList.get(inputList.size() - 1), password);


            loginPage = connector.getPage(_PORTFOLIO_URL_);

            System.out.printf("Data from jsoup:%s last index:%s and :%s\n",
                    loginPage, inputList.get(inputList.size() - 2),
                    inputList.get(inputList.size() - 1));
        
    }


    public void connect() {

        
    }




}
