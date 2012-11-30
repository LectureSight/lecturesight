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
package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.ptz.steering.ui", immediate = true)
@Service
public class CameraMovementUI implements UserInterface {

  @Reference
  CameraSteeringWorker worker;
//  private JFrame window = new JFrame();
  private CameraControlPanel controlPanel;
  
  protected void activate(ComponentContext cc) {
    controlPanel = ((CameraSteeringWorkerImpl)worker).getControlPanel();
//    window.setTitle("PTZ Camera Control");
//    window.add(controlPanel);
//    window.pack();
  }

  @Override
  public JPanel getPanel() {
    return controlPanel;
  }

  @Override
  public String getTitle() {
    return "PTZ Camera Control";
  }

  @Override
  public boolean isResizeable() {
    return true;
  }
}
