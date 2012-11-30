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
package cv.lecturesight.videoanalysis.change.impl;

import javax.swing.UIManager;

public class ChangeDetectorUIFrame extends javax.swing.JPanel {

  private enum Views {RAW,DILATED};
  
  ChangeDetectUI parent;
  
  public ChangeDetectorUIFrame(ChangeDetectUI parent) {
    this.parent = parent;
    
    // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    initComponents();
        
    viewTabs.add("raw", parent.dsps.getDisplayBySID(Constants.WINDOWNAME_CHANGE_RAW).getDisplayPanel());
    viewTabs.add("dilated", parent.dsps.getDisplayBySID(Constants.WINDOWNAME_CHANGE_DILATED).getDisplayPanel());
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    outputPanel = new javax.swing.JPanel();
    viewTabs = new javax.swing.JTabbedPane();
    optionsPanel = new javax.swing.JPanel();
    thresholdLabel = new javax.swing.JLabel();
    thresholdSlider = new javax.swing.JSlider();

    outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

    viewTabs.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

    javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
    outputPanel.setLayout(outputPanelLayout);
    outputPanelLayout.setHorizontalGroup(
      outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(viewTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
    );
    outputPanelLayout.setVerticalGroup(
      outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(viewTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
    );

    optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

    thresholdLabel.setText("Threshold:");

    thresholdSlider.setMaximum(255);
    thresholdSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        thresholdSliderStateChanged(evt);
      }
    });

    javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
    optionsPanel.setLayout(optionsPanelLayout);
    optionsPanelLayout.setHorizontalGroup(
      optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(optionsPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(thresholdLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(thresholdSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
        .addContainerGap())
    );
    optionsPanelLayout.setVerticalGroup(
      optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(optionsPanelLayout.createSequentialGroup()
        .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(optionsPanelLayout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addComponent(thresholdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
          .addComponent(thresholdSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(optionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

private void thresholdSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdSliderStateChanged
  parent.setThreshold(thresholdSlider.getValue());
}//GEN-LAST:event_thresholdSliderStateChanged

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JPanel outputPanel;
  private javax.swing.JLabel thresholdLabel;
  private javax.swing.JSlider thresholdSlider;
  private javax.swing.JTabbedPane viewTabs;
  // End of variables declaration//GEN-END:variables

}
