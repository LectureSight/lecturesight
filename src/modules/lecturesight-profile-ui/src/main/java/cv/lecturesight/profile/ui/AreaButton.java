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
