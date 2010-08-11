/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;
import java.util.LinkedList;
import jtotus.JtotusApp;
import jtotus.JtotusView;
import jtotus.common.Helper;
import jtotus.threads.*;


/**
 *
 * @author kappiev
 */


public class Engine {

    private Dispatcher dispatcher = null;
    private LinkedList <VoterThread>methodList;
    private Helper help = null;


    private void prepareMethodsList(){
        // Available methods
        methodList.add(new DummyMethod(dispatcher));
        methodList.add(new DummyMethod(dispatcher));

    }



    public Engine(){
        help = Helper.getInstance();
        dispatcher = new Dispatcher();
        methodList = new LinkedList<VoterThread>();

        prepareMethodsList();
        
        
    }


    

    public void run(){



        if(dispatcher.setList(methodList)){
            help.debug(1, "Dispatcher is already full");
            return;
        }
        
        dispatcher.run();
    }

    public void addGUI(JtotusApp mainApp) {
        System.out.printf("The mainWindow woop!\n");
       // JtotusView mainWindow = mainApp.getView();
        //mainWindow.prepareMethodList(methodList);
    }

}
