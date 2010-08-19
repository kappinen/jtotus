/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.graph;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 *
 * @author kappiev
 */
public class GraphPacket implements Serializable{
    private static final long serialVersionUID = 1L;

    
    public String seriesTitle;
    public int day;
    public int month;
    public int year;
    public float result;
}
