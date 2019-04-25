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
import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.conf.ConfigurationService;

import lombok.Setter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.pmw.tinylog.Logger;

import java.awt.HeadlessException;

public class MainGUI implements DummyInterface {

  @Setter
  private ConfigurationService configService;

  UserInterfaceTracker uiTracker;
  MainGUIFrame window;

  protected void activate(ComponentContext cc) {
    Logger.info("Activated");
    try {
      window = new MainGUIFrame(cc.getBundleContext(), configService);
      window.setVisible(true);
    } catch (HeadlessException he) {
      Logger.warn("No X11 environment present. Starting LectureSight in HEADLESS MODE.");
    }
    uiTracker = new UserInterfaceTracker(cc.getBundleContext());
    uiTracker.open();
  }

  protected void deactivate(ComponentContext cc) {
    uiTracker.close();
    Logger.info("Deactivated");
  }

  void install(UserInterface ui) {
    if (window != null) {
      Logger.info("Installing interface: " + ui.getTitle());
      window.addServiceUI(ui);
    }
  }

  void uninstall(UserInterface ui) {
    if (window != null) {
      Logger.info("Uninstalling interface: " + ui.getTitle());
      window.removeServiceUI(ui);
    }
  }

  private class UserInterfaceTracker extends ServiceTracker {

    UserInterfaceTracker(BundleContext bc) {
      super(bc, UserInterface.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference ref) {
      UserInterface ui = (UserInterface)context.getService(ref);
      install(ui);
      return ui;
    }

    @Override
    public void removedService(ServiceReference ref, Object so) {
      uninstall((UserInterface)so);
    }
  }
}
