/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlucrum.datafetcher;

import java.util.Map;
import org.joda.time.DateTime;

/**
 *
 * @author Evgeni Kappinen
 */
public interface MarketFetcher {
    public Map<String,Double> fetchDataPeriod(String name, DateTime fromDate, DateTime toDate, int type);
    public Double fetchData(String name, DateTime date, int type);
}
