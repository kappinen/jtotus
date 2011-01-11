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
import org.jtotus.common.StockType;
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
    private GUIConfig uiConfig = null;
    private JDesktopPane desktopPane = null;
    private HashMap <String, String>titleMap = null;
    
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
        if (retValue == null) {
            retValue = new JTable();
        }


        DefaultTableModel portfolioModel = new DefaultTableModel(new Object[][]{
                    {null, null, null, null, null, null, null, null, null},},
                new String[]{
                    "Stock", "Price", "Buy", "Sell",
                    "High", "Low", "Volume", "TradeSum",
                    "Time"
                });


        retValue.setModel(portfolioModel);

        //Load configuration for GUI

        String listOfStocks[] = uiConfig.fetchStockNames();
        StockType stock = new StockType();
        for (int i = 0; i < listOfStocks.length; i++) {
            if (i >= portfolioModel.getRowCount()) {
                portfolioModel.addRow(new Object[]{null, null, null, null,
                            null, null, null, null,
                            null});
            }

            portfolioModel.setValueAt(listOfStocks[i], i, 0);
            stock.setStockName(listOfStocks[i]);
            portfolioModel.setValueAt(stock.fetchCurrentClosingPrice().toString(), i, 1);
            portfolioModel.setValueAt(stock.fetchCurrentVolume(), i, 3);
        }



        retValue.setColumnSelectionAllowed(true);
        retValue.setName("portfolioTable"); // NOI18N
        retValue.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//        portfolioTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title0")); // NOI18N
//        portfolioTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title1")); // NOI18N

        return retValue;
    }

    private void update(StockTick tick) {

        DefaultTableModel model = (DefaultTableModel) portfolioTable.getModel();

        for (int row = 0; row < model.getRowCount(); row++) {
            if (!model.getValueAt(row, 0).equals(tick.getStockName())) {
                continue;
            }

            for (int col = 0; col < model.getColumnCount(); col++) {
                switch (col) {
                    case 1:
                        model.setValueAt(tick.getLatestPrice(), row, col);
                        break;
                    case 2:
                        model.setValueAt(tick.getLatestBuy(), row, col);
                        break;
                    case 3:
                        model.setValueAt(tick.getLatestSell(), row, col);
                        break;
                    case 4:
                        model.setValueAt(tick.getLatestHighest(), row, col);
                        break;
                    case 5:
                        model.setValueAt(tick.getLatestLowest(), row, col);
                        break;
                    case 6:
                        model.setValueAt(tick.getVolume(), row, col);
                        break;
                    case 7:
                        model.setValueAt(tick.getTradesSum(), row, col);
                        break;
                    case 8:
                        model.setValueAt(tick.getTime(), row, col);
                        break;

                    default:
                        break;
                }

            }
            break;
        }


    }

    protected void monitorStock() {
        
        int[] selectedRows = portfolioTable.getSelectedRows();


        for (int row = 0; row < selectedRows.length; row++) {
            int[] selectedColumns = portfolioTable.getSelectedColumns();
            for (int col = 0; col < selectedColumns.length; col++) {

                if (selectedColumns[col] == 0 ||
                    selectedColumns[col] == 8)
                    continue;

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

                String colName = portfolioTable.getModel().getColumnName(selectedColumns[col]);

                String valueType = titleMap.get(colName);
                dynChart.registerForEvents("select " + valueType + " as valueForGUI from StockTick where stockName='" + stockName + "'");

                interFrame.setTitle(stockName + " (" + valueType + ")");
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
        uiConfig = new GUIConfig();
        portfolioTable = createPortfolioTable();


        portfolioTable.addMouseListener(new PopupListener());

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
        eps.addListener(this);

    }

    public void update(EventBean[] ebs, EventBean[] ebs1) {

        for (int i = 0; i < ebs.length; i++) {
            StockTick tick = (StockTick) ebs[i].getUnderlying();
            update(tick);

        }

    }
}
