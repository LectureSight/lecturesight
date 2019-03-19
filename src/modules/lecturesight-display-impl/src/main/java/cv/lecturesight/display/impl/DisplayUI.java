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
package cv.lecturesight.display.impl;

import cv.lecturesight.display.Display;
import cv.lecturesight.gui.api.UserInterface;

import javax.swing.JPanel;

public class DisplayUI implements UserInterface {

  String title;
  DisplayUIPanel ui;

  public DisplayUI(Display display, String title) {
    this.title = title;
    ui = new DisplayUIPanel(title);
    ui.setDisplay(display);
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public JPanel getPanel() {
    return ui;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }
}
