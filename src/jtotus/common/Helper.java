/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author kappiev
 */
public class Helper {
    private static Helper help = null;
    private int debugLvl = 1;


    protected Helper() {

    }

    public static Helper getInstance() {

        if (help == null) {
            help = new Helper();
        }
        return help;
    }

    public void setDebugLvl(int lvl){
        debugLvl = lvl;
    }


    public synchronized void debug(int lvl, String pattern, Object... arguments) {

        if(lvl >= debugLvl) {
            System.out.printf(pattern, arguments);
        }
    }

    public synchronized String getTimeNow(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("dd:MM:yyyy");

        return date.format(cal.getTime());
    }

    public synchronized String dateReduction(String date, int count){
        String newDate = null;

        String[] parsedDate = date.split(":");
        int intDate = Integer.parseInt(parsedDate[0]);
        intDate -= count;

        newDate = Integer.toString(intDate) + ":" + parsedDate[1] + ":" + parsedDate[2];

        return newDate;
    }

    public synchronized void printCrtDir(){

     File dir1 = new File (".");
     File dir2 = new File ("..");
     try {
       System.out.println ("Current dir : " + dir1.getCanonicalPath());
       System.out.println ("Parent  dir : " + dir2.getCanonicalPath());
       }
     catch(Exception e) {
       e.printStackTrace();
       }
     }





    
}
