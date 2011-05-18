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
package org.jtotus.database;

import java.sql.*;
import java.sql.Connection;
import java.util.HashMap;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jtotus.common.DateIterator;


/**
 *
 * @author Evgeni Kappinen
 */
public class StockMerger {
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
    private static final LocalJDBC fetcher = new LocalJDBC();
    private static final NetworkGoogle google = new NetworkGoogle();
    private static final boolean debug = true;

    public StockMerger() {
        DataFetcher fetch = new DataFetcher();
        fetcher.setFetcher(fetch);
    }

    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:~/.jtotus/local_database", "sa", "sa");
    }

    public double[][] mergedPeriods(String stockA, String stockB, DateTime startDate, DateTime endDate) {
        int matched = 0;
        HashMap<String, Double> aRet;
        HashMap<String, Double> bRet;
        
        
        aRet = fetcher.fetchPeriodAsMap(stockA, startDate, endDate);
        if (aRet.isEmpty()) {
            aRet = google.fetchPeriodAsMap(stockA, startDate, endDate);
        }
        
        bRet = fetcher.fetchPeriodAsMap(stockB, startDate, endDate);
        if (bRet.isEmpty()) {
            bRet = google.fetchPeriodAsMap(stockB, startDate, endDate);
        }
        
        int size = Math.min(aRet.size(), bRet.size());
        double retMatrix[][] = new double[2][size];
        
        DateIterator iter = new DateIterator(startDate, endDate);
        while(iter.hasNext()) {
            DateTime date = iter.nextInCalendar();
            Double aValue = aRet.get(formatter.print(date));
            Double bValue = bRet.get(formatter.print(date));
            if (aValue != null && bValue != null) {
                retMatrix[0][matched] = aValue;
                retMatrix[1][matched] = bValue;
                matched++;
            }
        }
        
        double ret[][] = new double[2][matched];

        System.arraycopy(retMatrix[0], 0, ret[0], 0, matched);
        System.arraycopy(retMatrix[1], 0, ret[1], 0, matched);
        return ret;
    }

}
