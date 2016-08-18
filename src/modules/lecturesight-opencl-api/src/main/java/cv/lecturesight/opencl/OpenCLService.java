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

/** API for the OpenCL Service
 * 
 * @author wulff
 */
public interface OpenCLService {

  // Image formats
  enum Format {
    RGBA_UINT8(new CLImageFormat(CLImageFormat.ChannelOrder.RGBA, CLImageFormat.ChannelDataType.UnsignedInt8));
    
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

  /** Returns the underlying JavaCL context
   * 
   * @return JavaCl context
   */
  CLContext context();

  /** Returns the <code>OCLProgramStore</code> of this service instance. Every
   * bundle that uses the OpenCL Service is scanned for OpenCL sources. The
   * compiled OpenCL programs can be accessed through the program store.
   * 
   * @return OpenCL program store
   */
  OCLProgramStore programs();

  /** Schedule a <ComputationRun> for immediate launch. The <code>run</code> 
   * will be launched of the next possible occasion.
   * 
   * TODO return CLEvent for synchronization
   * 
   * @param run ComputationRun to be launched
   */
  void immediateLaunch(ComputationRun run);

  /** Returns the <code>OCLSignal</code> with the specified name. If a signal
   * with the specified name is not existing, it will be created and registered
   * under that name.
   * 
   * @param name of the signal
   * @return OCLSignal registered under <code>name</name>
   */
  OCLSignal getSignal(String name);

  /** Returns the object that holds the utility functions.
   * 
   * @return object with utility functions
   */
  OCLUtils utils();

  /** Emiits the provided signal the to OpenCL runtime management system.
   * 
   * @param signal signal to be emitted.
   */
  void castSignal(OCLSignal signal);

  /** Register <code>run</code> to be launched when <code>trigger</code> is 
   * emitted.
   * 
   * @param trigger signal that should trigger the launch
   * @param run ComputationRun to be launched on trigger
   */
  void registerLaunch(OCLSignal trigger, ComputationRun run);

  /** Register <code>run</code> to be launched when any of the <code>triggers</code> 
   * is emitted.
   * 
   * @param triggers signals that should trigger the launch
   * @param run ComputationRun to be launched on trigger
   */
  void registerLaunch(OCLSignal[] triggers, ComputationRun run);

  /** Unregister <code>run</code> from launch on <code>trigger</code>. 
   * 
   * @param trigger signal from which the ComputationRun should be detached
   * @param run ComputationRun to be detached from signal
   */
  void unregisterLaunch(OCLSignal trigger, ComputationRun run);

  void registerTriggerable(OCLSignal trigger, Triggerable handler);

  void registerTriggerable(OCLSignal[] triggers, Triggerable handler);

  void unregisterTriggerable(OCLSignal trigger, Triggerable handler);

  OCLSignalBarrier createSignalBarrier(OCLSignal[] triggers);
}
