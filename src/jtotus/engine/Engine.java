/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.JtotusView;
import jtotus.config.ConfigLoader;
import jtotus.common.Helper;
import jtotus.config.MethodConfig;
import jtotus.database.AutoUpdateStocks;
import jtotus.database.DataFetcher;
import jtotus.graph.GraphPacket;
import jtotus.graph.GraphSender;
import jtotus.threads.*;



/**
 *
 * @author kappiev
 */


public class Engine {
    private static Engine singleton = null;
    private Dispatcher dispatcher = null;
    private LinkedList <VoterThread>methodList;
    private Helper help = null;
    private JtotusView mainWindow = null;
    private HashMap <String,Integer> graphAccessPoints = null;




    private void prepareMethodsList(){
        // Available methods
        methodList.add(new DummyMethod(dispatcher));

        File scriptDir = new File("./src/jtotus/rulebase/");
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
        dispatcher = new Dispatcher();
        graphAccessPoints = new HashMap<String,Integer>();
        methodList = new LinkedList<VoterThread>();

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
        mainWindow.prepareMethodList(methodList);
    }
    

    public void run(){

        if(dispatcher.setList(methodList)){
            help.debug(1, "Dispatcher is already full");
            return;
        }

        MethodConfig config = new MethodConfig();

        String[] stocks = config.StockNames;
        for (int i = stocks.length - 1; i >= 0; i--) {
            Thread updateThread = new Thread(new AutoUpdateStocks(stocks[i]));
            updateThread.start();
        }

        testRun();
       // dispatcher.run();
    }

    private void testRun() {
        PotentialWithIn pot = new PotentialWithIn();
        Thread th = new Thread(pot);
        th.start();
    }

    public void train(){

        LinkedList<String>methodNames = mainWindow.getMethodList();


        LinkedList <VoterThread>methodL = (LinkedList<VoterThread>) methodList.clone();
        Iterator <VoterThread>methodIter = methodL.iterator();
        boolean found = false;


        while(methodIter.hasNext())
        {
            Iterator <String>nameIter = methodNames.iterator();
            VoterThread methName = methodIter.next();
            String tempName = methName.getMethName();
            
            while(nameIter.hasNext()){
                String nameList = nameIter.next();
                System.out.printf("Search name:%s in list:%s\n",tempName, nameList);
                if(nameList.compareTo(tempName)==0){
                    found=true;
                    break;
                }
            }
            if (!found){
               // System.out.printf("Removeing:%s\n",tempName);
                methodIter.remove();
            }
            found=false;
        }

        if(dispatcher.setList(methodL)){
            help.debug(1, "Dispatcher is already full");
            return;
        }
        dispatcher.run();
        
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
             System.err.printf("Warning FIXME!!\n");
            //FIXME:what to do when..
            return;
        }

        //Register access port for messages
        graphAccessPoints.put(reviewTarget, new Integer(acceccPoint));

    }

    public int fetchGraph(String reviewTarget) {
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


    public void testGrapth(){
        GraphSender logger = new GraphSender(this);
        GraphPacket packet = new GraphPacket();
        
        for(int i=1;i<10;i++)
        {
            packet.day = i;
            packet.month = 11;
            packet.year = 2010;
            packet.seriesTitle  = "GraphTestDecision";
            packet.result = packet.day + i / 3;
            
            logger.sentPacket("Fortum Oyj", packet);
        }

        
    }






}
