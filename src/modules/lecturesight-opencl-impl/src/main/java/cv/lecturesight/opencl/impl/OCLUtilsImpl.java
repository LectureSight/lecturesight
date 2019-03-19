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

import cv.lecturesight.opencl.OCLUtils;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

public class OCLUtilsImpl implements OCLUtils {

  private CLQueue queue;
  // Kernels
  CLKernel set_values4;
  CLKernel set_values1;
  CLKernel set_valuesInt;
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
  public synchronized CLEvent setValues(int start, int end, CLBuffer<Integer> buffer, int val) {
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
