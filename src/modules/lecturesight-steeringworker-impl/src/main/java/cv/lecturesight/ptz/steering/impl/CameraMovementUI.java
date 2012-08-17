package cv.lecturesight.ptz.steering.impl;

import javax.swing.JFrame;

class CameraMovementUI {

  private JFrame window = new JFrame();
  private CameraControlPanel controlPanel;
  
  public CameraMovementUI(CameraPositionModel model) {
    controlPanel = new CameraControlPanel(model);
    window.setTitle("PTZ Control:  " + model.getCameraName());
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
