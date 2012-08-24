package cv.lecturesight.videoanalysis.change.impl;

import java.awt.BorderLayout;
import javax.swing.UIManager;

public class ChangeDetectorUIFrame extends javax.swing.JPanel {

  ChangeDetectUI parent;
  
  public ChangeDetectorUIFrame(ChangeDetectUI parent) {
    this.parent = parent;
     // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    initComponents();
    DisplayPanel display = new DisplayPanel();
    displayContainer.setLayout(new BorderLayout());
    displayContainer.add(display, BorderLayout.CENTER);
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    outputSelectButtonGroup = new javax.swing.ButtonGroup();
    outputPanel = new javax.swing.JPanel();
    outputSelectRaw = new javax.swing.JRadioButton();
    outputSelectDilated = new javax.swing.JRadioButton();
    displayContainer = new javax.swing.JPanel();
    optionsPanel = new javax.swing.JPanel();
    thresholdLabel = new javax.swing.JLabel();
    thresholdSlider = new javax.swing.JSlider();

    outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

    outputSelectButtonGroup.add(outputSelectRaw);
    outputSelectRaw.setSelected(true);
    outputSelectRaw.setText("raw");

    outputSelectButtonGroup.add(outputSelectDilated);
    outputSelectDilated.setText("dilated (1 pass, 8-neighbourhood)");

    javax.swing.GroupLayout displayContainerLayout = new javax.swing.GroupLayout(displayContainer);
    displayContainer.setLayout(displayContainerLayout);
    displayContainerLayout.setHorizontalGroup(
      displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 767, Short.MAX_VALUE)
    );
    displayContainerLayout.setVerticalGroup(
      displayContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 217, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
    outputPanel.setLayout(outputPanelLayout);
    outputPanelLayout.setHorizontalGroup(
      outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(outputPanelLayout.createSequentialGroup()
        .addComponent(outputSelectRaw)
        .addGap(18, 18, 18)
        .addComponent(outputSelectDilated)
        .addContainerGap(474, Short.MAX_VALUE))
      .addComponent(displayContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    outputPanelLayout.setVerticalGroup(
      outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputPanelLayout.createSequentialGroup()
        .addComponent(displayContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(outputSelectRaw)
          .addComponent(outputSelectDilated)))
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
        .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

private void thresholdSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdSliderStateChanged
  parent.setThreshold(thresholdSlider.getValue());
}//GEN-LAST:event_thresholdSliderStateChanged

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel displayContainer;
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JPanel outputPanel;
  private javax.swing.ButtonGroup outputSelectButtonGroup;
  private javax.swing.JRadioButton outputSelectDilated;
  private javax.swing.JRadioButton outputSelectRaw;
  private javax.swing.JLabel thresholdLabel;
  private javax.swing.JSlider thresholdSlider;
  // End of variables declaration//GEN-END:variables

}
