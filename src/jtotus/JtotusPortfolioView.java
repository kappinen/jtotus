/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus;


import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;
import jtotus.config.ConfigLoader;
import jtotus.config.GUIConfig;

/**
 *
 * @author house
 */
public class JtotusPortfolioView extends JTabbedPane {
    private JScrollPane jScrollPane4 = null;
    private JTable portfolioTable = null;
    private GUIConfig uiConfig = null;


    public void loadStoredView() {
        if (uiConfig != null) {
            ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");
            loader.storeConfig(uiConfig);
        }
    }


   public JTable createPortfolioTable() {
       JTable retValue = null;


       ConfigLoader<JTable> tableState = new ConfigLoader<JTable>("GuiTableState");
       retValue = tableState.getConfig();

        if (retValue == null){
            retValue = new JTable();
        }


        DefaultTableModel portfolioModel = new DefaultTableModel(new Object [][] {
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null}
                            },
                            new String [] {
                                "Stock", "Title 2", "Title 3", "Title 4"
                            });


        retValue.setModel(portfolioModel);

        //Load configuration for GUI
       
         String listOfStocks[] = uiConfig.fetchStockName();

         for (int i = 0;i<listOfStocks.length-1;i++) {
             if (i>=portfolioModel.getRowCount()){
                 portfolioModel.addRow(new Object[] {null,null,null,null});
             }
             portfolioModel.setValueAt(listOfStocks[i], i, 0);
         }


         retValue.setColumnSelectionAllowed(true);
         retValue.setName("portfolioTable"); // NOI18N
         retValue.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        portfolioTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title0")); // NOI18N
//        portfolioTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title1")); // NOI18N
//        portfolioTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title2")); // NOI18N
//        portfolioTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title3")); // NOI18N

       return retValue;
   }


   public void initialize() {
        jScrollPane4 = new javax.swing.JScrollPane();
        uiConfig = new GUIConfig();
        portfolioTable = createPortfolioTable();

        //FIXME:name from config
        this.addTab("OMX", jScrollPane4);

        jScrollPane4.setViewportView(portfolioTable);

    }

}
