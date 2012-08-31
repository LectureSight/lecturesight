package cv.lecturesight.display;

import javax.swing.JLabel;

public abstract class DisplayPanel extends JLabel {
  
  private CustomRenderer renderer = null;
  
  public boolean hasCustomRenderer() {
    return renderer != null;
  }
  
  public CustomRenderer getCustomRenderer() {
    return renderer;
  }
  
  public void setCustomRenderer(CustomRenderer renderer) {
    this.renderer = renderer;
  }
}
