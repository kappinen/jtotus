/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;


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


    public LocalJavaDB() {
        
    }

    public int initialize() {


        try {

             Class.forName(driver);
             conJavaDB = DriverManager.getConnection(connectionUrl[0],
                                                     connectionUrl[1],
                                                     connectionUrl[2]);


        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
            return -2;//Unable to connect to database
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
            return -1; // Cannot load driver
        }

        return 0;
    }




    

    public Float fetchClosingPrice(String stockName, SimpleDateFormat time) {
        Float closingPrice = null;
        
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

            ResultSet results = pstmt.executeQuery();

            while (results.next()) {
                float tmpFloat = results.getFloat("CLOSINGPRICE");
                closingPrice = new Float(tmpFloat);
                break;
            }

            pstmt.clearParameters();
            pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return closingPrice;

    }

    public Float fetchAveragePrice(String stockName, SimpleDateFormat time) {
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
                                  SimpleDateFormat date,
                                  Float value) {

        float closingPrice = value.floatValue();
        
        if (conJavaDB == null) {
            if (initialize() < 0) {
                return;
            }
        }

        try {

            String query = "INSERT INTO "+mainTable+" (STOCKNAME,DATE,CLOSINGPRICE) VALUES (?,?,?)";


            PreparedStatement pstmt = conJavaDB.prepareStatement(query);
  
            pstmt.setString(1, stockName);


            Calendar cal = date.getCalendar();
            java.util.Date searchDay = cal.getTime();


            java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
            pstmt.setDate(2, sqlDate, date.getCalendar());


            pstmt.setFloat(3, closingPrice);


            int result = pstmt.executeUpdate();

            System.out.printf("Stock:%s price:%f res:%d\n", stockName, closingPrice, result);

            pstmt.clearParameters();
            pstmt.close();
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
