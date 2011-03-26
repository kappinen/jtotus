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
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
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

    public BigDecimal fetchData(String tableName, Calendar date, String column) {
        BigDecimal retValue = null;
        PreparedStatement pstm = null;

        try {
            String statement = "SELECT "+column+" FROM "+this.normTableName(tableName)+" WHERE DATE=?";
            connection = mainPool.getConnection();
             this.createTable(connection, this.normTableName(tableName));

            pstm = connection.prepareStatement(statement);
            
            java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());
            pstm.setDate(1, sqlDate);

  //          System.out.printf("Fetching:'%s' from'%s' Time"+date.getTime()+"Stm:%s\n", column,tableName, statement);
            ResultSet results = pstm.executeQuery();

//            System.out.printf("Results:%d :%d :%s (%d)\n",results.getType(), results.findColumn(column), results.getMetaData().getColumnLabel(1),java.sql.Types.DOUBLE);

            results.next();
            
            retValue = results.getBigDecimal(column);

        } catch (SQLException ex) {
            System.err.printf("LocalJDBC Unable to find date for:'%s' from'%s' Time"+date.getTime()+"\n", column,tableName);
             //   Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }


        try {
            if (pstm != null) {
                pstm.close();
            }

            if (!connection.isClosed())
                connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retValue;
    }

    public BigDecimal fetchVolume(String stockName, Calendar date) {
        
        return this.fetchData(this.normTableName(stockName), date, "VOLUME");
    }

    public void storeData(String type, String stockName, Calendar date, BigDecimal value) {
        PreparedStatement pstm=null;
        try {

            String table = this.normTableName(stockName);
            connection = mainPool.getConnection();
            //upsert
            this.createTable(connection, table);

            String query = "MERGE INTO "+table+" (ID,DATE,"+type+") VALUES((SELECT ID FROM "+table+" ID WHERE DATE=?), ?, ?)";
            pstm = connection.prepareStatement(query);

            java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());
            pstm.setDate(1, sqlDate);
            pstm.setDate(2, sqlDate);

            System.out.printf("Inserting :%f :%s time:%s\n", value.doubleValue(), stockName, date.getTime().toString());
            pstm.setDouble(3, value.doubleValue());
            pstm.execute();

            pstm.close();
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String normTableName(String name) {
        return name.replace(" ", "").replace("-", "");
    }

    public void storeClosingPrice(String stockName, Calendar date, BigDecimal value) {
        this.storeData("CLOSE",stockName,date,value);
    }
    public void storeVolume(String stockName, Calendar date, BigDecimal value) {
        this.storeData("VOLUME",stockName,date,value);
    }

    public long entryExists(Connection con, String stockName, Calendar date) {
        try {
            String statement = "SELECT ID FROM " + this.normTableName(stockName) + " WHERE DATE=?";

            PreparedStatement pstm = con.prepareStatement(statement);

            java.sql.Date sqlDate = new java.sql.Date(date.getTimeInMillis());

            pstm.setDate(1, sqlDate);

            ResultSet results = pstm.executeQuery();
            if (results.next()) {
                pstm.close();
                return results.getLong(1);
            }
            
            pstm.close();

        } catch (SQLException ex) {
            Logger.getLogger(LocalJDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }


    void setPool(JdbcConnectionPool pool) {
        mainPool = pool;
    }


}
