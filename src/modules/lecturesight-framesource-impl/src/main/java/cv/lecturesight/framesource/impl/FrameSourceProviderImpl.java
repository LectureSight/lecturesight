package cv.lecturesight.framesource.impl;

import cv.lecturesight.framesource.FrameSource;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.util.Log;
import org.osgi.service.component.ComponentContext;

public class FrameSourceProviderImpl implements FrameSourceProvider {

  private Log log = new Log("FrameSource Provider");
  private FrameSource source;

  public FrameSourceProviderImpl(FrameSource source) {
    this.source = source;
  }

  protected void activate(ComponentContext context) {
    log.info("Activated");
  }

  @Override
  public FrameSource getFrameSource() {
    return source;
  }

}
