/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2009 Yan Cheng CHEOK <yccheok@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


package org.jtotus.database;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.util.Calendar;
import org.jtotus.common.Helper;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;


/**
 *
 * @author Evgeni Kappinen
 */
public class FileSystemFromHex implements InterfaceDataBase {

    String pathToDataBaseDir = "OMXNordic/";
    String filePattern = "yyyy-MM-dd";
    Helper help = null;


    private int columnHighestPrice = 1;
    private int columnLowestPrice = 2;
    private int columnClosingPrice = 3;
    private int columnAvrPrice = 4;
    private int columnTotalVolume = 5;
    private int columnTurnOver = 6;
    private int columnTrades = 7;


    //TODO:find column* values by reading first line in file,
    // if contains string which indicates value type change value.

    public FileSystemFromHex(){
        help = Helper.getInstance();
    }


    private FileFilter filterForDir()
    {
       FileFilter fileFilter = new FileFilter() {
           public boolean accept(File file)
           {
                if(!file.isFile() || !file.canRead()) {
                    return false;
                }

                String name = file.getName();
                if (!name.endsWith(".xls"))
                {
                    return false;
                }
               return true;
           }
       };
       return fileFilter;
    }

    
    
public BigDecimal fetchHighestPrice(String stockName, Calendar calendar){
    return this.fetchValue(stockName, calendar, columnHighestPrice);
}

public BigDecimal fetchLowestPrice(String stockName, Calendar calendar){
    return this.fetchValue(stockName, calendar, columnLowestPrice);
}

public BigDecimal fetchClosingPrice(String stockName, Calendar calendar){
    return this.fetchValue(stockName, calendar, columnClosingPrice);
}

public BigDecimal fetchAveragePrice(String stockName, Calendar calendar){
    return this.fetchValue(stockName, calendar, columnAvrPrice);
}

public BigDecimal fetchTurnOver(String stockName, Calendar calendar){
    return this.fetchValue(stockName, calendar, columnTurnOver);
}

public BigDecimal fetchTrades(String stockName, Calendar calendar){
    return this.fetchValue(stockName, calendar, columnTrades);
}


private BigDecimal fetchValue(String stockName, Calendar date, int row)
{
    BigDecimal result = null;
    
    System.out.printf("Reading file system !:%d time:%s\n", row, date.getTime().toString());

    File dir = new File("./" + pathToDataBaseDir);
    FileFilter filter = filterForDir();

    File[] listOfFiles = dir.listFiles(filter);


    for (int i = 0; i < listOfFiles.length ; i++) {
        String nameOfFile = listOfFiles[i].getName();

        if (nameOfFile.indexOf(stockName) != -1) {
            help.debug("FileSystemFromHex","Found File:%s\n", nameOfFile);
            result = this.omxNordicFile(nameOfFile, date, row);
            if (result != null) {
               return result;
            }
        }
    }
    help.debug("FileSystemFromHex", "Not found value for:%s\n", stockName);
    return result;
}





    public BigDecimal omxNordicFile(String fileName, Calendar calendar,int row) {
        BigDecimal result = null;

        try {

            POIFSFileSystem fs = new POIFSFileSystem(
                new FileInputStream(pathToDataBaseDir+fileName));

            HSSFWorkbook workbook = new HSSFWorkbook(fs);

            HSSFSheet worksheet = workbook.getSheetAt(0);
            //HSSFRow row1 = worksheet.getRow(0);

            // Year-Mount-Data
            SimpleDateFormat time =  new SimpleDateFormat(filePattern);
            time.setCalendar(calendar);

            //System.out.printf("Class :%s : %s\n",this.getClass().toString(), this.toString());
            String correctTime = help.dateToString(time);
            Iterator rowIter = worksheet.rowIterator();

            while(rowIter.hasNext())
            {
                HSSFRow rows = (HSSFRow)rowIter.next();
                HSSFCell cell = rows.getCell(0);
                String dateString = null;
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    dateString = cell.getStringCellValue();
                } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    dateString = format.format(date);
                    
//                    System.err.printf("File (%s) is corrucped ? type:%s\n", fileName, dateString);
                    
                } else {
                    System.err.printf("File (%s) is corrucped ? type:%d formula:%d\n", fileName, cell.getCellType(), Cell.CELL_TYPE_FORMULA);
                    return null;
                }
              

              //  help.debug("FileSystemFromHex","Searching:%s from:%s\n", correctTime, temp);
                if (correctTime.compareTo(dateString) == 0)
                {
                    HSSFCell closingPrice = rows.getCell(row);
                    if (closingPrice == null)
                        return null;
                    
                    float floatTemp = (float)closingPrice.getNumericCellValue();
                    help.debug("FileSystemFromHex",
                            "Closing price at:%d f:%.4f Time:%s\n",
                            cell.getRowIndex(), floatTemp, correctTime);
                    
                    return new BigDecimal(floatTemp);
                }
            }

             
        } catch (IOException ex) {
            Logger.getLogger(FileSystemFromHex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public BigDecimal fetchVolume(String stockName, Calendar calendar) {
        return this.fetchValue(stockName, calendar, columnTotalVolume);
    }

}
