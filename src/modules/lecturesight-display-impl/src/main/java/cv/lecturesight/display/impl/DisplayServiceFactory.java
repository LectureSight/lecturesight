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

import cv.lecturesight.opencl.OpenCLService;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.pmw.tinylog.Logger;

public class DisplayServiceFactory implements ServiceFactory {

  final static String PROPKEY_AUTOSHOW = "autoshow";

  private OpenCLService ocl;
  BundleContext bundleContext;
  
  Map<DisplayRegistrationImpl, DisplayImpl> displays = new HashMap<DisplayRegistrationImpl, DisplayImpl>();

  public DisplayServiceFactory(BundleContext bc) {
    this.bundleContext = bc;
    registerCommands();
    Logger.info("Activated");
  }

  private void registerCommands() {
    DisplayCommands commandImpl = new DisplayCommands(this);
    Dictionary<String, Object> commands = new Hashtable<String, Object>();
    commands.put("osgi.command.scope", "display");
    commands.put("osgi.command.function", DisplayCommands.commands);
    bundleContext.registerService(DisplayCommands.class.getName(), commandImpl, commands);
  }
  
  public void deactivate() {
    Logger.info("Deactivated");
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    return new DisplayServiceImpl(ocl, this);
  }

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    // TODO dispose all
  }

  public void setOpenCL(OpenCLService service) {
    this.ocl = service;
  }
}
