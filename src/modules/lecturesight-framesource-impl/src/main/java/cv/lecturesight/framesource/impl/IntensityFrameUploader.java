package cv.lecturesight.framesource.impl;

import cv.lecturesight.framesource.FrameGrabber;
import cv.lecturesight.opencl.OpenCLService;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;

import org.pmw.tinylog.Logger;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.UUID;

public class IntensityFrameUploader implements FrameUploader {

  private final String UID = UUID.randomUUID().toString();
  private final OpenCLService ocl;
  private final FrameGrabber grabber;
  private final CLKernel conversionK;
  private final CLKernel maskK;
  private final int[] workDim;
  private final long bufferSize;
  private ByteBuffer hostBuffer;
  private CLBuffer<Byte> gpuRawBuffer;
  private final CLImage2D gpuBuffer;
  private final CLImage2D tempBuffer;
  private CLImage2D last;
  private CLImage2D mask = null;
  private BufferedImage imageHost;
  private final OCLSignal sig_start;
  private final OCLSignal sig_done;
  private final OCLSignal sig_newframe;
  private BufferedImage maskImage = null;
  private ComputationRun uploadRun;

  public IntensityFrameUploader(OpenCLService clService, FrameGrabber grabber) {
    this.ocl = clService;
    this.grabber = grabber;

    // create signals
    sig_start = ocl.getSignal(UID + "_START");
    sig_done = ocl.getSignal(UID + "_DONE");
    sig_newframe = ocl.getSignal(FrameUploader.SIG_NEWFRAME);

    // set up gpu buffers
    bufferSize = grabber.getWidth() * grabber.getHeight() * 3;
    gpuRawBuffer = ocl.context().createByteBuffer(CLMem.Usage.InputOutput, bufferSize);
    gpuBuffer = ocl.context().createImage2D(CLMem.Usage.InputOutput,
                                            new CLImageFormat(CLImageFormat.ChannelOrder.BGRA, CLImageFormat.ChannelDataType.UnsignedInt8),
                                            grabber.getWidth(), grabber.getHeight());
    last = ocl.context().createImage2D(CLMem.Usage.InputOutput,
                                       new CLImageFormat(CLImageFormat.ChannelOrder.BGRA, CLImageFormat.ChannelDataType.UnsignedInt8),
                                       grabber.getWidth(), grabber.getHeight());
    ocl.utils().setValues(0, 0, grabber.getWidth(), grabber.getHeight(), last, 0, 0, 0, 0);
    tempBuffer = ocl.context().createImage2D(CLMem.Usage.InputOutput,
                                             new CLImageFormat(CLImageFormat.ChannelOrder.BGRA, CLImageFormat.ChannelDataType.UnsignedInt8),
                                             grabber.getWidth(), grabber.getHeight());

    // set up conversion kernel
    conversionK = ocl.programs().getKernel("conversions", "Intensity8_RGBAUint8");

    // set up mask kernel
    maskK = ocl.programs().getKernel("conversions", "apply_mask");

    workDim = new int[]{grabber.getWidth(), grabber.getHeight()};

    Logger.trace("Computed buffer size: {}", bufferSize);

    // set up conversion run
    uploadRun = new ComputationRun() {
      @Override
      public void launch(CLQueue queue) {
        ocl.utils().copyImage(0, 0, workDim[0], workDim[1], gpuBuffer, 0, 0, last);
        CLEvent uploadDone = gpuRawBuffer.writeBytes(queue, 0, bufferSize, hostBuffer, false);
        if (mask != null) {
          conversionK.setArgs(workDim[0], workDim[1], gpuRawBuffer, tempBuffer);
          conversionK.enqueueNDRange(queue, workDim, uploadDone);
          maskK.setArgs(tempBuffer, mask, gpuBuffer);
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
    };
    ocl.registerLaunch(sig_start, uploadRun);
  }

  @Override
  public void destroy() {
    // deregister this uploaders computationRun
    ocl.unregisterLaunch(sig_start, uploadRun);

    // free GPU buffers
    gpuRawBuffer.release();
    gpuBuffer.release();
    tempBuffer.release();
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
      return tempBuffer;
    } else {
      return gpuBuffer;
    }
  }

  @Override
  public void upload(Buffer frame) {
    hostBuffer = (ByteBuffer) frame;     // FIXME not thread-safe!
    ocl.castSignal(sig_start);
  }

  @Override
  public BufferedImage getOutputImageHost() {
    return imageHost;
  }

  @Override
  public void setMask(BufferedImage mask) {
    this.maskImage = mask;
    this.mask = ocl.context().createImage2D(CLMem.Usage.InputOutput, maskImage, false);
  }

  @Override
  public CLImage2D getLastOutputImage() {
    return last;
  }
}
