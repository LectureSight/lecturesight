/* Copyright (C) 2017 University of Cape Town
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
package cv.lecturesight.status;

import cv.lecturesight.framesource.FrameSourceManager;
import cv.lecturesight.heartbeat.api.HeartBeat;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileManager;
import cv.lecturesight.profile.api.SceneProfileSerializer;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.conf.ConfigurationListener;
import cv.lecturesight.util.metrics.MetricsService;

import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatusServiceImpl implements StatusService, ConfigurationListener {

  @Setter
  private Configuration config;

  @Setter
  private HeartBeat heart;

  @Setter
  private CameraOperator operator;

  @Setter
  private MetricsService metrics;

  @Setter
  private SceneProfileManager sceneProfileManager;

  @Setter
  private FrameSourceManager frameSourceManager;

  // The active profile
  private SceneProfile activeProfile;

  public static final String PROPKEY_ENABLE = "enable";
  public static final String PROPKEY_URL = "url";
  public static final String PROPKEY_NAME = "name";
  public static final String PROPKEY_INTERVAL = "interval";

  // Enabled by default
  private boolean enable = true;

  // Reporting URL
  private String url = null;

  // Agent name
  private String name = null;

  // Shutting down
  private boolean shutdown = false;

  /* Reporting interval */
  private int interval = 30;

  private ScheduledExecutorService executor = null;
  private final StatusExecutor statusExecutor = new StatusExecutor();

  protected void activate(ComponentContext cc) {

    setConfiguration();

    startReporting();

    Logger.debug("Activated");
  }

  protected void deactivate(ComponentContext cc) {
    stopReporting();
    shutdown = true;
    Logger.debug("Deactivated");
  }

  @Override
  public void configurationChanged() {
    if (updateConfiguration() && !shutdown) {
      setConfiguration();
      stopReporting();
      startReporting();
    }
  }

  private void setConfiguration() {
    enable = config.getBoolean(PROPKEY_ENABLE);
    interval = config.getInt(PROPKEY_INTERVAL);
    url = config.get(PROPKEY_URL);
    name = config.get(PROPKEY_NAME);

    // Sanity check
    if ((interval <= 0) || (url == null) || (url.isEmpty()))
      enable = false;

    if (enable) {
      Logger.info("Status Reporting Enabled URL: {} name: {}, interval: {}", url, name, interval);
    } else {
      Logger.info("Status Reporting Disabled");
    }
  }

  private boolean updateConfiguration() {

    // Ideally we would only get configurationChanged() events for this service, but
    // as it's called for any config change, we need to see what's changed.

    boolean changed = (
         (enable != config.getBoolean(PROPKEY_ENABLE))
      || (interval != config.getInt(PROPKEY_INTERVAL))
      || (url == null || !url.equals(config.get(PROPKEY_URL)))
      || (name == null || !name.equals(config.get(PROPKEY_NAME))));

    return changed;
  }

  private void startReporting() {

    if (!enable) return;

    // activate the status executor
    executor = Executors.newScheduledThreadPool(1);
    executor.scheduleWithFixedDelay(statusExecutor, interval, interval, TimeUnit.SECONDS);
  }

  private void stopReporting() {
    if (executor != null)
      executor.shutdownNow();     // shut down the status executor
    executor = null;
  }


  /**
   * Periodically called <code>Runnable</code> that is responsible for status reporting.
   */
  class StatusExecutor implements Runnable {

    @Override
    public void run() {

      // Post to URL
      Logger.debug("Sending status update to {}", url);

      String metricsJson = metrics.json();

      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

        HttpPost uploadFile = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // Agent name
        builder.addTextBody("name", name, ContentType.TEXT_PLAIN);

        // Status
        String status = (heart.isRunning() && operator.isRunning()) ? "active" : "idle";
        builder.addTextBody("status", status, ContentType.TEXT_PLAIN);

        // Metrics 
        builder.addTextBody("metrics", metricsJson, ContentType.APPLICATION_JSON);

        // Active scene profile
        SceneProfile activeProfile = sceneProfileManager.getActiveProfile();
        String profile = SceneProfileSerializer.serialize(activeProfile);
        builder.addTextBody("profile", profile, ContentType.TEXT_PLAIN);

        // Overview image snapshot
        String snFile =  frameSourceManager.getOverviewSnapshotFile();
        if (snFile != null && !snFile.isEmpty()) {
          File f = new File(snFile);
          if (f.isFile()) {
            builder.addBinaryBody("overview-image", new FileInputStream(f),
              ContentType.APPLICATION_OCTET_STREAM, f.getName());
          }
        }

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        Logger.debug("Status update returned code {}: {}",
          response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
      } catch (Exception e) {
        // catch java.net.UnknownHostException | org.apache.http.conn.HttpHostConnectException
        Logger.error(e, "Status update failed");
      }
    }
  }

}
