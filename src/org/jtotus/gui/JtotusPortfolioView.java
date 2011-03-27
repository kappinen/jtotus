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

import brokerwatcher.BrokerWatcher;
import brokerwatcher.eventtypes.IndicatorData;
import brokerwatcher.eventtypes.StockTick;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;
import org.jtotus.gui.graph.DynamicCharting;

/**
 *
 * @author Evgeni Kappinen
 */
public class JtotusPortfolioView extends JTabbedPane implements UpdateListener {

    private JScrollPane jScrollPane4 = null;
    private JTable portfolioTable = null;
    DefaultTableModel portfolioModel = null;
    private JDesktopPane desktopPane = null;
    private HashMap <String, String>titleMap = null;
    private GUIConfig uiConfig = null;
    


    public void loadStoredView() {
        if (uiConfig == null) {
            ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");
            uiConfig = loader.getConfig();
            if (uiConfig == null) {
                uiConfig = new GUIConfig();
            }
        }
    }

    
    public void createPortfolioTable() {

        //Load configuration for GUI
        loadStoredView();
        String listOfStocks[] = uiConfig.fetchStockNames();
        
        for (int i = 0; i < listOfStocks.length; i++) {
            insertRow(listOfStocks[i]);
        }

//            portfolioModel.setValueAt(listOfStocks[i], i, 0);
//            stock.setStockName(listOfStocks[i]);
//            portfolioModel.setValueAt(stock.fetchCurrentClosingPrice().toString(), i, 1);
//            portfolioModel.setValueAt(stock.fetchCurrentVolume(), i, 3);
//        }
     

//        portfolioTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title0")); // NOI18N
//        portfolioTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title1")); // NOI18N

        return;
    }

    private void update(StockTick tick) {

        upsertValue(tick.getStockName(), "Price", tick.getLatestPrice());
        upsertValue(tick.getStockName(), "Buy", tick.getLatestBuy());
        upsertValue(tick.getStockName(), "Sell", tick.getLatestSell());
        upsertValue(tick.getStockName(), "High", tick.getLatestHighest());
        upsertValue(tick.getStockName(), "Low", tick.getLatestLowest());
        upsertValue(tick.getStockName(), "Volume", tick.getVolume());
        upsertValue(tick.getStockName(), "TradeSum", tick.getTradesSum());
        upsertValue(tick.getStockName(), "Time", tick.getTime());

    }

   private int insertRow(String columnHeader) {
       Object []row = new Object[portfolioTable.getColumnCount()+1];
       row[0] = columnHeader;
       DefaultTableModel tableModel= (DefaultTableModel) portfolioTable.getModel();
       tableModel.addRow(row);
       
       return portfolioTable.getRowCount();
   }


    private int insertColumn(String columnHeader) {

        DefaultTableModel model = (DefaultTableModel)portfolioTable.getModel();
        TableColumn column = new TableColumn(model.getColumnCount());

        if (portfolioTable.getAutoCreateColumnsFromModel()) {
            throw new IllegalStateException();
        }

        column.setHeaderValue(columnHeader);
        portfolioTable.addColumn(column);
        model.addColumn(column);

        return portfolioTable.getColumnModel().getColumnIndex(columnHeader);
    }

    private void upsertValue(String stockName, String columnName, Object value) {
        int columnIndex = -1;
        int rowIndex = -1;

        try {
            columnIndex = portfolioTable.getColumnModel().getColumnIndex(columnName);
        } catch (IllegalArgumentException ex) {
            columnIndex = insertColumn(columnName);
        }

        for (int count = portfolioTable.getRowCount(), i = 0; i < count; i++) {
            Object rowValue = portfolioTable.getModel().getValueAt(i, 0);
            if (rowValue != null && rowValue.toString().equalsIgnoreCase(stockName)) {
                rowIndex = i;
                break;
            }
        }

        if (rowIndex == -1) {
            rowIndex = insertRow(stockName);
        }

        portfolioTable.getModel().setValueAt(value, rowIndex, columnIndex);
    }

    protected void monitorStock() {
        
        int[] selectedRows = portfolioTable.getSelectedRows();


        for (int row = 0; row < selectedRows.length; row++) {
            int[] selectedColumns = portfolioTable.getSelectedColumns();
            for (int col = 0; col < selectedColumns.length; col++) {
                
                if (!portfolioTable.isCellSelected(selectedRows[row], selectedColumns[col]) ||
                    selectedColumns[col] == 0) {
                    continue;
                }

                String stockName = portfolioTable.getModel().getValueAt(selectedRows[row], 0).toString();
                JInternalFrame interFrame = new JInternalFrame();
                interFrame.setClosable(true);
                interFrame.setIconifiable(true);
                interFrame.setMaximizable(true);
                interFrame.setDoubleBuffered(true);
                interFrame.setInheritsPopupMenu(true);
                interFrame.setLayer(5);
                interFrame.setName(stockName); // NOI18N
                interFrame.setOpaque(false);
                interFrame.setBounds(10, 10, 590, 460);
                interFrame.setResizable(true);

                interFrame.setVisible(true);

                DynamicCharting dynChart = new DynamicCharting();

                String colName = (String) portfolioTable.getColumnModel().getColumn(selectedColumns[col]).getHeaderValue();
                String valueType = titleMap.get(colName);

                if (valueType != null) {
                    dynChart.registerForEvents("select " + valueType + " as valueForGUI from StockTick where stockName='" + stockName + "'");
                }else {
                    dynChart.registerForEvents("select indicatorValue as valueForGUI from IndicatorData where stockName='" + stockName + "' and indicatorName='"+colName+"'");
                }

                interFrame.setTitle(stockName + " (" + colName + ")");
                interFrame.getContentPane().add(dynChart);
                desktopPane.add(interFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
            }
        }
    }

    void setMainPane(JDesktopPane mainPane) {
        desktopPane = mainPane;
    }

    //http://download.oracle.com/javase/tutorial/uiswing/components/menu.html
    class PopupListener extends MouseAdapter {

        JPopupMenu popup = null;

        public JPopupMenu getStockPortfolioPopupMenu() {
            if (popup != null) {
                return popup;
            }

            popup = new JPopupMenu();
            JMenuItem item = new JMenuItem("Monitor");

            popup.add(item);

            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    monitorStock();
                }
            });

            return popup;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                final JPopupMenu popupMenu = getStockPortfolioPopupMenu();
                popupMenu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }






    public void initialize() {
        jScrollPane4 = new javax.swing.JScrollPane();
        portfolioTable = new JTable();
        portfolioModel = new DefaultTableModel(0,1);
//                new Object[][]{ new Object[100],}, new Object[100]
//        );

        portfolioTable.setModel(portfolioModel);
        portfolioTable.setName("portfolioTable"); // NOI18N
        portfolioTable.setColumnSelectionAllowed(true);
        portfolioTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        portfolioTable.addMouseListener(new PopupListener());
        portfolioTable.setAutoCreateColumnsFromModel(false);

        createPortfolioTable();

        //FIXME:name from config
        this.addTab("OMXHelsinki", jScrollPane4);

        jScrollPane4.setViewportView(portfolioTable);

        titleMap = new HashMap<String, String>();
        
        titleMap.put("Stock", "StockName");
        titleMap.put("Price", "latestPrice");
        titleMap.put("Buy", "latestBuy");
        titleMap.put("Sell", "latestSell");
        titleMap.put("High", "latestHighest");
        titleMap.put("Low", "latestLowest");
        titleMap.put("Volume", "volume");
        titleMap.put("TradeSum", "tradesSum");
        titleMap.put("Time", "time");
        

        EPServiceProvider provider = BrokerWatcher.getMainEngine();
        EPStatement eps = provider.getEPAdministrator().createEPL("select * from StockTick");
        EPStatement eps2 = provider.getEPAdministrator().createEPL("select * from IndicatorData");
        eps.addListener(this);
        eps2.addListener(this);
        //insertColumn("testing");
    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {


        for (int i = 0; i < ebs.length; i++) {
            if (ebs[i].getEventType().getName().equals("StockTick")) {
                StockTick tick = (StockTick) ebs[i].getUnderlying();
                if (tick != null) {
                    update(tick);
                }
            }else if (ebs[i].getEventType().getName().equals("IndicatorData")){
                IndicatorData data = (IndicatorData) ebs[i].getUnderlying();
                upsertValue(data.getStockName(), data.getIndicatorName(), data.getIndicatorValue());
            }
        }

    }
}
