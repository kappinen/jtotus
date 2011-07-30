/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jtotus.database;

import java.math.BigDecimal;
import org.joda.time.DateTime;

/**
 *
 * @author Evgeni Kappinen
 */
public interface Cache {

    public void putValue(String stockName, DateTime date, BigDecimal value);
    public BigDecimal getValue(String stockName, DateTime date);
}
