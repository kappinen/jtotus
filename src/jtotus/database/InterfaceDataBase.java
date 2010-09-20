/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jtotus.database;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 *
 * @author kappiev
 */
public interface InterfaceDataBase {

    public BigDecimal fetchClosingPrice(String stockName, SimpleDateFormat time);

    public BigDecimal fetchAveragePrice(String stockName, SimpleDateFormat time);
}
