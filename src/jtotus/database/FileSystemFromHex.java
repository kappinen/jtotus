/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.common.Helper;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 * @author kappiev
 */
public class FileSystemFromHex implements InterfaceDataBase {

    String pathToDataBaseDir = "stock_data/";
    Helper help = null;


    public FileSystemFromHex(){
        help = Helper.getInstance();
    }




    public Float fetchPrice(String stockName, String time) {
        Float result = 0.0f;


        try {
            help.printCrtDir();


            POIFSFileSystem fs = new POIFSFileSystem(
                new FileInputStream(pathToDataBaseDir+"Fortum_1_1_1990_12_8_2010.xls"));

            HSSFWorkbook workbook = new HSSFWorkbook(fs);

            HSSFSheet worksheet = workbook.getSheetAt(0);

            HSSFRow row1 = worksheet.getRow(0);
            

            HSSFCell cellA1 = row1.getCell(0);
            String a1Val = cellA1.getStringCellValue();
            
            HSSFCell cellB1 = row1.getCell(1);
            String b1Val = cellB1.getStringCellValue();

 
    
     

            System.out.println("A1: " + a1Val);
            System.out.println("B1: " + b1Val);


             
        } catch (IOException ex) {
            Logger.getLogger(FileSystemFromHex.class.getName()).log(Level.SEVERE, null, ex);
        } 
       

        

        return result;
    }


}
