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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import brokerwatcher.BrokerWatcher;
import com.espertech.esper.client.*;
import org.jtotus.common.Helper;
import org.jtotus.common.MethodResults;
import org.jtotus.config.ConfPortfolio;
import org.jtotus.config.ConfigLoader;
import org.jtotus.config.GUIConfig;
import org.jtotus.config.MainMethodConfig;
import org.jtotus.engine.Engine;
import org.jtotus.gui.mail.JtotusGmailClient;
import org.jtotus.methods.MethodEntry;

/**
 *
 * @author Evgeni Kappinen
 */
public class JTotusMethodView extends JTabbedPane implements MethodResultsPrinter, UpdateListener {

    private JScrollPane jScrollPane1 = null;
    private JDesktopPane drawDesktopPane = null;
    private JTable methodTable = null;
    private Helper help = Helper.getInstance();

    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {

        for (EventBean eventBean : eventBeans) {
            if (eventBean.getUnderlying() instanceof MethodResults) {
                MethodResults results = (MethodResults) eventBean.getUnderlying();
                this.drawResults(results);
            }
        }
    }

    class methodTableListener implements TableModelListener {

        public void tableChanged(TableModelEvent event) {
            DefaultTableModel source = (DefaultTableModel) event.getSource();

            if (event.getType() == TableModelEvent.UPDATE
                    || event.getType() == TableModelEvent.INSERT) {
                String type = (String) source.getValueAt(source.getRowCount() - 1, 0);

                //If sum column does not exists create one
                if (type.compareTo("Sum") != 0 && source.getRowCount() != 0) {
                    String[] data = new String[source.getColumnCount()];
                    data[0] = "Sum";
                    source.addRow(data);
                }

                //TODO: calculate sum, TableModelEvent.ALL_COLUMNS
                //TODO: summ only when Normilizer is used.
                int col = event.getColumn();
                if (col == TableModelEvent.ALL_COLUMNS) {
                    System.err.printf("TODO: all columns\n");
                    return;
                }


                Double sum = new Double(0.0f);
                int count = 0;
                for (int row = source.getRowCount() - 2; row > 0; row--) {
                    String rowValue = (String) source.getValueAt(row, col);
                    if (rowValue != null) {
                        sum += Double.valueOf(rowValue);
                        count++;
                    }
                }

                sum /= Double.valueOf(count);
                String sumValue = sum.toString();
                String value = (String) source.getValueAt(source.getRowCount() - 1, col);
                if (value == null || value.compareTo(sumValue) != 0) {
                    source.setValueAt(sumValue, source.getRowCount() - 1, col);
                }

            }

        }
    }

    private class PopupListener extends MouseAdapter {
        JTable table = null;
        JPopupMenu popup = null;
        JCheckBoxMenuItem item = null;
        JCheckBoxMenuItem auto = null;
        ConfigLoader<MainMethodConfig> configFile = null;
        MainMethodConfig config = null;
        
        public PopupListener(JTable table) {
            this.table =  table;
        }

        public JPopupMenu getPopupMenu() {
            if (popup != null) {
                return popup;
            }

            popup = new JPopupMenu();
            item = new JCheckBoxMenuItem("Draw");
            auto = new JCheckBoxMenuItem("Auto-start");
            popup.add(item);
            popup.add(auto);

            item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    config.inputPrintResults = !config.inputPrintResults;
                    configFile.storeConfig(config);
                }
            });

            auto.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    int[] selectedRows = table.getSelectedRows();
                    ConfPortfolio portfolioConfig;

                    ConfigLoader<ConfPortfolio> configPortfolio =
                            new ConfigLoader<ConfPortfolio>("OMXHelsinki");

                    portfolioConfig = configPortfolio.getConfig();
                    if (portfolioConfig == null) {
                        //Load default values
                        portfolioConfig = new ConfPortfolio();
                    }

                    for (int selectedRow : selectedRows) {
                        String method = table.getModel().getValueAt(selectedRow, 0).toString();
                        portfolioConfig.setAutoStared(method);
                    }
                    configPortfolio.storeConfig(portfolioConfig);
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
                final JPopupMenu popupMenu = getPopupMenu();

                ConfPortfolio portfolioConfig;

                ConfigLoader<ConfPortfolio> configPortfolio =
                        new ConfigLoader<ConfPortfolio>("OMXHelsinki");

                portfolioConfig = configPortfolio.getConfig();
                if (portfolioConfig == null) {
                    //Load default values
                    portfolioConfig = new ConfPortfolio();
                }

                int[] selectedRows = table.getSelectedRows();

                for (int row = 0; row < selectedRows.length; row++) {
                    String method = table.getModel().getValueAt(selectedRows[row], 0).toString();
                    if (portfolioConfig.isAutoStared(method)) {
                        auto.setSelected(true);
                    } else {
                        auto.setSelected(false);
                    }
                    
                    int[] selectedColumns = table.getSelectedColumns();
                    for (int selectedColumn : selectedColumns) {
                        if (!table.isCellSelected(selectedRows[row], selectedColumn)
                                || selectedColumn == 0) {
                            continue;
                        }
                        //
                        String name = table.getValueAt(selectedRows[row], 0).toString();

                        configFile = new ConfigLoader<MainMethodConfig>("OMXHelsinki"
                                    + File.separator
                                    + table.getColumnModel().getColumn(selectedColumn).getHeaderValue()
                                    + File.separator
                                    + name);

                        config = configFile.getConfig();
                        if (config != null && config.inputPrintResults) {
                            item.setSelected(true);
                        } else {
                            item.setSelected(false);
                        }
                    }
                }

                
                

                popupMenu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    public JDesktopPane getMainPane() {
        return drawDesktopPane;
    }

    public JInternalFrame addComponentToInternalWindow(JComponent component, String title) {

        JInternalFrame tempGraph = new JInternalFrame();

        tempGraph.setClosable(true);
        tempGraph.setIconifiable(true);
        tempGraph.setMaximizable(true);
        tempGraph.setDoubleBuffered(true);
        tempGraph.setInheritsPopupMenu(true);
        tempGraph.setLayer(5);
        tempGraph.setName("tempFrameGraph"); // NOI18N
        tempGraph.setOpaque(false);
        tempGraph.setVisible(true);
        tempGraph.setBounds(10, 10, 590, 460);
        tempGraph.setResizable(true);

        if(component != null) {
            tempGraph.getContentPane().add(component);
        }
        
        tempGraph.setTitle(title);

        drawDesktopPane.add(tempGraph, javax.swing.JLayeredPane.DEFAULT_LAYER);

        return tempGraph;
    }



    public LinkedList<String> getSelectedMethods() {
        LinkedList<String> selectedRows = new LinkedList<String>();

        int[] selRow = methodTable.getSelectedRows();
        help.debug("jtotusMethodView",
                "Selected total:%d\n",
                selRow.length);

        DefaultTableModel methodModel = (DefaultTableModel) methodTable.getModel();

        for (int i = 0; i < selRow.length; i++) {
            help.debug("jtotusMethodView",
                    "Selected:%s\n", (String) methodModel.getValueAt(selRow[i], 0));

            selectedRows.add((String) methodModel.getValueAt(selRow[i], 0));
        }

        return selectedRows;
    }

    private JTable createMethodTable() {
        JTable retValue = new JTable();

        DefaultTableModel methodModel = new DefaultTableModel();
        methodModel.addColumn("");


        GUIConfig uiConfig = new GUIConfig();
        String listOfStocks[] = uiConfig.fetchStockNames();

        for (String listOfStock : listOfStocks) {
            methodModel.addColumn(listOfStock);
        }

        LinkedList<MethodEntry> methods = uiConfig.getSupportedMethodsList();
        for (MethodEntry method : methods) {
            MethodEntry next = method;
            String rowsValues[] = new String[listOfStocks.length + 1];
            rowsValues[0] = next.getMethName();
            methodModel.addRow(rowsValues);
        }

        methodTableListener methTableLister = new methodTableListener();
        methodModel.addTableModelListener(methTableLister);

        retValue.setModel(methodModel);
        retValue.setUpdateSelectionOnSort(true);
        retValue.addMouseListener(new PopupListener(retValue));
//        retValue.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


        return retValue;
    }

    private void configureMethodTab() {

        this.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        this.setAutoscrolls(false);
        this.setName("methodTabbedPane"); // NOI18N


        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setName("jScrollPane1");
        

        methodTable = createMethodTable();
        jScrollPane1.setViewportView(methodTable);

        //First tab - Method result
        this.setAutoscrolls(false);

        jScrollPane1.setBorder(null);
        
        this.addTab("Methods", jScrollPane1);

        //Second tab - Graphs
        this.addTab("Desktop", drawDesktopPane);

        drawDesktopPane.setAutoscrolls(false);
        drawDesktopPane.setName("drawDesktopPane");
        methodTable.setShowGrid(true);

    }

    public void initialize() {
        jScrollPane1 = new JScrollPane();
        drawDesktopPane = new JDesktopPane();
        //Register Method Results printer
        //TODO:remove from engine !!!
        Engine engine = Engine.getInstance();
        engine.registerResultsPrinter(this);

        EPServiceProvider cepEngine = BrokerWatcher.getMainEngine();
        EPAdministrator cepAdm = cepEngine.getEPAdministrator();

        EPStatement eps = cepAdm.createEPL("select * from MethodResults");
        eps.addListener(this);
        this.configureMethodTab();
        jScrollPane1.setHorizontalScrollBarPolicy(jScrollPane1.HORIZONTAL_SCROLLBAR_ALWAYS);
    }

    public void drawResults(MethodResults results) {
        DefaultTableModel methodModel = (DefaultTableModel) methodTable.getModel();
        int method_idx = this.getRowIndex(results.getMethodName());

        HashMap<String, Double> result = results.getResults();
        Set<Entry<String, Double>> stockNameSet = result.entrySet();
        Iterator<Entry<String, Double>> entryIter = stockNameSet.iterator();
        int stock_idx = 0;
        Double resultDoubleToString = null;
        while (entryIter.hasNext()) {
            Entry<String, Double> entry = entryIter.next();
            stock_idx = this.getColumnIndex(entry.getKey());
            resultDoubleToString = entry.getValue();
            if (method_idx != -1 && stock_idx != -1) {
                methodModel.setValueAt(
                        String.valueOf(resultDoubleToString.doubleValue()),
                        method_idx, stock_idx);
            } else {
                System.err.printf("Warning could not find '%s' for '%s' method\n", entry.getKey(), results.getMethodName());
            }

        }
    }

    private int getRowIndex(String methodName) {
        DefaultTableModel methodModel = (DefaultTableModel) methodTable.getModel();

        int i = 0;
        while (i < methodModel.getRowCount()) {
            help.debug(this.getClass().getName(),
                    "From columns Searching:%s:%s\n", methodName,
                    (String) methodModel.getValueAt(i, 0));

            String method = (String) methodModel.getValueAt(i, 0);
            if (method.compareTo(methodName) == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int getColumnIndex(String stockName) {
        DefaultTableModel methodModel = (DefaultTableModel) methodTable.getModel();

        for (int i = 0; i < methodModel.getColumnCount(); i++) {
            help.debug(this.getClass().getName(),
                    "From rows Searching:%s:%s\n", stockName,
                    (String) methodModel.getColumnName(i));

            String stock = methodModel.getColumnName(i);
            if (stockName.compareTo(stock) == 0) {
                return i;
            }
        }
        return -1;
    }

    public void sendReport() {
        JtotusGmailClient gmailClient = new JtotusGmailClient();

        ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");
        GUIConfig config = loader.getConfig();

        gmailClient.setDefaultLogin(config.getGmailLogin());
        gmailClient.setDefaultPassword(config.getGmailPassword());
        
        DefaultTableModel methodModel = (DefaultTableModel) methodTable.getModel();

        for(int column = 1;column <methodModel.getColumnCount();column++) {
            gmailClient.pushText("Stock: " + methodModel.getColumnName(column) + "\n");
            for(int row = 1; row < methodModel.getRowCount();row++) {
                Object value = methodModel.getValueAt(row, column);
                if (value != null) {
                    Object method = methodModel.getValueAt(row, 0);
                    gmailClient.pushText( method.toString() +"=" + value.toString()+"\n");
                }
            }
            gmailClient.pushText("\n");
        }

        gmailClient.sendThreaded();
    }

}
