/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.common.Helper;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

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
            //HSSFRow row1 = worksheet.getRow(0);


            String correctTime = filterTime(time);
            Iterator rowIter = worksheet.rowIterator();


            while(rowIter.hasNext())
            {
                HSSFRow rows = (HSSFRow)rowIter.next();
                HSSFCell cell = rows.getCell(0);
                String temp = cell.getStringCellValue();
               // help.debug(3,"Searching:%s from:%s\n", correctTime, temp);
                if (correctTime.compareTo(temp) == 0)
                {
                    HSSFCell closingPrice = rows.getCell(3);
                    float floatTemp = (float)closingPrice.getNumericCellValue();
                    help.debug(4, "Found at:%d f:%.4f\n",cell.getRowIndex(), floatTemp);
                    return new Float(floatTemp);
                }
            }

             
        } catch (IOException ex) {
            Logger.getLogger(FileSystemFromHex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }


    private String filterTime(String time)
    {
        String []timeSplit = time.split(":");

        // Year-Mount-Data
        return timeSplit[2]+"-"+timeSplit[1]+"-"+timeSplit[0];
    }

}
