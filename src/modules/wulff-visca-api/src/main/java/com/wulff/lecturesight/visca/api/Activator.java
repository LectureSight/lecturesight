package com.wulff.lecturesight.visca.api;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

  @Override
  public void start(BundleContext bc) throws Exception {
    System.loadLibrary("librxtxSerial.so");
  }

  @Override
  public void stop(BundleContext bc) throws Exception {
  }  
}
