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
