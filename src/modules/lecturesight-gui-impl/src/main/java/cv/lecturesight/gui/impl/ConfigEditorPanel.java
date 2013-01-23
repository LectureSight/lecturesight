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

import cv.lecturesight.util.Log;
import java.util.Properties;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class ConfigEditorPanel extends javax.swing.JPanel {

  private Log log;
  private Properties systemConfiguration;
  private Object[][] data;
  private String[] columns = new String[]{"Key", "Value"};

  /**
   * Creates new form ConfigEditorPanel
   */
  public ConfigEditorPanel(Properties config, Log log) {
    this.log = log;
    this.systemConfiguration = config;
    // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    initComponents();
    update();
  }

  public void update() {
    
    Set<String> keySet = systemConfiguration.stringPropertyNames();
    data = new Object[keySet.size()][2];
    int i = 0;
    for (String key : keySet) {
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
        log.info(key + " : " + data[row][1] + " => " + newValue);
        data[row][1] = newValue;
      }
    
    });
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    scrollPane = new javax.swing.JScrollPane();
    configTable = new javax.swing.JTable();
    jToolBar2 = new javax.swing.JToolBar();

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

    jToolBar2.setRollover(true);

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
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTable configTable;
  private javax.swing.JToolBar jToolBar2;
  private javax.swing.JScrollPane scrollPane;
  // End of variables declaration//GEN-END:variables
}
