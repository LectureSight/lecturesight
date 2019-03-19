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
package cv.lecturesight.display.impl;

import cv.lecturesight.display.DisplayRegistration;

import java.util.Objects;

public class DisplayRegistrationImpl implements DisplayRegistration {

  private static int nextId = 0;
  private final int id;
  private String sid;

  public DisplayRegistrationImpl(String sid) {
    this.sid = sid;
    this.id = nextId++;
  }

  @Override
  public int getID() {
    return id;
  }

  @Override
  public String getSID() {
    return sid;
  }

  @Override
  public boolean equals(Object other) {

    if (other == null || !(other instanceof DisplayRegistrationImpl))
      return false;

    DisplayRegistrationImpl o = (DisplayRegistrationImpl) other;
    return id == o.id && sid.equals(o.sid);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(sid) + 31*id;
  }

}
