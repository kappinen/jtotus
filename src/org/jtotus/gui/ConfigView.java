/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConfigView.java
 *
 * Created on Oct 9, 2010, 5:33:45 PM
 */

package org.jtotus.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfigView extends javax.swing.JDialog {
    private Document currentDocument = null;
    private String currentDocumentName = null;

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

        public void valueChanged(TreeSelectionEvent e) {

            if (e == null || e.getNewLeadSelectionPath() == null) {
                return;
            }
            
           Object obj = e.getNewLeadSelectionPath().getLastPathComponent();

           String fileName = obj.toString();
           if (fileName.endsWith(".xml")){
               currentDocumentName = fileName;
               File confFile = new File(currentDocumentName);
               if(confFile.isFile()){
                    try {
                        confLabel.setText(currentDocumentName);
                        String content = FileUtils.readFileToString(confFile);
                        Document document = DocumentHelper.parseText(content);
                        Element root = document.getRootElement();


                        //clean table model
                        DefaultTableModel model = (DefaultTableModel) confTable.getModel();
                        model.setNumRows(0);
                        for ( Iterator i = root.elementIterator(); i.hasNext(); ) {
                            Element element = (Element) i.next();


                            Object vo = element.getData();
                            System.out.printf("Got from xml:%s:type:%s\n", element.getName(), vo.getClass().getName());

                            model.addRow(new Object[] {element.getName(), element.getData()});



                        }
                    } catch (DocumentException ex) {
                        Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
                    }
               }
            }
         
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


        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        configTree = new javax.swing.JTree();
        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAndRunButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        confTable = new javax.swing.JTable();
        confLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        configTree.setName("ConfigView");
        jScrollPane1.setViewportView(configTree);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.jtotus.gui.JtotusApp.class).getContext().getResourceMap(ConfigView.class);
        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cancelButtonMouseClicked(evt);
                cancelButtonMouseClicked1(evt);
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

    }//GEN-LAST:event_cancelButtonMouseClicked

    private void cancelButtonMouseClicked1(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseClicked1
        dispose();
    }//GEN-LAST:event_cancelButtonMouseClicked1

    private void saveButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveButtonMouseClicked
        try {
            DefaultTableModel model = (DefaultTableModel) confTable.getModel();
            //Write document to XML
            File doc = new File(currentDocumentName);
            XMLWriter writer = new XMLWriter(new FileWriter(doc));
            writer.write(currentDocument);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ConfigView.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton saveAndRunButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

}
