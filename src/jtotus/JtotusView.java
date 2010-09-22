/*
 * JtotusView.java
 */

package jtotus;



import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import jtotus.common.Helper;
import jtotus.config.ConfigLoader;
import jtotus.config.GUIConfig;
import jtotus.engine.Engine;
import jtotus.engine.StatisticsFreqPeriod;
import jtotus.graph.JtotusGraph;
import jtotus.threads.VoterThread;

 
    
/**
 * The application's main frame.
 */
public class JtotusView extends FrameView {

   private DefaultListModel dlm = new DefaultListModel(); //Available list
   private Engine mainEngine = null;
   GUIConfig uiConfig = null;
   private Helper help = Helper.getInstance();



   JTable allocateTable() {
       JTable retValue = null;
       ConfigLoader<JTable> tableState = new ConfigLoader<JTable>("GuiTableState");
       retValue = tableState.getConfig();
       
        if (retValue == null){
            retValue = new JTable();
        }

       return retValue;
   }

   
   public void prepareMethodList(LinkedList <VoterThread>methods)
    {

//        TableModel  tableModel = portfolioTable.getModel();
        DefaultTableModel tableModel = new DefaultTableModel(new Object [][] {
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null}
                            },
                            new String [] {
                                "Stock", "Title 2", "Title 3", "Title 4"
                            });
        portfolioTable.setModel(tableModel);

        dlm.add(0, "(All)");

        
        
        Iterator iterator = methods.iterator();
        while (iterator.hasNext()) {
            dlm.add(dlm.size(),((VoterThread)iterator.next()).getMethName());
         }

         uiConfig = new GUIConfig();
         String listOfStocks[] = uiConfig.fetchStockName();

         for (int i = 0;i<listOfStocks.length-1;i++){
             if (i>=tableModel.getRowCount()){
                 tableModel.addRow(new Object[] {null,null,null,null});
             }
             
             tableModel.setValueAt(listOfStocks[i], i, 0);
         }

         
    }

   public LinkedList<String> getMethodList() {
       LinkedList<String>list = new LinkedList<String>();

       if(AvailableList.isSelectionEmpty()) {
           return list;
       }

       int selList[]  = AvailableList.getSelectedIndices();


       System.out.printf("The list is created : %d \n", selList.length);
       for (int i = selList.length-1; i>=0 ;i--){
          System.out.printf("JtotusView adding:%s\n", AvailableList.getModel().getElementAt(selList[i]));
          list.add((String) AvailableList.getModel().getElementAt(selList[i]));
       }

       return list;
   }



    public JtotusView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        
        
    }

    public void setListener(Engine engine)
    {
        mainEngine = engine;
    }

    
    @Action
    public void showAboutBox() {

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        AvailableList = new javax.swing.JList();
        jSplitPane1 = new javax.swing.JSplitPane();
        jButton1 = new javax.swing.JButton();
        jButtonRunScripts = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        portfolioTable = allocateTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        AvailableList.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        AvailableList.setModel(dlm);
        AvailableList.setDragEnabled(true);
        AvailableList.setDropMode(javax.swing.DropMode.ON);
        AvailableList.setName("AvailableList"); // NOI18N
        AvailableList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                AvailableListMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(AvailableList);

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jtotus.JtotusApp.class).getContext().getResourceMap(JtotusView.class);
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jSplitPane1.setLeftComponent(jButton1);

        jButtonRunScripts.setText(resourceMap.getString("jButtonRunScripts.text")); // NOI18N
        jButtonRunScripts.setName("jButtonRunScripts"); // NOI18N
        jButtonRunScripts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonRunScriptsMousePressed(evt);
            }
        });
        jSplitPane1.setRightComponent(jButtonRunScripts);

        jSeparator1.setName("jSeparator1"); // NOI18N

        jDesktopPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jDesktopPane1.setName("jDesktopPane1"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane3.setViewportView(jTable1);

        jScrollPane3.setBounds(-170, -80, 560, 60);
        jDesktopPane1.add(jScrollPane3, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        portfolioTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Stock Name", "Title 2", "Title 3", "Title 4"
            }
        ));
        portfolioTable.setColumnSelectionAllowed(true);
        portfolioTable.setName("portfolioTable"); // NOI18N
        jScrollPane4.setViewportView(portfolioTable);
        portfolioTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        portfolioTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title0")); // NOI18N
        portfolioTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title1")); // NOI18N
        portfolioTable.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title2")); // NOI18N
        portfolioTable.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("portfolioTable.columnModel.title3")); // NOI18N

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane4.TabConstraints.tabTitle"), jScrollPane4); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1032, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(214, 214, 214)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDesktopPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDesktopPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(jtotus.JtotusApp.class).getContext().getActionMap(JtotusView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1060, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 874, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void AvailableListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AvailableListMouseReleased


     
        
        if (AvailableList.isSelectionEmpty()){
            return;
        }
        else if (AvailableList.getModel().getSize() == 0){
            dlm.add(0, "(All)");
            return;
        }else if (AvailableList.getSelectedIndex() == 0) {
                System.out.printf("All selected\n");
                AvailableList.setSelectionInterval(1, dlm.getSize());
                return;
            }

    

    }//GEN-LAST:event_AvailableListMouseReleased

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO Tell engine to start training

        if (uiConfig != null) {
            ConfigLoader<GUIConfig> loader = new ConfigLoader<GUIConfig>("GUIConfig");
            loader.storeConfig(uiConfig);
        }
        mainEngine.train();

        
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButtonRunScriptsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRunScriptsMousePressed


        StatisticsFreqPeriod stats = new StatisticsFreqPeriod();
        stats.run();
        

      //  mainEngine.testGrapth();

}//GEN-LAST:event_jButtonRunScriptsMousePressed

   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JList AvailableList;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonRunScripts;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTable portfolioTable;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

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

        jDesktopPane1.add(tempFrameGraph, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jDesktopPane1.setAutoscrolls(true);
        return bindPort;
    }
}
