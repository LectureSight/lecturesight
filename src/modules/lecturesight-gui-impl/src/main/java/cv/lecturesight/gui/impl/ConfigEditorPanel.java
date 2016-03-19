/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package cv.lecturesight.gui.impl;

import cv.lecturesight.util.conf.ConfigurationListener;
import cv.lecturesight.util.conf.ConfigurationService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.pmw.tinylog.Logger;

public class ConfigEditorPanel extends javax.swing.JPanel implements ConfigurationListener {

  private ConfigurationService config;
  private Properties systemConfiguration, systemDefaults;
  private Object[][] data;
  private String[] columns = new String[]{"Key", "Value"};

  /**
   * Creates new form ConfigEditorPanel
   */
  public ConfigEditorPanel(ConfigurationService cs) {
    this.config = cs;
    config.addConfigurationListener(this);
    this.systemConfiguration = config.getSystemConfiguration();
    this.systemDefaults = config.getSystemDefaults();
    
    // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      Logger.warn("Unable to set operating system look-and-feel. " + e.getMessage());
    }
    initComponents();
    update();
  }
  
  public void update() {
    
    List<String> keyList = new LinkedList<String>();
    keyList.addAll(systemConfiguration.stringPropertyNames());
    Collections.sort(keyList);
    data = new Object[keyList.size()][2];
    int i = 0;
    for (String key : keyList) {
      data[i][0] = key;
      data[i++][1] = systemConfiguration.getProperty(key);
    }
    
    configTable.setModel(
            new javax.swing.table.DefaultTableModel(data, columns) {

              Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class
              };
              boolean[] canEdit = new boolean[]{
                false, true
              };

              public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
              }

              public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
              }
            });
    
    configTable.getModel().addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        String key = (String)data[row][0];
        String newValue = (String)configTable.getValueAt(row, 1);
        systemConfiguration.setProperty(key, newValue);
        config.notifyListeners();                                  // TODO ConfigurationService should care for this
        Logger.info(key + " : " + data[row][1] + " => " + newValue);
        data[row][1] = newValue;
      }
    
    });
    
    Logger.debug("Configuration UI updated");
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    scrollPane = new javax.swing.JScrollPane();
    configTable = new javax.swing.JTable();
    jToolBar2 = new javax.swing.JToolBar();
    loadButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();

    configTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {
        "Key", "Value"
      }
    ) {
      Class[] types = new Class [] {
        java.lang.String.class, java.lang.String.class
      };
      boolean[] canEdit = new boolean [] {
        false, true
      };

      public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
      }

      public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
      }
    });
    scrollPane.setViewportView(configTable);

    jToolBar2.setFloatable(false);
    jToolBar2.setRollover(true);

    loadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/folder_wrench.png"))); // NOI18N
    loadButton.setFocusable(false);
    loadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    loadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    loadButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        loadButtonActionPerformed(evt);
      }
    });
    jToolBar2.add(loadButton);

    saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disk.png"))); // NOI18N
    saveButton.setFocusable(false);
    saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveButtonActionPerformed(evt);
      }
    });
    jToolBar2.add(saveButton);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
      .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      try {
        config.loadSystemConfiguration(new FileInputStream(file));
        update();
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Failed to load system configuration:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }//GEN-LAST:event_loadButtonActionPerformed

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (file.exists() && JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(
              this, "The file " + file.getName() + " already exists. Do you want to replace it?", 
              "Replace file", JOptionPane.YES_NO_OPTION)) {
        return;
      }
      try {
        config.saveSystemConfiguration(new FileOutputStream(file));
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Failed to save system configuration:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }//GEN-LAST:event_saveButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTable configTable;
  private javax.swing.JToolBar jToolBar2;
  private javax.swing.JButton loadButton;
  private javax.swing.JButton saveButton;
  private javax.swing.JScrollPane scrollPane;
  // End of variables declaration//GEN-END:variables

  @Override
  public void configurationChanged() {
    update();
  }

  public void deactivate() {
    Logger.debug("Deactivating");
    config.removeConfigurationListener(this);
  }
}
