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

import java.util.Calendar;
import org.jtotus.common.Helper;
import org.jtotus.common.StockType;

/**
 *
 * @author Evgeni Kappinen
 */
public class AutoUpdateStocks implements Runnable {

    private String stockName = null;
    private LocalJDBC javadb = null;
    private int stepToRemove = 0;

    public AutoUpdateStocks(String tempName) {
        stockName = tempName;

    }

    private int updateClosingPrice(StockType stock, LocalJDBC javadb) {
        int counter = 0;

        Calendar calendar = Calendar.getInstance();

        final int failureLimit = -8;
        final int foundLimit = 8;
        for (int i = 0; (failureLimit < i) && (i < foundLimit); i++) {
            calendar.add(Calendar.DATE, stepToRemove);
            stepToRemove = -1;

            if (javadb.fetchClosingPrice(stockName, calendar) != null) {
                // Found in database
                counter++;
                continue;
            } else {
                calendar.add(Calendar.DATE, stepToRemove);
                if (stock.fetchClosingPrice(calendar) != null) {
                    // Found somewhere in resources..
                    // Database should be updated already.
                    counter = 0;
                    continue;
                } else {
                    // Not found int local database
                    //nor in other resources.
                    // Could be holiday or data simply is not available
                    counter--;
                }
            }



        }


        return 1;
    }

    public void run() {

        LocalJDBCFactory factory = new LocalJDBCFactory().getInstance();
        javadb = factory.jdbcFactory();
        if (stockName == null) {
            System.err.printf("Error autoupdator failure.\n");
            return;
        }

        StockType stock = new StockType(stockName);


        this.updateClosingPrice(stock, javadb);
        return;
    }
}
