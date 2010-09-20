/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.common.Helper;


/**
 *
 * @author kappiev
 */
public class LocalJavaDB implements InterfaceDataBase {

    private Connection conJavaDB = null;
    private String driver = "org.apache.derby.jdbc.ClientDriver";
    private String []connectionUrl = { "jdbc:derby://localhost:1527/OMXHelsinki",
                                       "hex", "hex"};
    public String mainTable = "APP.OMXHELSINKI";
    private Helper help = Helper.getInstance();


    public LocalJavaDB() {
        
    }

    public int initialize() {


        try {

             Class.forName(driver);
             conJavaDB = DriverManager.getConnection(connectionUrl[0],
                                                     connectionUrl[1],
                                                     connectionUrl[2]);


        } catch (SQLException ex) {
           // Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
           System.err.printf("Unable to connecto to JavaDB !\n");
            return -2;//Unable to connect to database
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
            return -1; // Cannot load driver
        }

        return 0;
    }




    

    public BigDecimal fetchClosingPrice(String stockName, SimpleDateFormat time) {
        BigDecimal closingPrice = null;
        
        try {
            if (conJavaDB == null) {
                if (initialize() < 0) {
                    return closingPrice;
                }
            }


            String query = "SELECT * FROM "+mainTable+" WHERE STOCKNAME=? AND DATE=?";
            PreparedStatement pstmt = conJavaDB.prepareStatement(query);
            pstmt.setString(1, stockName);

            Calendar cal = time.getCalendar();
            java.util.Date searchDay = cal.getTime();

            java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
            pstmt.setDate(2, sqlDate, time.getCalendar());

            help.debug(this.getClass().getName(), "Query:%s Stock:%s\n", query, stockName   );
            //Perform query
            ResultSet results = pstmt.executeQuery();

            while (results.next()) {
                BigDecimal tmpBigDecimal = results.getBigDecimal("CLOSINGPRICE");
                help.debug(this.getClass().getName(), "Javadb got closing price %f for %s\n", tmpBigDecimal, stockName);
                closingPrice = tmpBigDecimal;
                break;
            }

            pstmt.clearParameters();
            pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return closingPrice;

    }

    public BigDecimal fetchAveragePrice(String stockName, SimpleDateFormat time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stockNameExists(String stockName, SimpleDateFormat time){

      boolean result = false;

        try {
            if (conJavaDB == null) {
                if (initialize() < 0) {
                    return false;
                }
            }


            String query = "SELECT * FROM "+mainTable+" WHERE STOCKNAME=? AND DATE=?";
            PreparedStatement pstmt = conJavaDB.prepareStatement(query);
            pstmt.setString(1, stockName);

            Calendar cal = time.getCalendar();
            java.util.Date searchDay = cal.getTime();


            java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
            pstmt.setDate(2, sqlDate, time.getCalendar());

            ResultSet results = pstmt.executeQuery();
            if (results.next()) {
                result=true;
            }
            pstmt.clearParameters();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }

      return result;
    }

    //FIXME:add if failed store values to file in other database
    public void storeClosingPrice(String stockName, 
                                  SimpleDateFormat time,
                                  BigDecimal value) {
        
        if (conJavaDB == null) {
            if (initialize() < 0) {
                return;
            }
        }

        try {

            if (stockNameExists(stockName, time)){
                String query = "UPDATE "+mainTable+" SET STOCKNAME=? WHERE STOCKNAME=? AND DATE=?";
                PreparedStatement pstmt = conJavaDB.prepareStatement(query);

                pstmt.setBigDecimal(1, value);
                pstmt.setString(2, stockName);

                Calendar cal = time.getCalendar();
                java.util.Date searchDay = cal.getTime();
                java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
                pstmt.setDate(3, sqlDate, time.getCalendar());

            }else {
                String query = "INSERT INTO "+mainTable+" (STOCKNAME,DATE,CLOSINGPRICE) VALUES (?,?,?)";


                PreparedStatement pstmt = conJavaDB.prepareStatement(query);

                pstmt.setString(1, stockName);


                Calendar cal = time.getCalendar();
                java.util.Date searchDay = cal.getTime();


                java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
                pstmt.setDate(2, sqlDate, time.getCalendar());


                pstmt.setBigDecimal(3, value);

                int result = pstmt.executeUpdate();

                System.out.printf("Stock:%s price:"+value+" res:%d\n", stockName, result);

                pstmt.clearParameters();
                pstmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ;

    }





    /*
     * CREATE table APP.OMXHELSIKI (
                            ID          INTEGER NOT NULL
                                        PRIMARY KEY GENERATED ALWAYS AS IDENTITY
                                        (START WITH 1, INCREMENT BY 1),
                            STOCKNAME    VARCHAR(40) NOT NULL,
                            DATE         DATE,
                            CLOSINGPRICE REAL,
                            TRADES       BIGINT,
                            AVRPRICE     REAL,
                            VOLUME       BIGINT)
     */
    
    public boolean createTable(){
        boolean bCreatedTables = false;
        Statement statement = null;
        String query = "CREATE table APP.OMXHELSIKI ("
                + "ID          INTEGER NOT NULL "
                + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1),"
                + "STOCKNAME    VARCHAR(40) NOT NULL,"
                + "DATE         DATE,"
                + "CLOSINGPRICE REAL,"
                + "TRADES       BIGINT,"
                + "AVRPRICE     REAL,"
                + "VOLUME       BIGINT)";


        try {
            statement = conJavaDB.createStatement();
            statement.execute(query);

            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return bCreatedTables;
    }



}
