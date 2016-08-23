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

import com.codahale.metrics.*;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileOutputStream;
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
  @Property(name = "osgi.command.function", value = {"list", "save", "show", "reset", "pause"})
})

public class MetricsServiceImpl implements MetricsService {

  private static final MetricRegistry registry = new MetricRegistry();

  // JSON serialization
  private ObjectMapper objectMapper;

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

  /* CSV and JSON file location */
  private File metricsDir;
  private File metricsJson;

  public MetricRegistry getRegistry() {
      return registry;
  }

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

    // JSON output file
    metricsJson = new File(metricsDir.getAbsolutePath() + File.separator + "metrics.json");

    // Set up for JSON serialization
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MetricsModule(TimeUnit.MINUTES, TimeUnit.MILLISECONDS, true));
    objectMapper.registerModule(new HealthCheckModule());
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    // Set session start
    last_reset = System.currentTimeMillis();

    setup_metrics();
    start_reporting();

    Logger.info("Activated");
  } 

  /* Set up internal metrics */
  private void setup_metrics() {

    // set up a counter for session start time
    Counter counter = registry.counter("metrics.session.start");
    counter.inc(last_reset);

    // set up a gauge for elapsed time
    registry.register(MetricRegistry.name("metrics.session.elapsed"),
                         new Gauge<Long>() {
                             @Override
                             public Long getValue() {
                                 return new Long(System.currentTimeMillis() - last_reset);
                             }
                         });

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
                                        .convertRatesTo(TimeUnit.MINUTES)
                                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                                        .build(metricsDir);
        csv_reporter.start(csv_interval, TimeUnit.SECONDS);
    }

    // Console reporting (if enabled)
    if (report_log && log_reporter == null) {
       log_reporter = Slf4jReporter.forRegistry(registry)
                                            .outputTo(LoggerFactory.getLogger("cv.lecturesight.util.metrics"))
                                            .convertRatesTo(TimeUnit.MINUTES)
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

    last_reset = System.currentTimeMillis();

    setup_metrics();
    start_reporting();
  }

  @Override
  public void save() {
    Logger.info("Saving metrics data to " + metricsJson.getAbsolutePath());
    
    FileOutputStream os = null;
    try {
        os = new FileOutputStream(metricsJson);
        objectMapper.writer().writeValue(os, registry); 
    } catch (Exception e) {
	Logger.error(e, "Unable to write JSON metrics data to file");
    } finally {
	if (os != null) {
	   try { os.close(); } catch (Exception ef) { }
	}
    }
  }

  @Override
  public String json() {

    String json = "{}";

    try {
      json = objectMapper.writer().writeValueAsString(registry);
    } catch (JsonProcessingException jpe) {
      Logger.error(jpe, "Unable to process metrics JSON");
    }

    return json;
  }

  @Override
  public void pause() {
    Logger.info("CSV and log reporting paused (metrics will continue to be updated)");
    stop_reporting();
  }

  @Override
  public void setDescription(String key, String desc) {
    Logger.debug("Set description for: " + key + " to: " + desc);
  }

  @Override
  public void incCounter(String key) {

    SortedMap<String,Counter> counters = registry.getCounters(MetricFilter.ALL);
    Counter counter;

    if (counters.containsKey(key)) {
       Logger.debug("Increment existing counter: " + key);
       counter = counters.get(key);
    } else {
       Logger.debug("Increment new counter: " + key);
       counter = registry.counter(key);
    }

    counter.inc();

  }

  @Override
  public void timedEvent(String key, long duration_ms) {

    // Timed events use both a histogram and a counter (for total elapsed time)

    SortedMap<String,Timer> timers = registry.getTimers(MetricFilter.ALL);
    SortedMap<String,Counter> counters = registry.getCounters(MetricFilter.ALL);

    Timer timer;
    Counter counter;

    if (timers.containsKey(key)) {
       Logger.debug("Adding duration to existing timer: " + key);
       timer = timers.get(key);
       counter = counters.get(MetricRegistry.name(key,"elapsed"));
    }  else {
       Logger.debug("Adding duration to new timer: " + key);
       // TODO Use a sliding window reservoir
       timer = registry.timer(key);
       counter = registry.counter(MetricRegistry.name(key,"elapsed"));
    }

    timer.update(duration_ms, TimeUnit.MILLISECONDS);
    counter.inc(duration_ms);

  }

  @Override
  public void setGauge(String key, long value) {
    Logger.debug("Set value for: " + key + " to " + value);
  }

  // Console commands

  public void list(String[] args) {
    // List all the metrics
    for (String metrickey : registry.getNames()) {
       System.out.println(metrickey);
    }
  }

  public void show(String[] args) {
    System.out.println(json());
  }

  public void save(String[] args) {
    save();
  }

  public void reset(String[] args) {
    reset();
  }

  public void pause(String[] args) {
    pause();
  }

}
