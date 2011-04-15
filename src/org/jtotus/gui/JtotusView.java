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
 *
 *
 * http://tutorials.jenkov.com/java-collections/navigableset.html
 */

package org.jtotus.gui;


import brokerwatcher.generators.TickInterface;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jtotus.crypt.JtotusKeyRingPassword;
import org.jtotus.engine.Engine;
import org.jtotus.gui.passwords.JtotusPasswordGUI;
import org.jtotus.gui.passwords.JtotusSetPasswordsGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The application's main frame.
 */
public class JtotusView extends FrameView {

    private Engine mainEngine = null;
    private JFrame mainFrame = null;


    public void initialize() {
        ((JTotusMethodView) methodTabbedPane).initialize();

        JtotusPortfolioView portTabTable = new JtotusPortfolioView();
        portTabTable.initialize();

        portTabTable.setMainPane(((JTotusMethodView) methodTabbedPane).getMainPane());
        ((JTotusMethodView) methodTabbedPane).addComponentToInternalWindow(portTabTable, "PortfolioView");


    }

    public LinkedList<String> getMethodList() {

        //Returns selected methods from methoTabPane
        return ((JTotusMethodView) methodTabbedPane).getSelectedMethods();
    }


    public void checkKeyRingPassword() {
        //Ask for Keyring password first
        JtotusPasswordGUI keyRingGUI = new JtotusPasswordGUI(mainFrame, true);
        keyRingGUI.askForKeyRing();

        JtotusKeyRingPassword password = JtotusKeyRingPassword.getInstance();
        if (password.getKeyRingPassword() == null) {
            keyRingGUI.dispose();
            System.exit(-1);
        }
    }

    public JtotusView(SingleFrameApplication app) {
        super(app);
        mainFrame = app.getMainFrame();

        this.checkKeyRingPassword();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });


    }

    public void setListener(Engine engine) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JtotusView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(JtotusView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JtotusView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(JtotusView.class.getName()).log(Level.SEVERE, null, ex);
        }
        mainEngine = engine;
    }


    @Action
    public Task showConfigView() {
        return new ShowConfigViewTask(getApplication());
    }

    private class ShowConfigViewTask extends org.jdesktop.application.Task<Object, Void> {
        ShowConfigViewTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ShowConfigViewTask fields, here.
            super(app);

            ConfigView configView = new ConfigView(mainFrame, true);
            configView.setVisible(true);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jButton1 = new javax.swing.JButton();
        jButtonRunScripts = new javax.swing.JButton();
        methodTabbedPane = new org.jtotus.gui.JTotusMethodView();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu configMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem configMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        indicatorsMenu = new javax.swing.JMenu();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        infoLable = new javax.swing.JLabel();

        mainPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.jtotus.gui.JtotusApp.class).getContext().getResourceMap(JtotusView.class);
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

        methodTabbedPane.setBorder(new javax.swing.border.MatteBorder(null));
        methodTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        methodTabbedPane.setAutoscrolls(true);
        methodTabbedPane.setName("methodTabbedPane"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(481, Short.MAX_VALUE))
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(methodTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addContainerGap(767, Short.MAX_VALUE)
                                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                        .addComponent(methodTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 762, Short.MAX_VALUE)
                                        .addGap(32, 32, 32)))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jCheckBoxMenuItem1.setSelected(false);
        jCheckBoxMenuItem1.setText(resourceMap.getString("jCheckBoxMenuItem1.text")); // NOI18N
        jCheckBoxMenuItem1.setName("jCheckBoxMenuItem1"); // NOI18N
        jCheckBoxMenuItem1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jCheckBoxMenuItem1MouseReleased(evt);
            }
        });
        fileMenu.add(jCheckBoxMenuItem1);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.jtotus.gui.JtotusApp.class).getContext().getActionMap(JtotusView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        configMenu.setText(resourceMap.getString("configMenu.text")); // NOI18N
        configMenu.setName("configMenu"); // NOI18N

        configMenuItem.setAction(actionMap.get("showConfigView")); // NOI18N
        configMenuItem.setText(resourceMap.getString("configMenuItem.text")); // NOI18N
        configMenuItem.setName("configMenuItem"); // NOI18N
        configMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                configMenuItemMouseClicked(evt);
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                configMenuItemMousePressed(evt);
            }
        });
        configMenu.add(configMenuItem);

        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItem1MousePressed(evt);
            }
        });
        configMenu.add(jMenuItem1);

        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jMenuItem2MousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jMenuItem2MouseReleased(evt);
            }
        });
        configMenu.add(jMenuItem2);

        menuBar.add(configMenu);

        indicatorsMenu.setText(resourceMap.getString("indicatorsMenu.text")); // NOI18N
        indicatorsMenu.setName("indicatorsMenu"); // NOI18N
        indicatorsMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                indicatorsMenuMousePressed(evt);
            }
        });
        menuBar.add(indicatorsMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        infoLable.setText(resourceMap.getString("infoLable.text")); // NOI18N
        infoLable.setName("infoLable"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
                statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
                        .addGroup(statusPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(statusMessageLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 507, Short.MAX_VALUE)
                                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(statusAnimationLabel)
                                .addContainerGap())
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(statusPanelLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(infoLable, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(254, Short.MAX_VALUE)))
        );
        statusPanelLayout.setVerticalGroup(
                statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(statusPanelLayout.createSequentialGroup()
                                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(statusMessageLabel)
                                        .addComponent(statusAnimationLabel)
                                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12))
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(statusPanelLayout.createSequentialGroup()
                                        .addGap(9, 9, 9)
                                        .addComponent(infoLable)
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO Tell engine to start training
//       ConfigLoader<JTable> tableState = new ConfigLoader<JTable>("GuiTableState");
//       tableState.storeConfig(portfolioTable);

        mainEngine.train();


    }//GEN-LAST:event_jButton1MouseClicked

    private void jButtonRunScriptsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRunScriptsMousePressed

    }//GEN-LAST:event_jButtonRunScriptsMousePressed

    private void configMenuItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configMenuItemMouseClicked
        System.out.printf("Staring config view\n");
        ConfigView configView = new ConfigView(mainFrame, true);
        configView.setVisible(true);
    }//GEN-LAST:event_configMenuItemMouseClicked

    private void configMenuItemMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_configMenuItemMousePressed
        // TODO add your handling code here:
        System.out.printf("Staring config view2\n");
        ConfigView configView = new ConfigView(mainFrame, true);
        configView.setVisible(true);
    }//GEN-LAST:event_configMenuItemMousePressed

    private void jMenuItem1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem1MousePressed
        // TODO add your handling code here:
        MethodResultsPrinter printer = (MethodResultsPrinter) methodTabbedPane;
        printer.sendReport();


    }//GEN-LAST:event_jMenuItem1MousePressed

    private void jMenuItem2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem2MousePressed
    }//GEN-LAST:event_jMenuItem2MousePressed

    private void jMenuItem2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem2MouseReleased
        JtotusSetPasswordsGUI passwords = new JtotusSetPasswordsGUI(mainFrame, false);
        passwords.doShow();
    }//GEN-LAST:event_jMenuItem2MouseReleased

    public void fetchGeneratorList() {
        this.indicatorsMenuMousePressed(null);
    }

    private void indicatorsMenuMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_indicatorsMenuMousePressed

        Engine engine = Engine.getInstance();
        Map<String, HashMap<String, TickInterface>> listOfGen = engine.getListOfGenerators();

        for (Map.Entry<String, HashMap<String, TickInterface>> entry : listOfGen.entrySet()) {
            String itemName = entry.getKey();
            boolean itemFound = false;
            int count = indicatorsMenu.getItemCount();
            for (int i = 0; i < count; i++) {
                JMenuItem item = indicatorsMenu.getItem(i);
                if (item.getText().equalsIgnoreCase(itemName)) {
                    itemFound = true;
                    break;
                }
            }

            if (itemFound) {
                continue;
            }

            HashMap<String, TickInterface> generator = entry.getValue();

            //FIXME: add other statements
            Iterator<String> stmts = generator.keySet().iterator();
            TickInterface tickGen = generator.get(stmts.next());

            JCheckBoxMenuItem menu = new JCheckBoxMenuItem();
            ActionListener aListener = new GeneratorActionListener(generator);

            menu.setSelected(false);
            menu.setText(itemName);
            menu.setToolTipText(tickGen.getListnerInfo());

            menu.addActionListener(aListener);
            indicatorsMenu.add(menu);
        }
    }//GEN-LAST:event_indicatorsMenuMousePressed

    private void jCheckBoxMenuItem1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1MouseReleased
        Engine engine = Engine.getInstance();
        if (jCheckBoxMenuItem1.isSelected()) {
            engine.startHistorySimulator();
        } else {
            engine.startMarketTicker();
        }
    }//GEN-LAST:event_jCheckBoxMenuItem1MouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu indicatorsMenu;
    private javax.swing.JLabel infoLable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonRunScripts;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTabbedPane methodTabbedPane;
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


    @Action
    public Task configMenuItemMouseClicked() {
        return new ConfigMenuItemMouseClickedTask(getApplication());
    }

    private class ConfigMenuItemMouseClickedTask extends org.jdesktop.application.Task<Object, Void> {
        ConfigMenuItemMouseClickedTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to ConfigMenuItemMouseClickedTask fields, here.
            super(app);
        }

        @Override
        protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.
            return null;  // return your result
        }

        @Override
        protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
        }
    }
}
