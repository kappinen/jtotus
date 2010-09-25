/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus;

import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import jtotus.common.Helper;
import jtotus.config.GUIConfig;
import jtotus.graph.JtotusGraph;
import jtotus.threads.VoterThread;

/**
 *
 * @author house
 */
public class jtotusMethodView extends JTabbedPane{
    private JScrollPane  jScrollPane1 = null;
    private JDesktopPane drawDesktopPane = null;
    private Helper help = Helper.getInstance();

    
       public int createIntFrame(String reviewTarget) {
        int bindPort=-1;

        javax.swing.JInternalFrame tempFrameGraph = new javax.swing.JInternalFrame();

        tempFrameGraph.setClosable(true);
        tempFrameGraph.setIconifiable(true);
        tempFrameGraph.setMaximizable(true);
        tempFrameGraph.setDoubleBuffered(true);
        tempFrameGraph.setInheritsPopupMenu(true);
        tempFrameGraph.setLayer(5);
        tempFrameGraph.setName("tempFrameGraph"); // NOI18N
        tempFrameGraph.setOpaque(false);
        tempFrameGraph.setVisible(true);

        tempFrameGraph.setBounds(10, 10, 590, 460);


        JtotusGraph tempGraph = new JtotusGraph(tempFrameGraph, reviewTarget);
        if(tempGraph.initialize()==false) { //Failed to bind to port
             System.out.printf("[%s] Failed ot bind to port\n",this.getClass().getName());
            return bindPort;
        }

        bindPort = tempGraph.getBindPort();
        help.debug(this.getClass().getName(),
                "Binded to port:%d\n", tempGraph.getBindPort());

        Thread painter = new Thread(tempGraph);
        painter.start();

        drawDesktopPane.add(tempFrameGraph, javax.swing.JLayeredPane.DEFAULT_LAYER);
        drawDesktopPane.setAutoscrolls(true);
        return bindPort;
    }

    private JTable createMethodTable() {
        JTable retValue = new JTable();
        
        DefaultTableModel methodModel = new DefaultTableModel();
        methodModel.addColumn("");
        

        GUIConfig uiConfig = new GUIConfig();
        String listOfStocks[] = uiConfig.fetchStockName();

        for (int i = 0;i<listOfStocks.length-1;i++) {
            methodModel.addColumn(listOfStocks[i]);
         }
        
        LinkedList<VoterThread> methods = uiConfig.getSupportedMethodsList();
        Iterator <VoterThread>methIter = methods.iterator();
        while(methIter.hasNext()) {
            VoterThread next = methIter.next();
            String rowsValues[] = new String[listOfStocks.length];
            rowsValues[0] = next.getMethName();
            methodModel.addRow(rowsValues);
        }
        
        retValue.setModel(methodModel);

        return retValue;
    }
    
    private void configureMethodTab() {

        this.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setAutoscrolls(true);
        this.setName("methodTabbedPane"); // NOI18N


        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setName("jScrollPane1");


        jScrollPane1.setViewportView(createMethodTable());

        //First tab - Method result
        this.addTab("Methods", jScrollPane1);

        //Second tab - Graphs
        drawDesktopPane.setAutoscrolls(true);
        drawDesktopPane.setName("drawDesktopPane"); // NOI18N
        this.addTab("Graphs", drawDesktopPane); // NOI18N
    }

    public void initialize(){
        jScrollPane1 = new JScrollPane();
        drawDesktopPane = new JDesktopPane();

        configureMethodTab();
       
    }

}
