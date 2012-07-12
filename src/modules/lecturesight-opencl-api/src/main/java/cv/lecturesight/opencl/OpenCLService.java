package cv.lecturesight.opencl;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLImageFormat;
import cv.lecturesight.opencl.api.ComputationRun;
import cv.lecturesight.opencl.api.OCLSignal;
import cv.lecturesight.opencl.api.OCLSignalBarrier;
import cv.lecturesight.opencl.api.Triggerable;

/** OpenCL Service API
 * 
 */
public interface OpenCLService {

  enum Format {
    BGRA_UINT8(new CLImageFormat(CLImageFormat.ChannelOrder.BGRA, CLImageFormat.ChannelDataType.UnsignedInt8)), 
    INTENSITY_UINT8(new CLImageFormat(CLImageFormat.ChannelOrder.INTENSITY, CLImageFormat.ChannelDataType.UnsignedInt8));
    
    //<editor-fold defaultstate="collapsed" desc="Enums gutts">
    private CLImageFormat clFormat;
  
    Format(CLImageFormat clFormat) {
      this.clFormat = clFormat;
    }
    
    public CLImageFormat getCLImageFormat() {
      return clFormat;
    }
    //</editor-fold>
  };

  CLContext context();

  OCLProgramStore programs();

  void immediateLaunch(ComputationRun run);

  OCLSignal getSignal(String name);

  OCLUtils utils();

  void castSignal(OCLSignal signal);

  void registerLaunch(OCLSignal trigger, ComputationRun run);

  void registerLaunch(OCLSignal[] triggers, ComputationRun run);

  void unregisterLaunch(OCLSignal trigger, ComputationRun run);

  void registerTriggerable(OCLSignal trigger, Triggerable handler);

  void registerTriggerable(OCLSignal[] triggers, Triggerable handler);

  void unregisterTriggerable(OCLSignal trigger, Triggerable handler);

  OCLSignalBarrier createSignalBarrier(OCLSignal[] triggers);

}
