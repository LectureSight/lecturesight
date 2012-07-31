package cv.lecturesight.opencl.impl;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.CLIntBuffer;
import cv.lecturesight.opencl.OCLUtils;

public class OCLUtilsImpl implements OCLUtils {

  private CLQueue queue;
  // Kernels
  CLKernel set_values4, set_values1, set_valuesInt;
  CLKernel copy_image;

  public OCLUtilsImpl(CLQueue queue, CLProgram utils) throws CLBuildException {
    this.queue = queue;
    set_values4 = utils.createKernel("set_values4");
    set_values1 = utils.createKernel("set_values1");
    set_valuesInt = utils.createKernel("set_valuesInt");
    copy_image = utils.createKernel("copy_image");
  }

  @Override
  public synchronized CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int r, int g, int b, int a) {
    int[] workDim = new int[]{width, height};
    set_values4.setArgs(x, y, image, r, g, b, a);
    return set_values4.enqueueNDRange(queue, workDim);
  }

  @Override
  public synchronized CLEvent setValues(int x, int y, int width, int height, CLImage2D image, int val) {
    int[] workDim = new int[]{width, height};
    set_values1.setArgs(x, y, image, val);
    return set_values1.enqueueNDRange(queue, workDim);
  }

  @Override
  public synchronized CLEvent setValues(int start, int end, CLIntBuffer buffer, int val) {
    int[] workDim = new int[]{end-start};
    set_valuesInt.setArgs(start, buffer, val);
    return set_valuesInt.enqueueNDRange(queue, workDim);
  }

  @Override
  public synchronized CLEvent copyImage(int x, int y, int width, int height, CLImage2D source, int dest_x, int dest_y, CLImage2D dest) {
    int[] workDim = new int[]{width, height};
    copy_image.setArgs(x, y, source, dest_x, dest_y, dest);
    return copy_image.enqueueNDRange(queue, workDim);
  }
}
