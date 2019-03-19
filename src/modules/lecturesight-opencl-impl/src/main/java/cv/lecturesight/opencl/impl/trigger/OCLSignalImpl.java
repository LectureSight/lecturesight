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
package cv.lecturesight.opencl.impl.trigger;

import cv.lecturesight.opencl.api.OCLSignal;

public class OCLSignalImpl implements OCLSignal {

  private int id;
  private String name;

  public OCLSignalImpl(int id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof OCLSignalImpl) {
      OCLSignalImpl otherSignal = (OCLSignalImpl) other;
      return otherSignal.id == this.id && otherSignal.name.equals(this.name);
    } else {
      return false;
    }
  }
}
