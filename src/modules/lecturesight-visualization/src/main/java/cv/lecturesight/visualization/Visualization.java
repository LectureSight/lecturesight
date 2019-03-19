package cv.lecturesight.visualization;

import cv.lecturesight.opencl.api.OCLSignal;

interface Visualization {

  enum Signal {
    DONE_VISUAL
  }
  
  OCLSignal getSignal();
}
