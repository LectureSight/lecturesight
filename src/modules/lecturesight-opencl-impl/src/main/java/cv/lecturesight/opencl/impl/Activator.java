package cv.lecturesight.opencl.impl;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.Triggerable;
import cv.lecturesight.opencl.impl.profiling.ProfilingServer;
import cv.lecturesight.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public final class Activator implements BundleActivator, ServiceFactory {

  private final static String PROPKEY_DOPROFILING = "cv.lecturesight.profiling";
  private Log log = new Log("OpenCL Service");
  private BundleContext bundleContext;
  private CLContext oclContext;
  private CLQueue oclQueue;
  private OCLSignalDispatcher dispatcher;
  private OCLExecutor executor;
  private ProfilingServer profiler;

  @Override
  public void start(BundleContext context) throws Exception {
    bundleContext = context;

    // initialize OpenCL and print device report
    oclContext = initOpenCL();
    log.info(generateDeviceReport(oclContext.getPlatform().getBestDevice()));

    // set up CL Command Queue
    oclQueue = oclContext.createDefaultQueue();  
    
    // set up Execution
    dispatcher = new OCLSignalDispatcher();
    executor = new OCLExecutor(oclQueue); 
    executor.start();
    dispatcher.start();
    
    // set up profiler if configured
    if (profilingOn()) {
      log.info("Profiling is enabled!");
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
    log.info("Activated");
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    log.info("Shutting down");
    if (profilingOn()) {
      profiler.shutdown();
    }
    dispatcher.shutdown();
    executor.shutdown();
    oclContext.release();
  }

  /** Initialize the OpenCL Context
   * 
   * @return
   * @throws Exception
   */
  private CLContext initOpenCL() throws Exception {
//    Class.forName("com.nativelibs4java.opencl.JavaCL");
    CLContext ctx = null;

    // Try to create OpenGL shared context if demanded
//    if ( config.getBoolean(...) ) {
//      try {
//        ctx = JavaCL.createContextFromCurrentGL();
//        log.info("Successfully created OpenCL context from current OpenGL context.");
//      } catch (RuntimeException e) {
//        log.warn("Unable to create OpenCL context from current OpenGL context.");
//        ctx = null;
//      }
//    }

    // Try to create standard context if creation of OpenGL shared context failed or was not demanded
    if (ctx == null) {
      try {
        ctx = JavaCL.createBestContext();
      } catch (Exception e) {
        throw new IllegalStateException("Unable to create valid OpenCL context!");
      }
    }

    try {
      CLPlatform platform = ctx.getPlatform();
      log.info("OpenCL platform: " + platform.getName() + " " + platform.getVersion() + " " + platform.getProfile());
    } catch (Exception e) {   // this will also catch the case where ctx == null
      throw new IllegalStateException("Unable to query context for OpenCL platform!", e);
    }

    return ctx;
  }

  private String generateDeviceReport(CLDevice device) {
    final float KILOBYTE = 1024;
    final float MEGABYTE = 1024 * KILOBYTE;
    String out = "Device report:\n\n";
    out += " " + device.getVendor() + " " + device.getName() + " (driver version: " + device.getDriverVersion() + ")\n\n";
    out += " Compute units :  " + device.getMaxComputeUnits() + " at " + device.getMaxClockFrequency() + " MHz max\n\n";
    out += "      Memories :  global   : " + (device.getGlobalMemSize() / MEGABYTE) + " MB\n";
    out += "                  constant : " + (device.getMaxConstantBufferSize() / KILOBYTE) + " KB\n";
    out += "                  local    : " + (device.getLocalMemSize() / KILOBYTE) + " KB\n\n";
    out += "    Workgroups :  " + device.getMaxWorkGroupSize() + " threads max in " + device.getMaxWorkItemDimensions() + " dimensions\n";
    out += " 2D Image size :  " + device.getImage2DMaxWidth() + "x" + device.getImage2DMaxHeight() + " max\n";
    return out;
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    log.debug("Creating new OpenCLService instance for " + bundle.getSymbolicName());

    OpenCLServiceImpl serviceInstance = new OpenCLServiceImpl();
    Map<String, CLProgram> programs = buildPrograms(bundle.findEntries("opencl", "*.cl", false));
    serviceInstance.context = oclContext;
    serviceInstance.programs = new SimpleOCLProgramStore(programs);
    serviceInstance.executor = executor;
    serviceInstance.dispatcher = dispatcher;
    
    if (profilingOn()) {
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
          log.info("Compiling: " + sourceUrl.toString());

          InputStream in = sourceUrl.openStream();
          CLProgram prog = oclContext.createProgram(IOUtils.readTextClose(in));
          prog.build();
          out.put(programName, prog);
        } catch (IOException e) {
          throw new ServiceException("Error reading resource.", ServiceException.FACTORY_ERROR, e);
        } catch (CLBuildException e) {
          log.warn("Could not build " + programName + "\n\n" + e.getMessage());
          throw new ServiceException("Failed to compile OpenCL source.", ServiceException.FACTORY_ERROR, e);
        }
      }
    }
    return out;
  }
  
  public static boolean profilingOn() {
    try {
      String pvalue = System.getProperty(PROPKEY_DOPROFILING);
      return (pvalue.equalsIgnoreCase("true") || pvalue.equalsIgnoreCase("yes") || pvalue.equalsIgnoreCase("on"));
    } catch (Exception e) {
      return false;
    }
  }
}
