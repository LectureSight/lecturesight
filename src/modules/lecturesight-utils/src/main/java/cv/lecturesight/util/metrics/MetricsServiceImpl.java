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
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.SortedMap;
import java.util.Locale;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;
import org.slf4j.LoggerFactory;

@Component(name="lecturesight.util.metrics", immediate=true)
@Service
@Properties({
  @Property(name = "osgi.command.scope", value = "metrics"),
  @Property(name = "osgi.command.function", value = {"show", "reset", "pause"})
})

public class MetricsServiceImpl implements MetricsService {

  private static final MetricRegistry registry = new MetricRegistry();

  /* Time of last reset */
  private long last_reset = 0;

  /* Reporting options - TODO from config */
  private boolean report_jmx = true;
  private boolean report_csv = true;
  private boolean report_log = true;

  /* Reporters */
  private CsvReporter   csv_reporter;
  private JmxReporter   jmx_reporter;
  private Slf4jReporter log_reporter;
  
  /* Reporting interval - TODO from config*/
  private int csv_interval = 5;
  private int log_interval = 60;

  /* CSV location */
  private File metricsDir;

  protected void deactivate(ComponentContext cc) {
      Logger.info("Deactivated");
  }

  protected void activate(ComponentContext cc) {

    Logger.info("Metrics Service");

    // make sure metrics directory exists
    metricsDir = new File(System.getProperty("user.dir") + File.separator + "metrics");
    if (!metricsDir.exists()) {
      Logger.info("Creating new metrics directory at: " + metricsDir.getAbsolutePath());
      try {
        metricsDir.mkdir();
      } catch (Exception e) {
        Logger.error("Failed to create metrics directory", e);
      }
    }

    start_reporting();

    last_reset = System.currentTimeMillis();

    Logger.info("Activated");
  } 

  private void start_reporting() {

    // JMX reporting (if enabled)
    if (report_jmx && jmx_reporter == null) {
       jmx_reporter = JmxReporter.forRegistry(registry).inDomain("cv.lecturesight.util.metrics").build();
       jmx_reporter.start();
    }

    // CSV reporting (if enabled)
    if (report_csv && csv_reporter == null) {
        csv_reporter = CsvReporter.forRegistry(registry)
                                        .formatFor(Locale.US)
                                        .convertRatesTo(TimeUnit.SECONDS)
                                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                                        .build(metricsDir);
        csv_reporter.start(csv_interval, TimeUnit.SECONDS);
    }

    // Console reporting (if enabled)
    if (report_log && log_reporter == null) {
       log_reporter = Slf4jReporter.forRegistry(registry)
                                            .outputTo(LoggerFactory.getLogger("cv.lecturesight.util.metrics"))
                                            .convertRatesTo(TimeUnit.SECONDS)
                                            .convertDurationsTo(TimeUnit.MILLISECONDS)
                                            .build();
       log_reporter.start(log_interval, TimeUnit.SECONDS);
    }

  }

  private void stop_reporting() {

    // Shut down reporting threads
    if (report_csv && csv_reporter != null) {
	csv_reporter.stop();
	csv_reporter = null;
    }

    if (report_log && log_reporter != null) {
	log_reporter.stop();
	log_reporter = null;
    }

  }

  @Override
  public void reset() {
    Logger.info("reset");

    // To reset the metrics, remove all metrics (they will be re-created when next updated)
    registry.removeMatching(MetricFilter.ALL);
 
    start_reporting();

    last_reset = System.currentTimeMillis();
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
  public void pause() {
    Logger.info("pause");
    stop_reporting();
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
  public void timedEvent(String key, long duration_ms) {

    SortedMap<String,Timer> timers = registry.getTimers(MetricFilter.ALL);
    if (timers.containsKey(key)) {
       Logger.info("Adding duration to existing timer: " + key);
       Timer timer = timers.get(key);
       timer.update(duration_ms, TimeUnit.MILLISECONDS);
    }  else {
       Logger.info("Adding duration to new timer: " + key);
       Timer timer = registry.timer(key);
       timer.update(duration_ms, TimeUnit.MILLISECONDS);
    }

  }

  @Override
  public void setValue(String key, long value) {
    Logger.info("Set value for: " + key + " to " + value);
  }

  // Console commands
  public void show(String[] args) {
    show();
  }

  public void reset(String[] args) {
    reset();
  }

  public void pause(String[] args) {
    pause();
  }


}
