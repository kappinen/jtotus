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

/*
 * ConfigView.java
 *
 * Created on Oct 9, 2010, 5:33:45 PM
 */

package org.jtotus.gui;

import net.sf.nachocalendar.table.JTableCustomizer;
import org.dom4j.Document;
import org.jtotus.config.ConfigLoader;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfigView extends javax.swing.JDialog {
    private Document currentDocument = null;
    private String currentDocumentName = null;

//http://www.javaworld.com/javaworld/javatips/jw-javatip102.html
//http://forums.devshed.com/java-help-9/datepicker-inside-a-jtable-536152.html
 class DateCellEditor extends AbstractCellEditor implements TableCellEditor {
    private Date currentDate;
    private JSpinner spinner;

    protected static final String EDIT = "edit";

    
    public DateCellEditor() {

        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        SpinnerModel dateModel = new SpinnerDateModel(initDate,
                                                    earliestDate,
                                                    latestDate,
                                                    Calendar.YEAR);//ignored for user input
        spinner = new JSpinner(dateModel);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd.MM.yyyy"));
    }


    // Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
        currentDate = ((SpinnerDateModel)spinner.getModel()).getDate();
        return ((SpinnerDateModel)spinner.getModel()).getDate();

    }

     // Implement the one method defined by TableCellEditor.
     public Component getTableCellEditorComponent(javax.swing.JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  int row, int column) {
//         DateFieldTableEditor editor = new DateFieldTableEditor();
//         if (value.getClass() == Date.class);

         currentDate = (Date) value;
         spinner.setValue(value);
         return spinner;
     }

}


   // http://docstore.mik.ua/orelly/java-ent/jfc/ch03_19.htm
    class FileTreeModel implements TreeModel {
      // We specify the root directory when we create the model.
      protected File root;

      public FileTreeModel(File root) { this.root = root; }

      // The model knows how to return the root object of the tree
      public Object getRoot() { return root; }

      // Tell JTree whether an object in the tree is a leaf
      public boolean isLeaf(Object node) {  return ((File)node).isFile(); }

      // Tell JTree how many children a node has
      public int getChildCount(Object parent) {
        String[] children = ((File)parent).list();
        if (children == null) return 0;
        return children.length;
      }

      // Fetch any numbered child of a node for the JTree.
      // Our model returns File objects for all nodes in the tree.  The
      // JTree displays these by calling the File.toString() method.
      public Object getChild(Object parent, int index) {
        String[] children = ((File)parent).list();
        if ((children == null) || (index >= children.length)) return null;
        return new File((File) parent, children[index]);
      }

      // Figure out a child's position in its parent node.
      public int getIndexOfChild(Object parent, Object child) {
        String[] children = ((File)parent).list();
        if (children == null) return -1;
        String childname = ((File)child).getName();
        for(int i = 0; i < children.length; i++) {
          if (childname.equals(children[i])) return i;
        }
        return -1;
     }
      // This method is invoked by the JTree only for editable trees.
      // This TreeModel does not allow editing, so we do not implement
      // this method.  The JTree editable property is false by default.
      public void valueForPathChanged(TreePath path, Object newvalue) {}

      // Since this is not an editable tree model, we never fire any events,
      // so we don't actually have to keep track of interested listeners
      public void addTreeModelListener(TreeModelListener l) {}
      public void removeTreeModelListener(TreeModelListener l) {}
    }


    class FileSelectionlListener implements TreeSelectionListener {
       private Object config = null;

        public void valueChanged(TreeSelectionEvent e) {

            if (e == null || e.getNewLeadSelectionPath() == null) {
                return;
            }
            
           Object obj = e.getNewLeadSelectionPath().getLastPathComponent();

           ConfigLoader<Object> configLoader = new ConfigLoader<Object>(null);

           String fileName = obj.toString();


           if (fileName.endsWith(".xml")){

                DefaultTableModel model = (DefaultTableModel) confTable.getModel();
                config = configLoader.readObj(fileName);
                
                model.setNumRows(0);
                Object toInsert = null;
                Field [] fields = getConfig().getClass().getDeclaredFields();
                for (int i=0;i < fields.length;i++) {
                    try {

                        if (fields[i].getType() == Calendar.class){
                            Calendar cal = (Calendar)fields[i].get(getConfig());

                            //DateCellEditor editor = new DateCellEditor();
//                            confTable.getEditorComponent();
//                            confTable.setDefaultEditor(confTable.getColumnClass(1), editor);
                            JTableCustomizer.setDefaultEditor(confTable);
                            //DateField datefield = CalendarFactory.createDateField();
                            //datefield.setValue(cal.getTime());
                            //JTableCustomizer.setEditorForRow(confTable, 1);
                            model.addRow(new Object[]{fields[i].getName(), cal.getTime()});

                            continue;
                        }else {
                            toInsert = fields[i].get(getConfig());
                        }

                        model.addRow(new Object[]{fields[i].getName(), toInsert});
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                confLabel.setText(fileName);
            }
         
        }

        /**
         * @return the config
         */
        public Object getConfig() {
            return config;
        }

        /**
         * @param config the config to set
         */
        public void setConfig(Object config) {
            this.config = config;
        }

   }

    /** Creates new form ConfigView */
    public ConfigView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        FileTreeModel model = new FileTreeModel(new File("config"));
        
        configTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        configTree.addTreeSelectionListener(new FileSelectionlListener());
        configTree.setModel(model);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jScrollPane1 = new javax.swing.JScrollPane();
        configTree = new javax.swing.JTree();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAndRunButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        confTable = new javax.swing.JTable();
        confLabel = new javax.swing.JLabel();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.jtotus.gui.JtotusApp.class).getContext().getResourceMap(ConfigView.class);
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jPopupMenu1.add(jMenuItem1);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jPopupMenu1.add(jSeparator1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        configTree.setName("ConfigView");
        jScrollPane1.setViewportView(configTree);

        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelButtonMouseClicked(evt);
            }
        });

        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                saveButtonMouseClicked(evt);
            }
        });

        saveAndRunButton.setText(resourceMap.getString("saveAndRunButton.text")); // NOI18N
        saveAndRunButton.setName("saveAndRunButton"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        confTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Parameter Name", "Default Values"
            }
        ));
        confTable.setName("confTable"); // NOI18N
        jScrollPane2.setViewportView(confTable);
        confTable.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("confTable.columnModel.title0")); // NOI18N
        confTable.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("confTable.columnModel.title1")); // NOI18N

        confLabel.setText(resourceMap.getString("confLabel.text")); // NOI18N
        confLabel.setName("confLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(confLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveAndRunButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveAndRunButton)
                    .addComponent(saveButton)
                    .addComponent(cancelButton)
                    .addComponent(confLabel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseClicked
         this.processWindowEvent(
            new WindowEvent(
                  this, WindowEvent.WINDOW_CLOSING));
          dispose();
    }//GEN-LAST:event_cancelButtonMouseClicked

    private void saveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveButtonMouseClicked

            DefaultTableModel model = (DefaultTableModel) confTable.getModel();

            String currentFile = confLabel.getText();

            ConfigLoader<Object> loader = new ConfigLoader<Object>(null);
            Object config = loader.readObj(currentFile);

            Field [] fields = config.getClass().getDeclaredFields();
            for(int i = 0;i<model.getRowCount();i++){
                Object param = model.getValueAt(i, 0);
                String paramName = param.toString();
                for(int y=0;y<fields.length;y++) {   
                    System.out.printf("Param name:%s fields:%s\n",paramName, fields[y].getName());
                    if(paramName.compareTo(fields[y].getName())==0){
                        try {
                            Object newValue = model.getValueAt(i, 1);
                       //     System.out.printf("Type name:%s fields:%s\n",fields[y].getType().getName(), newValue.getClass().getName());
                            if (newValue == null)
                                continue;
                            
                            if(fields[y].getType() == newValue.getClass()){
                                 System.out.printf("Writtign name:%s fields:%s\n",fields[y].getType().getName(), newValue.getClass().getName());
                                fields[y].set(config, newValue);
                            }else if (fields[y].getType() == int.class){
                                fields[y].setInt(config, Integer.parseInt(newValue.toString()));
                            }else if(fields[y].getType() == boolean.class){
                                fields[y].setBoolean(config, Boolean.parseBoolean(newValue.toString()));
                            }
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            loader.writeObj(config, currentFile);
            
       
    }//GEN-LAST:event_saveButtonMouseClicked

    /**
    * @param args the command line arguments
    */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                ConfigView dialog = new ConfigView(new javax.swing.JFrame(), true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel confLabel;
    private javax.swing.JTable confTable;
    private javax.swing.JTree configTree;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JButton saveAndRunButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

}
