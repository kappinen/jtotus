/*
 *
 * Tells Methods what to do, for example which stocks
 * to evaluate and how. General porpose Configuration.
 * 
 */

package jtotus.config;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author kappiev
 */
public class MethodConfig {

    public final String []StockNames = { "Fortum Oyj",
                                         "Nokia Oyj",
                                         "UPM-Kymmene Oyj",
                                         "Metso Oyj",
                                         "Kemira Oyj",
                                         "Konecranes Oyj",
                                         "KONE Oyj",
                                         "Rautaruukki Oy",
                                         "Sanoma Oyj"
                                          };
    public final int day_period = 5;





    public String []fetchStockName() {
        return StockNames;
    }

    public Date getStartTime() {
        Calendar calen = Calendar.getInstance();
        calen.set(2005, 8 -1, 30);
        return calen.getTime();
    }

    public Date getEndTime() {
        Calendar calen = Calendar.getInstance();
        return calen.getTime();
    }
    

}
