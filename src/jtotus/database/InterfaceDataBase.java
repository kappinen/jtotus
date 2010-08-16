/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.text.SimpleDateFormat;

/**
 *
 * @author kappiev
 */
public interface InterfaceDataBase {

     public Float fetchClosingPrice(String stockName, SimpleDateFormat time);

     public Float fetchAveragePrice(String stockName, SimpleDateFormat time);


}
