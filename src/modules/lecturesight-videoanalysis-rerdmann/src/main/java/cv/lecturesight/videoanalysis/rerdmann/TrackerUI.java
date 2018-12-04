package cv.lecturesight.videoanalysis.rerdmann;

import cv.lecturesight.gui.api.UserInterface;

import javax.swing.JPanel;

public class TrackerUI implements UserInterface {

  VideoAnalysisRErdmann parent;
  JPanel panel;

  public TrackerUI(VideoAnalysisRErdmann parent) {
    this.parent = parent;
    panel = new TrackerUIPanel(parent);
  }

  @Override
  public String getTitle() {
    return "Object Tracker";
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }
}
