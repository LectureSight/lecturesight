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

class ProfilingResult {

  private String name;
  private long launchTime;
  private long ladningTime;
  private long finishingTime;

  ProfilingResult(String name, long launchT, long landT, long finishT) {
    this.name = name;
    this.launchTime = launchT;
    this.ladningTime = landT;
    this.finishingTime = finishT;
  }

  public String getName() {
    return name;
  }

  public long getLaunchTime() {
    return launchTime;
  }

  public long getLadningTime() {
    return ladningTime;
  }

  public long getFinishingTime() {
    return finishingTime;
  }
}

