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


package jtotus.gui;

import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import jtotus.common.Helper;
import jtotus.config.GUIConfig;
import jtotus.gui.graph.JtotusGraph;
import jtotus.threads.MethodEntry;

/**
 *
 * @author house
 */
public class jtotusMethodView extends JTabbedPane{
    private JScrollPane  jScrollPane1 = null;
    private JDesktopPane drawDesktopPane = null;
    private JTable methodTable = null;
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
             System.out.printf("[%s] Failed ot bind to port\n","jtotusMethodView");
            return bindPort;
        }

        bindPort = tempGraph.getBindPort();
        help.debug("jtotusMethodView",
                "Binded to port:%d\n", tempGraph.getBindPort());

        Thread painter = new Thread(tempGraph);
        painter.start();

        drawDesktopPane.add(tempFrameGraph, javax.swing.JLayeredPane.DEFAULT_LAYER);
        drawDesktopPane.setAutoscrolls(true);
        return bindPort;
    }



    public LinkedList<String> getSelectedMethods() {
        LinkedList<String> selectedRows = new LinkedList<String>();

        int []selRow = methodTable.getSelectedRows();
        help.debug("jtotusMethodView",
                   "Selected total:%d\n", selRow.length);

        DefaultTableModel methodModel = (DefaultTableModel) methodTable.getModel();
        
        for (int i = 0; i < selRow.length;i++) {
            help.debug("jtotusMethodView",
                    "Selected:%s\n", (String)methodModel.getValueAt(selRow[i], 0));
            
            selectedRows.add((String)methodModel.getValueAt(selRow[i], 0));
        }

        return selectedRows;
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
        
        LinkedList<MethodEntry> methods = uiConfig.getSupportedMethodsList();
        Iterator <MethodEntry>methIter = methods.iterator();
        while(methIter.hasNext()) {
            MethodEntry next = methIter.next();
            String rowsValues[] = new String[listOfStocks.length];
            rowsValues[0] = next.getMethName();
            methodModel.addRow(rowsValues);
        }
        
        retValue.setModel(methodModel);
        retValue.setUpdateSelectionOnSort(true);
        
        retValue.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

 
        return retValue;
    }
    
    private void configureMethodTab() {

        this.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setAutoscrolls(true);
        this.setName("methodTabbedPane"); // NOI18N


        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setName("jScrollPane1");

        methodTable = createMethodTable();
        jScrollPane1.setViewportView(methodTable);

        //First tab - Method result
        this.addTab("Methods", jScrollPane1);
        
        //Second tab - Graphs
        this.addTab("Graphs", drawDesktopPane);

        drawDesktopPane.setAutoscrolls(true);
        drawDesktopPane.setName("drawDesktopPane");

    }

    public void initialize(){
        jScrollPane1 = new JScrollPane();
        drawDesktopPane = new JDesktopPane();

        configureMethodTab();
       
    }

}
