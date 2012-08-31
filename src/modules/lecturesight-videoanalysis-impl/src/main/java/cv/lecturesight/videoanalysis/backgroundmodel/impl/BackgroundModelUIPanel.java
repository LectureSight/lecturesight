package cv.lecturesight.videoanalysis.backgroundmodel.impl;

import cv.lecturesight.display.DisplayService;
import javax.swing.UIManager;

public class BackgroundModelUIPanel extends javax.swing.JPanel {
  
  DisplayService dsps;
  
  BackgroundModelUIPanel(DisplayService dsps) {
    this.dsps = dsps;
    
    // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    initComponents();

    viewTabs.add("model", dsps.getDisplayBySID(Constants.WINDOWNAME_MODEL).getDisplayPanel());
    viewTabs.add("diff", dsps.getDisplayBySID(Constants.WINDOWNAME_DIFF).getDisplayPanel());
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    outputPanel = new javax.swing.JPanel();
    viewTabs = new javax.swing.JTabbedPane();
    optionsPanel = new javax.swing.JPanel();

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
      .addComponent(viewTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
    );

    optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

    javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
    optionsPanel.setLayout(optionsPanelLayout);
    optionsPanelLayout.setHorizontalGroup(
      optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 767, Short.MAX_VALUE)
    );
    optionsPanelLayout.setVerticalGroup(
      optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 55, Short.MAX_VALUE)
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

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JPanel outputPanel;
  private javax.swing.JTabbedPane viewTabs;
  // End of variables declaration//GEN-END:variables

}
