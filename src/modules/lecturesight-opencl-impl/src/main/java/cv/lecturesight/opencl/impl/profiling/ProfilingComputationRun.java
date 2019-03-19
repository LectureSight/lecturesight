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
package cv.lecturesight.opencl.impl.profiling;

import cv.lecturesight.opencl.api.ComputationRun;

import com.nativelibs4java.opencl.CLQueue;

public class ProfilingComputationRun implements ComputationRun {

  private ComputationRun run;
  private ProfilingServer profiler;

  private String name;
  private long launchTime;
  private long landingTime;
  private long finishingTime;

  public ProfilingComputationRun(ComputationRun run, ProfilingServer profiler) {
    this.run = run;
    this.profiler = profiler;
    this.name = run.getClass().getName();
  }

  @Override
  public void launch(CLQueue queue) {
    launchTime = System.nanoTime();
    run.launch(queue);
  }

  @Override
  public void land() {
    landingTime = System.nanoTime();
    run.land();
    finishingTime = System.nanoTime();
    profiler.record(new ProfilingResult(name, launchTime, landingTime, finishingTime));
  }
}
