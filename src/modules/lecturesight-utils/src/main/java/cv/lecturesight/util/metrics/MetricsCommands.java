/* Copyright (C) 2016 University of Cape Town
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
package cv.lecturesight.util.metrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public class MetricsCommands {

  public static final String[] commands = {"show", "reset"};
  MetricsServiceImpl metrics;

  public MetricsCommands(MetricsServiceImpl metrics) {
    this.metrics = metrics;
  }

  public void show(String[] args) {
    metrics.show();
  }

  public void reset(String[] args) {
    metrics.reset();
  }

  private void println(String msg) {
    System.out.println(msg);
  }
}
