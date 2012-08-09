package cv.lecturesight.ptz.steering.impl;

import javax.swing.JFrame;

class CameraMovementUI {

  private JFrame window = new JFrame();
  private CameraControlPanel controlPanel;
  
  public CameraMovementUI(CameraMovementModel model) {
    controlPanel = new CameraControlPanel(model);
    window.setTitle("PTZ Conrtol:  " + model.getCameraName());
    window.add(controlPanel);
    window.pack();
  }
  
  public void show(boolean show) {
    window.setVisible(show);
  }
  
  public void update() {
    controlPanel.repaint();
  }
}
