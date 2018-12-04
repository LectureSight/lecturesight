package cv.lecturesight.videoanalysis.templ;

import cv.lecturesight.gui.api.UserInterface;

import javax.swing.JPanel;

public class TrackerUI implements UserInterface {

  VideoAnalysisTemplateMatching parent;
  JPanel panel;

  public TrackerUI(VideoAnalysisTemplateMatching parent) {
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
