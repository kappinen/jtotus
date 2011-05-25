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

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.DateFormatter;
import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jtotus.common.DateIterator;

/**
 * @author Evgeni Kappinen
 */
public class LocalJDBC implements InterfaceDataBase {
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
    private PreparedStatement fetchClosePriceStatement = null;
    private PreparedStatement fetchVolumeStatement = null;
    private PreparedStatement insertClosePriceStatement = null;
    private PreparedStatement insertVolumeStatement = null;
    private boolean debug = false;
    private DataFetcher fetcher = null;


    public static enum DataTypes {
        CLOSE,
        VOLUME
    };
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:~/.jtotus/local_database;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0",
                "sa", "sa");
    }

    //TODO;create procedures
    private void createTable(Connection con, String stockTable) {
        PreparedStatement createTableStatement = null;
        try {

            String statement = "CREATE TABLE IF NOT EXISTS " + stockTable + " ("
                    + "ID IDENTITY AUTO_INCREMENT,"
                    + "DATE          DATE,"
                    + "TIME          TIME,"
                    + "OPEN          DECIMAL(18,4),"
                    + "CLOSE         DECIMAL(18,4),"
                    + "HIGH          DECIMAL(18,4),"
                    + "LOW           DECIMAL(18,4),"
//                    + "AVRG           DECIMAL(18,4),"
//                    + "TRADES           DECIMAL(18,4),"
                    + "VOLUME        INT,"
                    + "PRIMARY KEY(ID));";

            createTableStatement = con.prepareStatement(statement);

            createTableStatement.execute();

        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BigDecimal fetchClosingPrice(String stockName, DateTime date) {
        return this.fetchData(stockName, date, "CLOSE");
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public BigDecimal fetchData(String tableName, DateTime date, String column) {
        BigDecimal retValue = null;
        PreparedStatement pstm = null;
        Connection connection = null;
        ResultSet results = null;


        try {
            connection = getConnection();
            String statement = "SELECT " + column + " FROM " + this.normTableName(tableName) + " WHERE DATE=?";
            
            this.createTable(connection, this.normTableName(tableName));

            pstm = connection.prepareStatement(statement);

            java.sql.Date sqlDate = new java.sql.Date(date.getMillis());
            pstm.setDate(1, sqlDate);

            if (debug) {
                System.out.printf("Fetching:'%s' from'%s' Time"+date.toDate()+"Stm:%s\n", column,tableName, statement);
            }
            
            results = pstm.executeQuery();

//            System.out.printf("Results:%d :%d :%s (%d)\n",results.getType(), results.findColumn(column), results.getMetaData().getColumnLabel(1),java.sql.Types.DOUBLE);

            if (results.next()) {
                retValue = results.getBigDecimal(column);
            }

        } catch (SQLException ex) {
            System.err.printf("LocalJDBC Unable to find date for:'%s' from'%s' Time" + date.toDate() + "\n", column, tableName);
            //   Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (results != null) {
                    results.close(); results = null;
                }

                if (pstm != null) {
                    pstm.close(); pstm = null;
                }
                
                if (connection != null) {
                    connection.close(); connection = null;
                }
            } catch (SQLException ex) {
                Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retValue;
    }

    public void setFetcher(DataFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public DataFetcher getFetcher() {
        if (this.fetcher == null) {
            this.fetcher = new DataFetcher();
        }

        return this.fetcher;
    }

    public double[] fetchPeriod(String tableName, DateTime startDate, DateTime endDate, String type) {
        BigDecimal retValue = null;
        PreparedStatement pstm = null;
        java.sql.Date retDate = null;
        ResultSet results = null;
        ArrayList<Double> closingPrices = new ArrayList<Double>(600);
        Connection connection = null;

        try {
            String query = "SELECT "+type+", DATE FROM " + this.normTableName(tableName) + " WHERE DATE>=? AND DATE<=? ORDER BY DATE ASC";
            // this.createTable(connection, this.normTableName(tableName));

            connection = this.getConnection();
            pstm = connection.prepareStatement(query);

            java.sql.Date startSqlDate = new java.sql.Date(startDate.getMillis());
            pstm.setDate(1, startSqlDate);

            java.sql.Date endSqlDate = new java.sql.Date(endDate.getMillis());
            pstm.setDate(2, endSqlDate);

            DateIterator iter = new DateIterator(startDate, endDate);
            results = pstm.executeQuery();
            DateTime dateCheck;

            while (results.next()) {
                retValue = results.getBigDecimal(1);
                retDate = results.getDate(2);

                if (retDate == null) {
                    System.err.println("Database is corrupted!");
                    System.exit(-1);
                } else if (retValue == null) {
//                    if (type.equals("CLOSE")) {
//                        retValue = fetcher.fetchClosingPrice(tableName, new DateTime(retDate.getTime()));
//                    } else if (type.equals("VOLUME")) {
//                        retValue = fetcher.fetchVolumeForDate(tableName, new DateTime(retDate.getTime()));
//                    }
                    retValue = getFetcher().fetchData(tableName, new DateTime(retDate.getTime()), type);
                    if (retValue == null) {
                        System.err.println("Unable to find " + type + " from databases for " + tableName + " and date " + retDate);
                        return null;
                    }
                }

                if (iter.hasNext()) {
                    dateCheck = iter.nextInCalendar();

                    DateTime compCal = new DateTime(retDate.getTime());
                    if (compCal.getDayOfMonth() == dateCheck.getDayOfMonth()
                            && compCal.getMonthOfYear() == dateCheck.getMonthOfYear()
                            && compCal.getYear() == dateCheck.getYear()) {
                        closingPrices.add(retValue.doubleValue());
                        continue;
                    }

                    while (((compCal.getDayOfMonth() != dateCheck.getDayOfMonth())
                            || (compCal.getMonthOfYear() != dateCheck.getMonthOfYear())
                            || (compCal.getYear() != dateCheck.getYear()))
                            && dateCheck.isBefore(compCal)) {

                        if (fetcher != null) {
                            BigDecimal failOverValue = getFetcher().fetchData(tableName, dateCheck, type);
                            if (failOverValue != null) {
                                closingPrices.add(retValue.doubleValue());
                            }

                            if (iter.hasNext()) {
                                System.err.printf("Warning : Miss matching dates for: %s - %s\n",
                                                  retDate.toString(), formatter.print(dateCheck));
                                dateCheck = iter.nextInCalendar();
                                continue;
                            }
                        } else {
                            System.err.printf("Fatal missing fetcher : Miss matching dates: %s - %s\n",
                                              retDate.toString(), formatter.print(dateCheck));
                            return null;
                        }
                    }

                }

                if (debug) {
                    if (retValue != null) {
                        System.out.printf("Fetched:\'%s\' from \'%s\' : value:%f date:%s\n",
                                "Closing Price", tableName, retValue.doubleValue(), retDate.toString());
                    } else {
                        System.out.printf("Fetched:\'%s\' from \'%s\' : value:%s date:%s\n",
                                "Closing Price", tableName, "is null", retDate.toString());
                    }
                }

            }

            while (iter.hasNext()) {
                retValue = getFetcher().fetchData(tableName, iter.nextInCalendar(), type);
                if (retValue != null) {
                    closingPrices.add(retValue.doubleValue());
                }
            }



        } catch (SQLException ex) {
            System.err.printf("LocalJDBC Unable to find date for:'%s' from'%s' Time" + startDate.toDate() + "\n", "Cosing Price", tableName);
            ex.printStackTrace();
            SQLException xp = null;
            while((xp = ex.getNextException()) != null) {
                xp.printStackTrace();
            }

        } finally {
            try {
                if (results != null) results.close();
                if (pstm != null) pstm.close();
                if (connection != null) connection.close();
//                System.out.printf("Max connect:%d in use:%d\n",mainPool.getMaxConnections(), mainPool.getActiveConnections());
//                mainPool.dispose();

            } catch (SQLException ex) {
                Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));
    }


    public BigDecimal fetchVolume(String stockName, DateTime date) {

        return this.fetchData(this.normTableName(stockName), date, "VOLUME");
    }

    public void storeData(String stockName, DateTime date, BigDecimal value, String type) {
        PreparedStatement pstm = null;
        Connection connection = null;
        try {

            String table = this.normTableName(stockName);
            connection = this.getConnection();
            //upsert
            this.createTable(connection, table);

            String query = "MERGE INTO " + table + " (ID,DATE," + type + ") VALUES((SELECT ID FROM " + table + " ID WHERE DATE=?), ?, ?)";
            pstm = connection.prepareStatement(query);

            java.sql.Date sqlDate = new java.sql.Date(date.getMillis());
            pstm.setDate(1, sqlDate);
            pstm.setDate(2, sqlDate);

            System.out.printf("Inserting :%f :%s time:%s\n", value.doubleValue(), stockName, date.toDate().toString());
            pstm.setDouble(3, value.doubleValue());
            pstm.execute();

        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public HashMap<String, Double> fetchPeriodAsMap(String tableName, DateTime startDate, DateTime endDate) {

        HashMap<String, Double> retMap = new HashMap<String, Double>();
        BigDecimal retValue = null;
        PreparedStatement pstm = null;
        java.sql.Date retDate = null;
        ResultSet results = null;
        Connection connection = null;

        try {
            String query = "SELECT CLOSE, DATE FROM " + this.normTableName(tableName) + " WHERE DATE>=? AND DATE<=? ORDER BY DATE ASC";
            // this.createTable(connection, this.normTableName(tableName));

            connection = this.getConnection();
            pstm = connection.prepareStatement(query);

            java.sql.Date startSqlDate = new java.sql.Date(startDate.getMillis());
            pstm.setDate(1, startSqlDate);

            java.sql.Date endSqlDate = new java.sql.Date(endDate.getMillis());
            pstm.setDate(2, endSqlDate);

            System.out.printf("fetchPeriod : %s : %s\n", startSqlDate, endSqlDate);
            DateIterator iter = new DateIterator(startDate, endDate);
            results = pstm.executeQuery();
            DateTime dateCheck;

            while (results.next()) {
                retValue = results.getBigDecimal(1);
                retDate = results.getDate(2);

                if (retValue == null || retDate == null) {
                    System.err.println("Database is corrupted!");
                    System.exit(-1);
                }

                if (iter.hasNext()) {
                    dateCheck = iter.nextInCalendar();

                    DateTime compCal = new DateTime(retDate.getTime());

                    if (debug) {
                        if (retValue != null) {
                            System.out.printf("Fetched:\'%s\' from \'%s\' : value:%f date:%s\n",
                                    "Closing Price", tableName, retValue.doubleValue(), retDate.toString());
                        } else {
                            System.out.printf("Fetched:\'%s\' from \'%s\' : value:%s date:%s\n",
                                    "Closing Price", tableName, "is null", retDate.toString());
                        }
                    }

                    if (compCal.getDayOfMonth() == dateCheck.getDayOfMonth()
                            && compCal.getMonthOfYear() == dateCheck.getMonthOfYear()
                            && compCal.getYear() == dateCheck.getYear()) {
                        retMap.put(formatter.print(compCal), retValue.doubleValue());
                        continue;
                    }
                    
                    while (((compCal.getDayOfMonth() != dateCheck.getDayOfMonth())
                            || (compCal.getMonthOfYear() != dateCheck.getMonthOfYear())
                            || (compCal.getYear() != dateCheck.getYear())) &&
                            dateCheck.isBefore(compCal)) {
                        if (fetcher != null) {
                            BigDecimal failOverValue = getFetcher().fetchClosingPrice(tableName, dateCheck);
                            if (failOverValue != null) {
                                retMap.put(formatter.print(dateCheck), retValue.doubleValue());
                            }

                            if (iter.hasNext()) {
                                System.err.printf("Warning : Miss matching dates for: %s - %s\n",
                                        retDate.toString(), dateCheck.toString());
                                dateCheck = iter.nextInCalendar();
                                continue;
                            }
                        } else {
                            System.err.printf("Fatal missing fetcher : Miss matching dates: %s - %s\n", retDate.toString(), dateCheck.toString());
                            return null;
                        }
                    }
                }
            }

            while (iter.hasNext()) {
                retValue = getFetcher().fetchClosingPrice(tableName, iter.nextInCalendar());
                if (retValue != null) {
                    retMap.put(formatter.print(iter.getCurrentAsCalendar()), retValue.doubleValue());
                }
            }



        } catch (SQLException ex) {
            System.err.printf("LocalJDBC Unable to find date for:'%s' from'%s' Time" + startDate.toDate() + "\n", "Cosing Price", tableName);
//            ex.printStackTrace();
//            SQLException xp = null;
//            while((xp = ex.getNextException()) != null) {
//                xp.printStackTrace();
//            }

        } finally {
            try {
                if (results != null) results.close();
                if (pstm != null) pstm.close();
                if (connection != null) connection.close();
//                System.out.printf("Max connect:%d in use:%d\n",mainPool.getMaxConnections(), mainPool.getActiveConnections());
//                mainPool.dispose();

            } catch (SQLException ex) {
                Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retMap;
    }
    
    
    
    
    public static String normTableName(String name) {
        return name.replace(" ", "").replace("-", "");
    }

    public void storeClosingPrice(String stockName, DateTime date, BigDecimal value) {
        this.storeData(stockName, date, value, "CLOSE");
    }

    public void storeVolume(String stockName, DateTime date, BigDecimal value) {
        this.storeData(stockName, date, value, "VOLUME");
    }

    public long entryExists(Connection con, String stockName, DateTime date) {
        long retValue = 0;
        PreparedStatement pstm = null;
        try {
            String statement = "SELECT ID FROM " + this.normTableName(stockName) + " WHERE DATE=?";

            pstm = con.prepareStatement(statement);

            java.sql.Date sqlDate = new java.sql.Date(date.getMillis());

            pstm.setDate(1, sqlDate);

            ResultSet results = pstm.executeQuery();
            if (results.next()) {
                retValue = results.getLong(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException ex) {
                    Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return retValue;
    }
}
