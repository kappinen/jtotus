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
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.lang.ArrayUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jtotus.common.DateIterator;

/**
 * @author Evgeni Kappinen
 */
public class LocalJDBC implements InterfaceDataBase {

    private Connection connection = null;
    private JdbcConnectionPool mainPool = null;
    private PreparedStatement createTableStatement = null;
    private PreparedStatement fetchClosePriceStatement = null;
    private PreparedStatement fetchVolumeStatement = null;
    private PreparedStatement insertClosePriceStatement = null;
    private PreparedStatement insertVolumeStatement = null;
    private boolean debug = false;
    private DataFetcher fetcher = null;


    void setConnection(Connection localJDBC) {
        connection = localJDBC;
    }

    //TODO;create procedures
    private void createTable(Connection con, String stockTable) {

        try {

            String statement = "CREATE TABLE IF NOT EXISTS " + stockTable + " ("
                    + "ID IDENTITY AUTO_INCREMENT,"
                    + "DATE          DATE,"
                    + "TIME          TIME,"
                    + "OPEN          DECIMAL(18,4),"
                    + "CLOSE         DECIMAL(18,4),"
                    + "HIGH          DECIMAL(18,4),"
                    + "LOW           DECIMAL(18,4),"
                    + "VOLUME        INT,"
                    + "PRIMARY KEY(ID));";

            createTableStatement = con.prepareStatement(statement);

            createTableStatement.execute();

        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public BigDecimal fetchClosingPrice(String stockName, Calendar date) {

        return this.fetchData(stockName, date, "CLOSE");
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public BigDecimal fetchData(String tableName, Calendar date, String column) {
        BigDecimal retValue = null;
        PreparedStatement pstm = null;

        try {
            String statement = "SELECT " + column + " FROM " + this.normTableName(tableName) + " WHERE DATE=?";
            connection = mainPool.getConnection();
            this.createTable(connection, this.normTableName(tableName));

            pstm = connection.prepareStatement(statement);

            java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());
            pstm.setDate(1, sqlDate);

            //          System.out.printf("Fetching:'%s' from'%s' Time"+date.getTime()+"Stm:%s\n", column,tableName, statement);
            ResultSet results = pstm.executeQuery();

//            System.out.printf("Results:%d :%d :%s (%d)\n",results.getType(), results.findColumn(column), results.getMetaData().getColumnLabel(1),java.sql.Types.DOUBLE);

            if (results.next()) {
                retValue = results.getBigDecimal(column);
            }

        } catch (SQLException ex) {
            System.err.printf("LocalJDBC Unable to find date for:'%s' from'%s' Time" + date.getTime() + "\n", column, tableName);
            //   Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }

                if (connection != null && !connection.isClosed()) {
                    connection.close();
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

    public double[] fetchPeriod(String tableName, Calendar startDate, Calendar endDate) {
        BigDecimal retValue = null;
        PreparedStatement pstm = null;
        java.sql.Date retDate = null;
        ArrayList<Double> closingPrices = new ArrayList<Double>(600);

        try {
            String query = "SELECT CLOSE, DATE FROM " + this.normTableName(tableName) + " WHERE DATE>=? AND DATE<=? ORDER BY DATE ASC";
            connection = mainPool.getConnection();
            // this.createTable(connection, this.normTableName(tableName));

            pstm = connection.prepareStatement(query);

            java.sql.Date startSqlDate = new java.sql.Date(startDate.getTimeInMillis());
            pstm.setDate(1, startSqlDate);

            java.sql.Date endSqlDate = new java.sql.Date(endDate.getTimeInMillis());
            pstm.setDate(2, endSqlDate);

            DateIterator iter = new DateIterator(startDate, endDate);
            ResultSet results = pstm.executeQuery();
            Calendar dateCheck;

            while (results.next()) {
                retValue = results.getBigDecimal(1);
                retDate = results.getDate(2);

                if (retValue == null || retDate == null) {
                    System.err.println("Database is corrupted!");
                    System.exit(-1);
                }

                if (iter.hasNext()) {
                    dateCheck = iter.nextInCalendar();

                    Calendar compCal = Calendar.getInstance();
                    compCal.setTimeInMillis(retDate.getTime());

                    while ((compCal.get(Calendar.DAY_OF_MONTH) != dateCheck.get(Calendar.DAY_OF_MONTH)) ||
                            (compCal.get(Calendar.MONTH) != dateCheck.get(Calendar.MONTH)) ||
                            (compCal.get(Calendar.YEAR) != dateCheck.get(Calendar.YEAR))) {
                        if (fetcher != null) {
                            BigDecimal failOverValue = fetcher.fetchClosingPrice(tableName, dateCheck);
                            if (failOverValue != null) {
                                closingPrices.add(retValue.doubleValue());
                            }

                            if (iter.hasNext()) {
                                System.err.printf("Warning : Miss matching dates for: %s - %s\n", retDate.toString(), dateCheck.toString());
                                dateCheck = iter.nextInCalendar();
                                continue;
                            }
                        } else {
                            System.err.printf("Fatal missing fetcher : Miss matching dates: %s - %s\n", retDate.toString(), dateCheck.toString());
                            return null;
                        }
                    }

                }

                if (debug) {
                    if (retValue != null)
                        System.out.printf("Fetched:\'%s\' from \'%s\' : value:%f date:%s\n",
                                "Closing Price", tableName, retValue.doubleValue(), retDate.toString());
                    else
                        System.out.printf("Fetched:\'%s\' from \'%s\' : value:%s date:%s\n",
                                "Closing Price", tableName, "is null", retDate.toString());
                }
                closingPrices.add(retValue.doubleValue());
            }

            while (iter.hasNext()) {
                retValue = fetcher.fetchClosingPrice(tableName, iter.nextInCalendar());
                if (retValue != null) {
                    closingPrices.add(retValue.doubleValue());
                }
            }

            pstm.close();

        } catch (SQLException ex) {
            System.err.printf("LocalJDBC Unable to find date for:'%s' from'%s' Time" + startDate.getTime() + "\n", "Cosing Price", tableName);
            ex.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return ArrayUtils.toPrimitive(closingPrices.toArray(new Double[0]));
    }


    public BigDecimal fetchVolume(String stockName, Calendar date) {

        return this.fetchData(this.normTableName(stockName), date, "VOLUME");
    }

    public void storeData(String type, String stockName, Calendar date, BigDecimal value) {
        PreparedStatement pstm = null;
        try {

            String table = this.normTableName(stockName);
            connection = mainPool.getConnection();
            //upsert
            this.createTable(connection, table);

            String query = "MERGE INTO " + table + " (ID,DATE," + type + ") VALUES((SELECT ID FROM " + table + " ID WHERE DATE=?), ?, ?)";
            pstm = connection.prepareStatement(query);

            java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());
            pstm.setDate(1, sqlDate);
            pstm.setDate(2, sqlDate);

            System.out.printf("Inserting :%f :%s time:%s\n", value.doubleValue(), stockName, date.getTime().toString());
            pstm.setDouble(3, value.doubleValue());
            pstm.execute();

            connection.close();
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

    }

    private String normTableName(String name) {
        return name.replace(" ", "").replace("-", "");
    }

    public void storeClosingPrice(String stockName, Calendar date, BigDecimal value) {
        this.storeData("CLOSE", stockName, date, value);
    }

    public void storeVolume(String stockName, Calendar date, BigDecimal value) {
        this.storeData("VOLUME", stockName, date, value);
    }

    public long entryExists(Connection con, String stockName, Calendar date) {
        long retValue = 0;
        PreparedStatement pstm = null;
        try {
            String statement = "SELECT ID FROM " + this.normTableName(stockName) + " WHERE DATE=?";

            pstm = con.prepareStatement(statement);

            java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());

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


    void setPool(JdbcConnectionPool pool) {
        mainPool = pool;
    }


}
