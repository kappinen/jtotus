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


    public DataFetcher()
    {
        listOfResources = new LinkedList<InterfaceDataBase>();
        //listOfResources.add(new FileSystemFromHex());
        // listOfResources.add(new NetworkGoogle());
        listOfResources.add(new NetworkOP());

    }

    public Float fetchClosingPrice(String sockName, SimpleDateFormat time){
        Float result = 0.0f;


        Iterator <InterfaceDataBase>resources = listOfResources.iterator();
        while(resources.hasNext()){
            InterfaceDataBase res = resources.next();
            
            result = res.fetchClosingPrice(sockName, time);
            if (result != 0.0f){
                return result;
            }

        }



        return result;
    }

}
