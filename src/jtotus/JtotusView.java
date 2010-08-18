/*
 * JtotusView.java
 */

package jtotus;


import java.awt.Color;
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
import javax.swing.JDialog;
import jtotus.engine.Engine;
import jtotus.threads.VoterThread;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
 
    
/**
 * The application's main frame.
 */
public class JtotusView extends FrameView {

   private DefaultListModel dlm = new DefaultListModel(); //Available list
   private DefaultListModel dlm2 = new DefaultListModel(); //selectedList
   private Engine mainEngine = null;
   private GraphPrinter printer = null;


   public void prepareMethodList(LinkedList <VoterThread>methods)
    {
        dlm.add(0, "(All)");
        dlm2.add(0, "(All)");

        
        Iterator iterator = methods.iterator();
        while (iterator.hasNext()) {
            dlm.add(dlm.size(),((VoterThread)iterator.next()).getMethName());
         }

    }

   public LinkedList<String> getMethodList() {
       LinkedList<String>list = new LinkedList<String>();
       

       for (int i = dlm2.size()-1;i>=1;i--){
         //  System.out.printf("JtotusView adding:%s\n", dlm2.get(i));
           list.add((String)dlm2.get(i));
           //dlm2.remove(i);
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





        printer = new GraphPrinter(jInternalFrameGraph);
        printer.draw();
        
        
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
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        SelectedList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        AvailableList = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButtonRunScripts = new javax.swing.JButton();
        jInternalFrameGraph = new javax.swing.JInternalFrame();
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

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        SelectedList.setModel(dlm2);
        SelectedList.setDragEnabled(true);
        SelectedList.setDropMode(javax.swing.DropMode.ON);
        SelectedList.setName("SelectedList"); // NOI18N
        SelectedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SelectedListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(SelectedList);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jtotus.JtotusApp.class).getContext().getResourceMap(JtotusView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

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

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jButtonRunScripts.setText(resourceMap.getString("jButtonRunScripts.text")); // NOI18N
        jButtonRunScripts.setName("jButtonRunScripts"); // NOI18N
        jButtonRunScripts.setPreferredSize(new java.awt.Dimension(43, 27));
        jButtonRunScripts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButtonRunScriptsMousePressed(evt);
            }
        });

        jInternalFrameGraph.setIconifiable(true);
        jInternalFrameGraph.setMaximizable(true);
        jInternalFrameGraph.setResizable(true);
        jInternalFrameGraph.setToolTipText(resourceMap.getString("jInternalFrameGraph.toolTipText")); // NOI18N
        jInternalFrameGraph.setDoubleBuffered(true);
        jInternalFrameGraph.setName("jInternalFrameGraph"); // NOI18N
        jInternalFrameGraph.setVisible(true);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jInternalFrameGraph, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), jInternalFrameGraph, org.jdesktop.beansbinding.BeanProperty.create("defaultCloseOperation"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jInternalFrameGraphLayout = new javax.swing.GroupLayout(jInternalFrameGraph.getContentPane());
        jInternalFrameGraph.getContentPane().setLayout(jInternalFrameGraphLayout);
        jInternalFrameGraphLayout.setHorizontalGroup(
            jInternalFrameGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 591, Short.MAX_VALUE)
        );
        jInternalFrameGraphLayout.setVerticalGroup(
            jInternalFrameGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 444, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonRunScripts, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
                .addComponent(jInternalFrameGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonRunScripts, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jInternalFrameGraph, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(97, Short.MAX_VALUE))
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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1119, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 933, Short.MAX_VALUE)
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

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void SelectedListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SelectedListMouseClicked
         
        if (SelectedList.isSelectionEmpty()){
            dlm2.add(0, "SelectedList test");
            dlm2.add(0, "SelectedList test2");
            dlm2.add(0, "SelectedList test3");
            return;
        }
        else if (SelectedList.getModel().getSize() == 0){
            dlm2.add(0, "(All)");
            return;
        }
        else
        {
            int element = SelectedList.getSelectedIndex();

            if (element == 0)
            {
                   while(dlm2.size() > 1)
                {
                    boolean found=false;
                    for (int i = dlm.size()-1;i>=1;i--)
                    {
                        String tmp = (String) dlm.get(i);
                        if(tmp.compareTo((String) dlm2.get(dlm2.size()-1)) == 0)
                        {
                          found=true;
                          break;
                        }
                    }

                    if (!found)
                    {
                        dlm.add(dlm.size(), dlm2.get(dlm2.size()-1));
                        found = false;
                    }

                     dlm2.remove(dlm2.size()-1);
                }

            }
            else {
                String name = (String) dlm2.getElementAt(element);
                dlm.add(dlm.size(), name);
                dlm2.remove(SelectedList.getSelectedIndex());
                SelectedList.clearSelection();
            }


        }
    }//GEN-LAST:event_SelectedListMouseClicked

    private void AvailableListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AvailableListMouseReleased

        
        if (AvailableList.isSelectionEmpty()){
            dlm.add(0, "AvailableList test");
             dlm.add(0, "AvailableList test2");
              dlm.add(0, "AvailableList test3");
               dlm.add(0, "AvailableList test");
                dlm.add(0, "AvailableList test4");
                
            return;
        }
        else if (AvailableList.getModel().getSize() == 0){
            dlm.add(0, "(All)");
            return;
        }
        else
        {
            int element = AvailableList.getSelectedIndex();
            if (element == 0)
            {
                while(dlm.size() > 1)
                {
                    boolean found=false;
                    for (int i = dlm2.size()-1;i>=1;i--)
                    {
                        String tmp = (String) dlm2.get(i);
                        if(tmp.compareTo((String) dlm.get(dlm.size()-1)) == 0)
                        {
                          found=true;
                          break;
                        }
                    }

                    if (!found)
                    {
                        dlm2.add(dlm2.size(), dlm.get(dlm.size()-1));
                        found = false;
                    }

                     dlm.remove(dlm.size()-1);
                }
            }
            else {
                String name = (String) dlm.getElementAt(element);
                dlm.remove(AvailableList.getSelectedIndex());
                dlm2.add(dlm2.size(), name);
                AvailableList.clearSelection();
            }
         

        }

    }//GEN-LAST:event_AvailableListMouseReleased

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO Tell engine to start training

        mainEngine.train();

        
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButtonRunScriptsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRunScriptsMousePressed
//        XYDataset dataset = createDataset();
//        JFreeChart chart = createChart(dataset);
//        ChartPanel chartPanel = new ChartPanel(chart);
//        jInternalFrameGraph.setContentPane(chartPanel);


        printer.testDraw();
       

}//GEN-LAST:event_jButtonRunScriptsMousePressed

   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JList AvailableList;
    public javax.swing.JList SelectedList;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonRunScripts;
    private javax.swing.JInternalFrame jInternalFrameGraph;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
