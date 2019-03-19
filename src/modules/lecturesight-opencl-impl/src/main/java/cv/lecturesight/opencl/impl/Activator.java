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
package cv.lecturesight.opencl.impl;

import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.opencl.impl.profiling.ProfilingServer;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLException;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLMem.Flags;
import com.nativelibs4java.opencl.CLMem.ObjectType;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLPlatform.DeviceComparator;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Activator implements BundleActivator, ServiceFactory {

  private final static String PROPKEY_DOPROFILING = "ocl.profiling";
  private final static String PROPKEY_DEVICE_TYPE = "ocl.device.type";
  private final static String PROPKEY_USE_GL = "ocl.use.gl";
  private BundleContext bundleContext;
  private CLContext oclContext;
  private CLQueue oclQueue;
  private OCLSignalDispatcher dispatcher;
  private OCLExecutor executor;
  private ProfilingServer profiler;

  @Override
  public void start(BundleContext context) throws Exception {
    bundleContext = context;

    // initialize OpenCL
    oclContext = initOpenCL();

    // Device report for all available devices
    CLDevice[] devices = oclContext.getPlatform().listAllDevices(true);
    for (CLDevice device : devices) {
      Logger.info(generateDeviceReport(device));
    }

    // Device which will be used
    devices = oclContext.getDevices();
    for (CLDevice device : devices) {
      Logger.info("Selected OpenCL device: " + device.getVendor() + " " + device.getName());
    }

    // Check supported image formats for the selected device
    if (Logger.getLevel() == Level.TRACE) {
      StringBuilder formatlist = new StringBuilder();
      CLImageFormat[] supportedImageFormats = oclContext.getSupportedImageFormats(Flags.ReadWrite, ObjectType.Image2D);
      for (CLImageFormat format : supportedImageFormats) {
        formatlist.append(format);
        formatlist.append("\n");
      }
      Logger.trace("Supported image formats ({}):\n\n{}", supportedImageFormats.length, formatlist);
    }

    // set up CL Command Queue
    oclQueue = oclContext.createDefaultQueue();

    // set up Execution
    dispatcher = new OCLSignalDispatcher();
    executor = new OCLExecutor(oclQueue);
    executor.start();
    dispatcher.start();

    // set up profiler if configured
    if (configured(PROPKEY_DOPROFILING)) {
      Logger.info("Profiling is enabled!");
      profiler = new ProfilingServer();
      OCLSignal SIG_nextFrame = dispatcher.signalManager.createSignal("BEGIN-FRAME");
      dispatcher.signalManager.registerWithSignal(SIG_nextFrame, new Triggerable() {
        @Override
        public void triggered(OCLSignal signal) {
          profiler.nextFrame();
        }
      });
      profiler.start();
    }

    // create CLUtils
    Map<String, CLProgram> progs = buildPrograms(context.getBundle().findEntries("opencl", "*.cl", false));
    OCLUtilsImpl utils = new OCLUtilsImpl(oclQueue, progs.get("imageutils"));
    OpenCLServiceImpl.utils = utils;

    // set up service factory
    bundleContext.registerService(OpenCLService.class.getName(), this, null);
    Logger.info("Activated");
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    Logger.info("Shutting down");
    if (configured(PROPKEY_DOPROFILING)) {
      profiler.shutdown();
    }
    dispatcher.shutdown();
    executor.shutdown();
    oclContext.release();
  }

  /**
   * Initialize the OpenCL Context
   *
   * @return
   * @throws Exception
   */
  private CLContext initOpenCL() throws Exception {
    CLContext ctx = null;

    // create OpenGL shared context if configured...
    if (configured(PROPKEY_USE_GL)) {

      try {
        ctx = JavaCL.createContextFromCurrentGL();
        Logger.info("Successfully created OpenCL context from current OpenGL context.");
      } catch (RuntimeException e) {
        Logger.warn("Unable to create OpenCL context from current OpenGL context.");
        ctx = null;
      }

    } else {    // ...create configured/default compute device otherwise

      CLDevice.Type type = configuredDeviceType();
      try {
        CLPlatform[] platforms = JavaCL.listPlatforms();
        List<CLDevice> devices = new LinkedList();

        // find all available devices of configured type
        for (CLPlatform platform : platforms) {
          try {
            devices.addAll(Arrays.asList(platform.listDevices(type, true)));
          } catch (CLException cle) {
            Logger.trace("No devices available of type {} available on platform: {}", type.name(), platform);
          }
        }

        // find best device
        Iterator<CLDevice> it = devices.iterator();
        CLDevice best = it.next();
        if (it.hasNext()) {
          List<CLPlatform.DeviceFeature> features = new LinkedList();
          features.add(CLPlatform.DeviceFeature.MaxComputeUnits);
          DeviceComparator comp = new DeviceComparator(features);
          while (it.hasNext()) {
            CLDevice device = it.next();
            best = comp.compare(device, best) > 0 ? device : best;
          }
        }

        // FIXME provide ContextProperties?
        ctx = JavaCL.createContext(null, best);
      } catch (Throwable t) {
        Logger.error(t, "!! OPENCL INITIALIZATION FAILED !! ");
        // This is a fatal error, so exit immediately
        bundleContext.getBundle(0).stop();

        Exception ex = new IllegalStateException("No suitable compute device found: " + type.name());
        throw ex;
      }
    }

    // testing if context is working by getting platform information
    try {
      CLPlatform platform = ctx.getPlatform();
      Logger.info("OpenCL platform: " + platform.getName() + " " + platform.getVersion() + " " + platform.getProfile());
    } catch (Exception e) {
      throw new IllegalStateException("Unable to query context for OpenCL platform!", e);
    }

    return ctx;
  }

  private String generateDeviceReport(CLDevice device) {
    final float kiloByte = 1024;
    final float megaByte = 1024 * kiloByte;
    String out = "OpenCL device report:\n\n";

    out += " " + device.getVendor() + " " + device.getName() + " (driver version: " + device.getDriverVersion() + ")\n\n";
    out += " Compute units :  " + device.getMaxComputeUnits() + " at " + device.getMaxClockFrequency() + " MHz max\n\n";
    out += "      Memories :  global   : " + (device.getGlobalMemSize() / megaByte) + " MB\n";
    out += "                  constant : " + (device.getMaxConstantBufferSize() / kiloByte) + " KB\n";
    out += "                  local    : " + (device.getLocalMemSize() / kiloByte) + " KB\n\n";
    out += "    Workgroups :  " + device.getMaxWorkGroupSize() + " threads max in " + device.getMaxWorkItemDimensions() + " dimensions\n";
    out += " 2D Image size :  " + device.getImage2DMaxWidth() + "x" + device.getImage2DMaxHeight() + " max\n";

    long[] workItemSizes = device.getMaxWorkItemSizes();
    out += "Work item sizes:  ";

    for (long size : workItemSizes) {
      out += size + " ";
    }
    out += "\n";

    return out;
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    Logger.debug("Creating new OpenCLService instance for " + bundle.getSymbolicName());

    OpenCLServiceImpl serviceInstance = new OpenCLServiceImpl();
    Map<String, CLProgram> programs = buildPrograms(bundle.findEntries("opencl", "*.cl", false));
    serviceInstance.context = oclContext;
    serviceInstance.programs = new SimpleOCLProgramStore(programs);
    serviceInstance.executor = executor;
    serviceInstance.dispatcher = dispatcher;

    if (configured(PROPKEY_DOPROFILING)) {
      serviceInstance.profiler = profiler;
      serviceInstance.doProfiling = true;
    }

    return serviceInstance;
  }

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    //TODO: Implement OpenCL.ungetService !!
    return;
  }

  private Map<String, CLProgram> buildPrograms(Enumeration sourceURLs) {
    Map<String, CLProgram> out = new HashMap<String, CLProgram>();

    if (sourceURLs != null) {
      while (sourceURLs.hasMoreElements()) {

        URL sourceUrl = (URL) sourceURLs.nextElement();
        File sourceFile = new File(sourceUrl.getFile());
        String filename = sourceFile.getName();
        String programName = filename.substring(0, filename.length() - 3);

        try {
          Logger.info("Compiling: " + sourceUrl.toString());

          InputStream in = sourceUrl.openStream();
          CLProgram prog = oclContext.createProgram(IOUtils.readTextClose(in));
          prog.build();
          out.put(programName, prog);
        } catch (IOException e) {
          throw new ServiceException("Error reading resource.", ServiceException.FACTORY_ERROR, e);
        } catch (CLBuildException e) {
          Logger.warn("Could not build " + programName + "\n\n" + e.getMessage());
          throw new ServiceException("Failed to compile OpenCL source.", ServiceException.FACTORY_ERROR, e);
        }
      }
    }
    return out;
  }

  /**
   * Returns <code>true</code> if system property <code>propkey</code> is "true"|"yes"|"on".
   *
   * @param propkey name of system property
   * @return true if configured, false otherwise
   */
  public static boolean configured(String propkey) {
    try {
      String pvalue = System.getProperty(propkey);
      return ("true".equalsIgnoreCase(pvalue) || "yes".equalsIgnoreCase(pvalue) || "on".equalsIgnoreCase(pvalue));
    } catch (Exception e) {
      return false;
    }
  }

  public static CLDevice.Type configuredDeviceType() {
    try {
      String strval = System.getProperty(PROPKEY_DEVICE_TYPE);
      return CLDevice.Type.valueOf(strval);
    } catch (Exception e) {
      return CLDevice.Type.GPU;   // defaults is GPU
    }
  }
}
