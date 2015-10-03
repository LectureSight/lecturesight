package cv.lecturesight.scheduler;

import cv.lecturesight.heartbeat.api.HeartBeat;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.scheduler.ical.ICalendar;
import cv.lecturesight.scheduler.ical.VEvent;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * A service that loads a schedule from a iCal file and starts/stops object
 * tracking and camera control accordingly.
 */
@Component(name = "lecturesight.dutyscheduler", immediate = true)
@Service
public class DutyScheduler implements ArtifactInstaller {

  final static String PROPKEY_LEADTIME = "tracker.leadtime";
  final static String PROPKEY_FILENAME = "schedule.file";
  final static String PROPKEY_TZOFFSET = "timezone.offset";
  final static String PROPKEY_AGENTNAME = "agent.name";
  final static String SCHEDULE_DIRECTORY_NAME = "schedule";
  private Log log = new Log("Duty Scheduler");
  @Reference
  Configuration config;
  @Reference
  HeartBeat heart;
  @Reference(policy = ReferencePolicy.DYNAMIC)
  volatile CameraSteeringWorker camera;
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private EventExecutor eventExecutor = new EventExecutor();
  private EventList events = new EventList();
  private String scheduleFileName;
  private String scheduleFileAbsoultePath;

  protected void activate(ComponentContext cc) {
    // generate absolut search path for schedule file
    scheduleFileName = config.get(PROPKEY_FILENAME);
    File scheduleDir = new File(SCHEDULE_DIRECTORY_NAME);
    scheduleFileAbsoultePath = scheduleDir.getAbsolutePath() + File.separator + scheduleFileName;

    // look for schedule file and load events if existing
    File scheduleFile = new File(SCHEDULE_DIRECTORY_NAME + File.separator + scheduleFileName);
    if (scheduleFile.exists()) {
      loadEvents(scheduleFile);
    }

    // activate the event executor
    executor.scheduleAtFixedRate(eventExecutor, 5, 1, TimeUnit.SECONDS);
    log.info("Activated. Listening for changes on " + scheduleFileAbsoultePath);
  }

  protected void deactivate(ComponentContext cc) {
    executor.shutdownNow();     // shut down the event executor
    log.info("Deactivated.");
  }

  /**
   * Removes all events from the event list
   */
  private void clearSchedule() {
    events.clear();
  }

  /**
   * Loads all VEVENTs from the iCal file into the event list. Events in iCal
   * are UTC. When events are created, the configured time zone offset is added
   * to the timestamp.
   *
   * TODO add support for daylight savings time
   *
   * @param file iCal file that holds the schedule
   */
  private void loadEvents(File file) {
    log.info("Loading schedule from " + file.getName());

    long timeZoneOffset = config.getLong(PROPKEY_TZOFFSET);  // get time zone offset
    timeZoneOffset *= 1000 * 60 * 60;                        // make time zone offset hours
    String agentName = config.get(PROPKEY_AGENTNAME);        // get agent name
    int trackerLeadTime = config.getInt(PROPKEY_LEADTIME);   // get tracker lead time

    synchronized (events) {
      try {
        List<Event> newEvents = new LinkedList<Event>();
        List<VEvent> eventList = ICalendar.parseVEvents(new FileInputStream(file));

        for (VEvent vevent : eventList) {
          String location = vevent.getLocation();
          if (agentName.isEmpty() || (location != null && location.equals(agentName))) {

            // create start events, apply configured time zone offset to UTC dates from iCal
            Date startDate = new Date(vevent.getStart().getTime() + timeZoneOffset);
            Event startTracker = new Event(startDate.getTime(), Event.Action.START_TRACKING);
            newEvents.add(startTracker);
            Event startCamera = new Event(startDate.getTime() + trackerLeadTime, Event.Action.START_CAMERACONTROL);
            newEvents.add(startCamera);

            // create stop events, apply configured time zone offset to UTC dates from iCal
            Date stopDate = new Date(vevent.getEnd().getTime() + timeZoneOffset);
            Event stopTracker = new Event(stopDate.getTime(), Event.Action.STOP_TRACKING);
            newEvents.add(stopTracker);
            Event stopCamera = new Event(stopDate.getTime() - 1, Event.Action.STOP_CAMERACONTROL);
            newEvents.add(stopCamera);

            log.info("Created recording event:  Start: " + startDate.toString() + "  End: " + stopDate.toString());
          }
        }
        events.clear();                             // clear schedule 
        eventExecutor.reset();                      // reset the event executor
        events.addAll(newEvents);                   // load new events
        events.cutHead(System.currentTimeMillis()); // discard events from the past

      } catch (Exception e) {
        log.error("Unable to load calendar. ", e);
        throw new RuntimeException("Unable to load calendar. ", e);
      }
    }
  }

  /**
   * Ensures that object tracking is running.
   */
  private void ensureTrackingON() {
    try {
      if (!heart.isRunning()) {
        if (!heart.isReady()) {
          heart.init();
        }
        heart.go();
        log.info("Object Tracker activated.");
      } else {
        log.info("Object Tracker is already active.");
      }
    } catch (Exception e) {
      log.error("Unexpected error in ensureTrackingON.", e);
    }
  }

  /**
   * Ensures that objects tracking is not running.
   */
  private void ensureTrackingOFF() {
    try {
      if (heart.isRunning()) {
        heart.stop();
        log.info("Stopped Object Tracking");
      } else {
        log.info("Object Tracking is already deactivated.");
      }
    } catch (Exception e) {
      log.error("Unexpected error in ensureTrackingOFF.", e);
    }
  }

  /**
   * Ensures that camera control is running.
   */
  private void ensureCameraControlON() {
    try {
      if (camera != null) {
        if (!camera.isSteering()) {
          camera.setSteering(true);
          log.info("Camera Control activated.");
        } else {
          log.info("Camera Control is already active.");
        }
      } else {
        log.warn("Activation of camera contol failed! No camera controller present.");
      }
    } catch (Exception e) {
      log.error("Unexpected error in ensureCameraControlON.", e);
    }
  }

  /**
   * Ensures that camera control is not running.
   */
  private void ensureCameraControlOFF() {
    try {
      if (camera != null) {
        if (camera.isSteering()) {
          camera.setSteering(false);
          log.info("Camera Control deactivated.");
        } else {
          log.info("Camera Control is already deactivated.");
        }
      } else {
        log.warn("No camera controller present.");
      }
    } catch (Exception e) {
      log.error("Unexpected error in ensureCameraControlOFF.", e);
    }
  }

  @Override
  public void install(File file) throws Exception {
    clearSchedule();
    loadEvents(file);
  }

  @Override
  public void update(File file) throws Exception {
    clearSchedule();
    loadEvents(file);
  }

  @Override
  public void uninstall(File file) throws Exception {
    clearSchedule();
  }

  @Override
  public boolean canHandle(File file) {
    return file.getAbsolutePath().equals(scheduleFileAbsoultePath);
  }

  /**
   * Periodically called
   * <code>Runable</code> that is responsible for starting and stopping the
   * tracking/camera control.
   */
  class EventExecutor implements Runnable {

    Event current = null;   // current event
    Event next = null;      // next event to come
    long now;               // time at the beginning of the execution of run()

    /**
     * Resets this object into original state.
     */
    public void reset() {
      current = null;
      next = null;
    }

    @Override
    public void run() {
      now = System.currentTimeMillis();   // get current time

      synchronized (events) {
        // try to load last event if we don't have one
        if (current == null) {
          current = events.getLastBefore(now);
        }

        ensureEvents();

        // check if next has become current
        if (next != null && now >= next.getTime()) {
          current = next;
          ensureEvents();
          next = null;
        }

        // try to load next event if we don't have one
        if (next == null) {
          next = events.getNextAfter(now);
        }
      }
    }

    /**
     * Ensure that action associated with current event was set in motion.
     */
    void ensureEvents() {
      if (current != null && now >= current.getTime()) {
        log.debug("Firing action " + current.getAction().name());
        switch (current.getAction()) {
          case START_TRACKING:
            ensureTrackingON();
            break;
          case STOP_TRACKING:
            ensureTrackingOFF();
            break;
          case START_CAMERACONTROL:
            ensureCameraControlON();
            break;
          case STOP_CAMERACONTROL:
            ensureCameraControlOFF();
            break;
        }
        events.remove(current);
        current = null;
      }
    }
  }
}
