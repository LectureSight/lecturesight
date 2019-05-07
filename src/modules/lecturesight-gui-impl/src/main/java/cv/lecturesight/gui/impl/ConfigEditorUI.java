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
package cv.lecturesight.gui.impl;

import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.util.conf.ConfigurationService;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import javax.swing.JPanel;

public class ConfigEditorUI implements UserInterface {

  ConfigEditorPanel editorPanel;

  @Setter
  ConfigurationService configService;

  protected void activate(ComponentContext cc) {
    editorPanel = new ConfigEditorPanel(configService);
    Logger.info("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    editorPanel.deactivate();
    Logger.info("Deactivated");
  }

  @Override
  public String getTitle() {
    return "System Configuration";
  }

  @Override
  public JPanel getPanel() {
    return editorPanel;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }
}
