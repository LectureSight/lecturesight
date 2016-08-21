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
package cv.lecturesight.util.metrics;

import com.codahale.metrics.*;
import java.util.concurrent.TimeUnit;
import java.util.SortedMap;
import java.util.Properties;
import org.pmw.tinylog.Logger;
import org.slf4j.LoggerFactory;

public class MetricsServiceImpl implements MetricsService {

  private static final MetricRegistry registry = new MetricRegistry();
  
  public MetricsServiceImpl() {
    Logger.info("Metrics Service");

    try {
    // JMX reporting (if enabled)
    final JmxReporter jmx_reporter = JmxReporter.forRegistry(registry).inDomain("cv.lecturesight.util.metrics").build();
    jmx_reporter.start();

    // Console reporting (if enabled)
    final Slf4jReporter log_reporter = Slf4jReporter.forRegistry(registry)
                                            .outputTo(LoggerFactory.getLogger("cv.lecturesight.util.metrics"))
                                            .convertRatesTo(TimeUnit.SECONDS)
                                            .convertDurationsTo(TimeUnit.MILLISECONDS)
                                            .build();
    log_reporter.start(1, TimeUnit.MINUTES);

    } catch (Throwable t) {
      Logger.error(t, "Error starting metrics");
    }

    Logger.info("Started");
  } 

  @Override
  public void reset() {
    Logger.info("reset");

    // To reset the metrics, remove all metrics (they will be re-created when next updated)
    registry.removeMatching(MetricFilter.ALL);
  }

  @Override
  public void save() {
    Logger.info("save");
  }

  @Override
  public void show() {
    Logger.info("show");
  }


  @Override
  public void setDescription(String key, String desc) {
    Logger.info("Set description for: " + key + " to: " + desc);
  }

  @Override
  public void incCounter(String key) {

    SortedMap<String,Counter> counters = registry.getCounters(MetricFilter.ALL);
    if (counters.containsKey(key)) {
       Logger.info("Increment existing counter: " + key);
       Counter counter = counters.get(key);
       counter.inc();
    } else {
       Logger.info("Increment new counter: " + key);
       Counter counter = registry.counter(key);
       counter.inc();
    }

  }

  @Override
  public void setValue(String key, long value) {
    Logger.info("Set value for: " + key + " to " + value);
  }


}
