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

import com.codahale.metrics.MetricRegistry;

public interface MetricsService {

  /* Reset metrics */
  void reset();

  /* Pause metric reporting */
  void pause();

  /* Resume metric reporting */
  void resume();

  /* Save JSON snapshot to file */
  void save();

  /* Save JSON snapshot, with the eventId in the filename */
  void save(String eventId);

  /* JSON snapshot of metrics */
  String json();

  void setDescription(String key, String desc);

  /* Increment Counter */
  void incCounter(String key);

  /* Add a timed event (Timer) */
  void timedEvent(String key, long duration_ms);

  /* Gauge */
  void setGauge(String key, long value);

  /* Access to the MetricsRegistry */
  MetricRegistry getRegistry();
}
