/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cv.lecturesight.profile.ui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JButton;

/**
 *
 * @author Alex
 */
public class AreaButton extends JButton {

  Color area_color;

  public AreaButton(Color c) {
    super();
    area_color = c;
  }

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(new Color(0,0,0,100));
    g.drawRect(0, 0, this.getWidth()-2, this.getHeight()-2);
    if(this.isFocusOwner()) {
      g.setColor(new Color(0, 0, 0, 15)); }
    else
      g.setColor(new Color(0, 0, 0, 5));

    g.fillRect(0, 0, this.getWidth()-2, this.getHeight()-2);
    g.setColor(area_color);
    g.fillRect((this.getHeight()-10)/2, (this.getHeight()-10)/2, 10, 10);

    g.setColor(new Color(0, 0, 0, 200));
    g.drawString(this.getText(), 30, this.getHeight()-10);
  }
}
