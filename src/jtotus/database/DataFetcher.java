/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author kappiev
 */
public class DataFetcher {

    LinkedList<InterfaceDataBase> listOfResources = null;
    LocalJavaDB javadb = null;

    public DataFetcher()
    {
        listOfResources = new LinkedList<InterfaceDataBase>();
        listOfResources.add(new FileSystemFromHex());
        listOfResources.add(new NetworkOP());
        javadb = new LocalJavaDB();
        // listOfResources.add(new NetworkGoogle());

    }

    public Float fetchClosingPrice(String stockName, SimpleDateFormat time){
        Float result = null;

        Iterator <InterfaceDataBase>resources = listOfResources.iterator();


        result = javadb.fetchClosingPrice(stockName, time);
        if(result == null) {
            while(resources.hasNext()){
                InterfaceDataBase res = resources.next();

                result = res.fetchClosingPrice(stockName, time);
                if (result != null) {
                        javadb.storeClosingPrice(stockName, time, result);
                    return result;
                }
            }
        }
        return result;
    }

}
