/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

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
        listOfResources.add(new FileSystemFromHex());

    }

    public Float fetchPrice(String sockName, String time){
        Float result = 0.0f;


        Iterator resources = listOfResources.iterator();
        while(resources.hasNext()){
            InterfaceDataBase res = (InterfaceDataBase) resources.next();
            
            result = res.fetchPrice(sockName, time);
            if (result != 0.0f){
                return result;
            }

        }



        return result;
    }

}
