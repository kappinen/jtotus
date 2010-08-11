/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.common;

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


    public synchronized  void debug(int lvl, String pattern, Object... arguments) {

        if(lvl >= debugLvl) {
            System.out.printf(pattern, arguments);
        }
    }

    
}
