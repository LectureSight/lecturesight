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

import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;

import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.HealthCheckModule;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class MetricsServiceImpl implements MetricsService, ConfigurationListener {

  private Configuration config;

  public static final String PROPKEY_ENABLE = "metrics.enable";
  public static final String PROPKEY_CSV_ENABLE = "metrics.csv.enable";
  public static final String PROPKEY_JMX_ENABLE = "metrics.jmx.enable";
  public static final String PROPKEY_LOG_ENABLE = "metrics.log.enable";
  public static final String PROPKEY_CSV_INTERVAL = "metrics.csv.interval";
  public static final String PROPKEY_LOG_INTERVAL = "metrics.log.interval";

  // Enabled by default
  private boolean enable = true;

  // Shutting down
  private boolean shutdown = false;

  private static final MetricRegistry registry = new MetricRegistry();

  // JSON serialization
  private ObjectMapper objectMapper;

  /* Time of last reset */
  private long last_reset = 0;

  /* Reporting options */
  private boolean report_csv = true;
  private boolean report_jmx = true;
  private boolean report_log = true;

  /* Reporters */
  private CsvReporter   csv_reporter;
  private JmxReporter   jmx_reporter;
  private Slf4jReporter log_reporter;

  /* Reporting interval */
  private int csv_interval = 30;
  private int log_interval = 300;

  /* CSV and JSON file location */
  private File metricsDir;

  protected void activate(ComponentContext cc) {

    setConfiguration();

    if (enable) {
      Logger.info("Metrics Enabled (CSV=" + report_csv + " JMX=" + report_jmx + " LOG=" + report_log + ")");
    } else {
      Logger.info("Metrics Disabled");
    }

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

    // Set up for JSON serialization
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MetricsModule(TimeUnit.MINUTES, TimeUnit.MILLISECONDS, true));
    objectMapper.registerModule(new HealthCheckModule());
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    // Set session start
    last_reset = System.currentTimeMillis();

    setupMetrics();

    if (enable) {
      startReporting();
    }

    Logger.debug("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    stopReporting();
    save();
    shutdown = true;
    Logger.debug("Deactivated");
  }

  @Override
  public void configurationChanged() {
    if (updateConfiguration() && !shutdown) {
      Logger.debug("Configuration updated");
      stopReporting();
      startReporting();
    }
  }

  private void setConfiguration() {
    enable = config.getBoolean(PROPKEY_ENABLE);
    report_csv = config.getBoolean(PROPKEY_CSV_ENABLE);
    report_jmx = config.getBoolean(PROPKEY_JMX_ENABLE);
    report_log = config.getBoolean(PROPKEY_LOG_ENABLE);
    csv_interval = config.getInt(PROPKEY_CSV_INTERVAL);
    log_interval = config.getInt(PROPKEY_LOG_INTERVAL);
  }

  private boolean updateConfiguration() {

    // Ideally we would only get configurationChanged() events for this service, but
    // as it's called for any config change, we need to see what's changed.

    boolean changed = false;

    if (enable != config.getBoolean(PROPKEY_ENABLE)) {
      enable = !enable;
      changed = true;
    }

    if (report_csv != config.getBoolean(PROPKEY_CSV_ENABLE)) {
      report_csv = !report_csv;
      changed = true;
    }

    if (report_jmx != config.getBoolean(PROPKEY_JMX_ENABLE)) {
      report_jmx = !report_jmx;
      changed = true;
    }

    if (report_log != config.getBoolean(PROPKEY_LOG_ENABLE)) {
      report_log = !report_log;
      changed = true;
    }

    if (csv_interval != config.getInt(PROPKEY_CSV_INTERVAL)) {
      csv_interval = config.getInt(PROPKEY_CSV_INTERVAL);
      changed = true;
    }

    if (log_interval != config.getInt(PROPKEY_LOG_INTERVAL)) {
      log_interval = config.getInt(PROPKEY_LOG_INTERVAL);
      changed = true;
    }

    return changed;
  }

  /* Set up internal metrics */
  private void setupMetrics() {

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

  private void startReporting() {

    if (!enable) return;

    // CSV reporting (if enabled)
    if (report_csv && csv_reporter == null) {
      csv_reporter = CsvReporter.forRegistry(registry)
      .formatFor(Locale.US)
      .convertRatesTo(TimeUnit.MINUTES)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(metricsDir);
      csv_reporter.start(csv_interval, TimeUnit.SECONDS);
    }

    // JMX reporting (if enabled)
    if (report_jmx && jmx_reporter == null) {
      jmx_reporter = JmxReporter.forRegistry(registry).inDomain("cv.lecturesight.util.metrics").build();
      jmx_reporter.start();
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

  private void stopReporting() {

    // Shut down reporting threads
    if (csv_reporter != null) {
      csv_reporter.stop();
      csv_reporter = null;
    }

    if (jmx_reporter != null) {
      jmx_reporter.stop();
      jmx_reporter = null;
    }

    if (log_reporter != null) {
      log_reporter.stop();
      log_reporter = null;
    }

  }

  @Override
  public MetricRegistry getRegistry() {
    return registry;
  }

  @Override
  public void reset() {

    if (!enable) return;

    Logger.info("reset");

    // To reset the metrics, remove all metrics (they will be re-created when next updated)
    registry.removeMatching(MetricFilter.ALL);

    last_reset = System.currentTimeMillis();

    setupMetrics();
    startReporting();
  }

  @Override
  public void save() {
    save(null);
  }

  @Override
  public void save(String eventId) {

    if (!enable) return;

    // JSON output file
    File metricsJson;

    if (eventId != null) {
      metricsJson = new File(metricsDir.getAbsolutePath() + File.separator + "metrics-" + eventId + ".json");
    } else {
      metricsJson = new File(metricsDir.getAbsolutePath() + File.separator + "metrics.json");
    }

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

    if (!enable) return;

    Logger.info("Reporting paused (metrics will continue to be updated)");
    stopReporting();
  }

  @Override
  public void resume() {

    if (!enable) return;

    Logger.info("Reporting resumed");
    startReporting();
  }

  @Override
  public void setDescription(String key, String desc) {

    if (!enable) return;

    Logger.debug("Set description for: " + key + " to: " + desc);
  }

  @Override
  public void incCounter(String key) {

    if (!enable) return;

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

    if (!enable) return;

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
      // TODO Use a sliding window reservoir?
      timer = registry.timer(key);
      counter = registry.counter(MetricRegistry.name(key,"elapsed"));
    }

    timer.update(duration_ms, TimeUnit.MILLISECONDS);
    counter.inc(duration_ms);

  }

  @Override
  public void setGauge(String key, long value) {

    if (!enable) return;

    Logger.debug("Set value for: " + key + " to " + value);
  }

  // Console commands

  //CHECKSTYLE:OFF
  public void list(String[] args) {
    if (enable) {
      // List all the metrics
      for (String metrickey : registry.getNames()) {
        System.out.println(metrickey);
      }
    } else {
      System.out.println("disabled");
    }
  }

  public void show(String[] args) {
    System.out.println(enable ? json() : "disabled");
  }
  //CHECKSTYLE:ON

  public void setConfig(Configuration config) {
    // lombok causes trouble in this module. So we define a setter here.
    this.config = config;
  }

}
