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

public interface MetricsService {

  /* Reset metrics */
  public void reset();

  /* Pause metric reporting */
  public void pause();

  /* Save */
  public void save();

  /* Show metrics */
  public void show();

  public void setDescription(String key, String desc);

  /* Counter */
  public void incCounter(String key);

  /* Timed event (Timer) */
  public void timedEvent(String key, long duration_ms);

  /* Gauge */
  public void setValue(String key, long value);

}

