/*
    This file is part of jTotus.

    jTotus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jTotus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
*/



package org.jtotus.engine;

import org.jtotus.methods.MethodEntry;
import org.jtotus.methods.DecisionScript;
import org.jtotus.methods.DummyMethod;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.gui.JtotusView;
import org.jtotus.common.Helper;
import org.jtotus.common.StateIterator;
import org.jtotus.config.MethodConfig;
import org.jtotus.database.AutoUpdateStocks;
import org.jtotus.gui.MethodResultsPrinter;
import org.jtotus.methods.PotentialWithIn;
import org.jtotus.methods.TaLibEMA;
import org.jtotus.methods.TaLibMOM;
import org.jtotus.methods.TaLibRSI;
import org.jtotus.config.ConfTaLibRSI;
import org.jtotus.methods.TaLibRSI;
import org.jtotus.methods.TaLibSMA;
import org.jtotus.threads.*;



/**
 *
 * @author kappiev
 */


public class Engine {
    private static Engine singleton = null;
    private PortfolioDecision portfolioDecision = null;
    private LinkedList <MethodEntry>methodList;
    private Helper help = null;
    private JtotusView mainWindow = null;
    private HashMap <String,Integer> graphAccessPoints = null;
    private MethodResultsPrinter resultsPrinter = null;



    private void prepareMethodsList(){
        // Available methods
        methodList.add(new DummyMethod(portfolioDecision));
        methodList.add(new PotentialWithIn());
        methodList.add(new TaLibRSI());
        methodList.add(new TaLibSMA());
        methodList.add(new TaLibEMA());
        methodList.add(new TaLibMOM());

        File scriptDir = new File("./src/org/jtotus/methods/scripts/");
        if(!scriptDir.isDirectory()) {
            return;
        }
        
        FileFilter filter = fileIsGroovyScript();
        File[] listOfFiles = scriptDir.listFiles(filter);

        for ( File tmp : listOfFiles) {
            try {
                methodList.add(new DecisionScript(tmp.getCanonicalPath()));
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }



    protected Engine(){
        help = Helper.getInstance();
        portfolioDecision = new PortfolioDecision();
        
        graphAccessPoints = new HashMap<String,Integer>();
        methodList = new LinkedList<MethodEntry>();

        
         
        prepareMethodsList();
    }

    public static Engine getInstance() {

        if (singleton == null) {
            singleton = new Engine();
        }

        return singleton;
    }


   public void setGUI(JtotusView tempView) {
        mainWindow = tempView;
        mainWindow.initialize();
    }

   public synchronized LinkedList<MethodEntry>getMethods() {
       return methodList;
   }

    public void run(){
        
        if(portfolioDecision.setList(methodList)){
            help.debug(1, "Dispatcher is already full");
            return;
        }

        MethodConfig config = new MethodConfig();

        //Auto-update stock values
        String[] stocks = config.StockNames;
        for (int i = stocks.length - 1; i >= 0; i--) {
            Thread updateThread = new Thread(new AutoUpdateStocks(stocks[i]));
            updateThread.start();
        }

        
        testRun();
    }

    private void testRun() {

//        StateIterator iter = new StateIterator();
//        iter.addParam("Param1", "int[2-5]");
//        iter.addParam("Param2", "int[6-8]");
//        while(iter.hasNext() != ) {
//            System.out.printf("Param1:%d Param2: %d\n", iter.nextInt("Param1"), iter.nextInt("Param2"));
//        }
    }

    public void train(){

        LinkedList<String>methodNames = mainWindow.getMethodList();


        LinkedList <MethodEntry>methodL = (LinkedList<MethodEntry>) methodList.clone();
        Iterator <MethodEntry>methodIter = methodL.iterator();
        boolean found = false;


        while(methodIter.hasNext())
        {
            Iterator <String>nameIter = methodNames.iterator();
            MethodEntry methName = methodIter.next();
            String tempName = methName.getMethName();
            
            while(nameIter.hasNext()){
                String nameList = nameIter.next();
                help.debug("Engine",
                           "Search name:%s in list:%s\n",tempName, nameList);
                
                if(nameList.compareTo(tempName)==0){
                    found=true;
                    break;
                }
            }
            if (!found){
               help.debug("Engine","Removeing:%s\n",tempName);
                methodIter.remove();
            }
            found=false;
        }

        if(portfolioDecision.setList(methodL)){
            help.debug(1, "Dispatcher is already full");
            return;
        }

        Thread portThread = new Thread(portfolioDecision);
        portThread.start();
        
    }



    private FileFilter fileIsGroovyScript()
    {
       FileFilter fileFilter = new FileFilter() {
           public boolean accept(File file)
           {
                if(!file.isFile() || !file.canRead()) {
                    return false;
                }

                String name = file.getName();
                if (!name.endsWith(".groovy"))
                {
                    return false;
                }
               return true;
           }
       };
       return fileFilter;
    }


    public void registerGraph(String reviewTarget, int acceccPoint){

        if(graphAccessPoints.containsKey(reviewTarget)) {
             System.err.printf("Warning BUG SHOULD NO HAPPEND!!\n");
            //FIXME:what to do when..
            return;
        }

        //Register access port for messages
        graphAccessPoints.put(reviewTarget, new Integer(acceccPoint));

    }


    public void registerResultsPrinter(MethodResultsPrinter printer) {
        System.out.printf("Registering result printer\n");
        resultsPrinter = printer;
        
        return;
    }

    public synchronized MethodResultsPrinter getResultsPrinter() {
        return resultsPrinter;
    }

    public synchronized int fetchGraph(String reviewTarget) {
        int accessPort = 0;
        Integer tmp = graphAccessPoints.get(reviewTarget);
        if (tmp==null) {
            //FIXME: create new internal frame
            accessPort = mainWindow.createIntFrame(reviewTarget);
            if (accessPort <= 0) {
                return accessPort;
            }
            
            registerGraph(reviewTarget, accessPort);
            return accessPort;
        }
        return tmp.intValue();
    }

}
