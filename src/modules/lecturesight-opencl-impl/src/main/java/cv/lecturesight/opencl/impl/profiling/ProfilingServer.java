package cv.lecturesight.opencl.impl.profiling;

import cv.lecturesight.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProfilingServer extends Thread {

  final static int QUEUE_SIZE = 2048;
  final static String NL = "\n";
  final static String DATAFILE_PREFIX = "profiling-";
  final static String DATAFILE_SUFFIX = ".csv";
  final static String DATAFILE_HEADER = "frame;time-total;time-gpu;time-host;name";
  final static String FRAMETIMEFILE_PREFIX = "frametimes-";
  final static String FRAMETIMEFILE_SUFFIX = ".csv";
  final static String FRAMETIEMFILE_HEADER = "frame;time";
  private Log log = new Log("OpenCL Profiling Server");
  private final BlockingQueue<ProfilingResult> recordQueue;
  private File dataFile, frametimeFile;
  private OutputStreamWriter dataWriter, frametimeWriter;
  private long currentFrame = 0L;
  private long lastNanoTime = -1L;

  public ProfilingServer() {
    recordQueue = new ArrayBlockingQueue<ProfilingResult>(QUEUE_SIZE);
    try {
      String sessionTime = Long.toString(System.currentTimeMillis());
      dataFile = new File(DATAFILE_PREFIX + sessionTime + DATAFILE_SUFFIX);
      dataWriter = new OutputStreamWriter(new FileOutputStream(dataFile));
      dataWriter.write(DATAFILE_HEADER + NL);
      
      frametimeFile = new File(FRAMETIMEFILE_PREFIX + sessionTime + DATAFILE_SUFFIX);
      frametimeWriter = new OutputStreamWriter(new FileOutputStream(frametimeFile));
      frametimeWriter.write( FRAMETIEMFILE_HEADER + NL);
    } catch (IOException e) {
      log.error("Could not open datafile " + dataFile.getAbsolutePath() + " for writing!", e);
    }
  }

  public void record(ProfilingResult record) {
    try {
      recordQueue.put(record);
    } catch(InterruptedException e) {
    }
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        final ProfilingResult result = recordQueue.take();
        long gpuTime = result.getLadningTime() - result.getLaunchTime();
        long hostTime = result.getFinishingTime() - result.getLadningTime();
        try {
          dataWriter.write(Long.toString(currentFrame));      // FIXME ProfilingComputationRun should know and report in which frame it was executed!!
          dataWriter.write(';');
          dataWriter.write(Long.toString(gpuTime+hostTime));
          dataWriter.write(';');
          dataWriter.write(Long.toString(gpuTime));
          dataWriter.write(';');
          dataWriter.write(Long.toString(hostTime));
          dataWriter.write(';');
          dataWriter.write(result.getName());
          dataWriter.write(NL);
        } catch (IOException e) {
          log.warn("Could not write to data file: " + e.getMessage()); 
        }
      }
    } catch (InterruptedException e) {
    }
  }
  
  public void nextFrame() {
    long currentNanoTime = System.nanoTime();
    if (++currentFrame > 1) {
      long frameTime = currentNanoTime - lastNanoTime;
      try {
        frametimeWriter.write(Long.toString(currentFrame));
        frametimeWriter.append(';');
        frametimeWriter.write(Long.toString(frameTime));
        frametimeWriter.append(NL);
      } catch (Exception e) {
        log.warn("Could not write to data file: " + e.getMessage()); 
      }
    }
    lastNanoTime = currentNanoTime;
  }

  public void shutdown() {
    log.info("Shutting down");
    interrupt();
    try {
      dataWriter.flush();         // there are nicer ways to do this
      dataWriter.close();
      frametimeWriter.flush();
      frametimeWriter.close();
    } catch (IOException e) {
      log.warn(e.getMessage());
    }
  }
}
