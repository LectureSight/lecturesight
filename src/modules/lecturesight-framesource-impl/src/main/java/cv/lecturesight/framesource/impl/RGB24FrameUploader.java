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
package cv.lecturesight.framesource.impl;

import com.nativelibs4java.opencl.CLByteBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLQueue;
import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.UUID;

public class RGB24FrameUploader implements FrameUploader {

  private final String UID = UUID.randomUUID().toString();
  private final OpenCLService ocl;
  private final FrameGrabber grabber;
  private final CLKernel conversionK, maskK;
  private final int [] workDim;
  private final long bufferSize;
  private ByteBuffer hostBuffer;
  private CLByteBuffer gpuRawBuffer;
  private final CLImage2D gpuBuffer, temp;
  private CLImage2D mask = null;
  private BufferedImage imageHost;
  private final OCLSignal sig_start;
  private final OCLSignal sig_done;
  private final OCLSignal sig_newframe;
  private BufferedImage maskImage = null;

  public RGB24FrameUploader(OpenCLService clService, FrameGrabber grabber) {
    this.ocl = clService;
    this.grabber = grabber;

    // create signals
    sig_start = ocl.getSignal(UID + "_START");
    sig_done = ocl.getSignal(UID + "_DONE");
    sig_newframe = ocl.getSignal(FrameUploader.SIG_NEWFRAME);

    // set up gpu buffgers
    bufferSize = grabber.getWidth() * grabber.getHeight() * 3;
    gpuRawBuffer = ocl.context().createByteBuffer(Usage.InputOutput, bufferSize);
    gpuBuffer = ocl.context().createImage2D(Usage.InputOutput,
            new CLImageFormat(CLImageFormat.ChannelOrder.BGRA, CLImageFormat.ChannelDataType.UnsignedInt8),
            grabber.getWidth(), grabber.getHeight());
    temp = ocl.context().createImage2D(Usage.InputOutput,
            new CLImageFormat(CLImageFormat.ChannelOrder.BGRA, CLImageFormat.ChannelDataType.UnsignedInt8),
            grabber.getWidth(), grabber.getHeight());

    // set up conversion kernel
    conversionK = ocl.programs().getKernel("conversions", "RGB24_RGBAUint8");
    
    // set up mask kernel
    maskK = ocl.programs().getKernel("conversions", "apply_mask");

    workDim = new int[] {grabber.getWidth(), grabber.getHeight()};

    // System.out.println("Computed buffer size: " + bufferSize);

    // set up conversion run
    ocl.registerLaunch(sig_start, new ComputationRun() {

      @Override
      public void launch(CLQueue queue) {
        CLEvent uploadDone = gpuRawBuffer.writeBytes(queue, 0, bufferSize, hostBuffer, false);
        if (mask != null) {
          conversionK.setArgs(workDim[0], workDim[1], gpuRawBuffer, temp);
          conversionK.enqueueNDRange(queue, workDim, uploadDone);
          maskK.setArgs(temp, mask, gpuBuffer);
          maskK.enqueueNDRange(queue, workDim);
        } else {
          conversionK.setArgs(workDim[0], workDim[1], gpuRawBuffer, gpuBuffer);
          conversionK.enqueueNDRange(queue, workDim, uploadDone);
        }
        imageHost = gpuBuffer.read(queue);
      }

      @Override
      public void land() {
        ocl.castSignal(sig_done);
        ocl.castSignal(sig_newframe);
      }

    });
  }

  @Override
  public OCLSignal getSignal() {
    return sig_done;
  }

  @Override
  public CLImage2D getOutputImage() {
    return gpuBuffer;
  }
  
  @Override
  public CLImage2D getRawOutputImage() {
    if (mask != null) {
      return temp;
    } else {
      return gpuBuffer;
    }
  }

  @Override
  public void upload(Buffer frame) {
    hostBuffer = (ByteBuffer)frame;     // FIXME not thread-safe!
    ocl.castSignal(sig_start);
  }

  @Override
  public BufferedImage getOutputImageHost() {
    return imageHost;
  }
  
  @Override
  public void setMask(BufferedImage mask) {
    this.maskImage = mask; 
    this.mask = ocl.context().createImage2D(Usage.InputOutput, maskImage, false);
  }
}
