/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.config;

/**
 *
 * @author kappiev
 */
public class GUIConfig {

    public final String []StockNames = { "Fortum Oyj",
                                         "Nokia Oyj",
                                         "UPM-Kymmene Oyj",
                                         "Metso Oyj",
                                         "Kemira Oyj",
                                         "Konecranes Oyj",
                                         "KONE Oyj",
                                         "Rautaruukki Oyj",
                                         "Sanoma Oyj",
                                         "Tieto Oyj",
                                         "Uponor Oyj"
                                          };
    public final int day_period = 5;

    public String []fetchStockName() {
        return StockNames;
    }


}

