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


package org.jtotus.gui;


import java.util.Calendar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jtotus.common.StockType;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;

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
                                "Stock", "Last", "Past", "Volume"
                            });


        retValue.setModel(portfolioModel);

        //Load configuration for GUI
       
         String listOfStocks[] = uiConfig.fetchStockName();

         for (int i = 0;i<listOfStocks.length;i++) {
             if (i>=portfolioModel.getRowCount()){
                 portfolioModel.addRow(new Object[] {null,null,null,null});
             }
             portfolioModel.setValueAt(listOfStocks[i], i, 0);
             StockType stock = new StockType(listOfStocks[i]);
             portfolioModel.setValueAt(stock.fetchCurrentClosingPrice().toString(), i, 1);
             //portfolioModel.setValueAt(stock.fetchPastDayClosingPrice(1).toString(), i, 2);
             portfolioModel.setValueAt(stock.fetchCurrentVolume(), i, 3);
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
        this.addTab("OMXHelsinki", jScrollPane4);

        jScrollPane4.setViewportView(portfolioTable);

    }

}
