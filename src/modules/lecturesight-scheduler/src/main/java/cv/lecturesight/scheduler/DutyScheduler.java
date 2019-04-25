package cv.lecturesight.scheduler;

import cv.lecturesight.heartbeat.api.HeartBeat;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.scheduler.ical.ICalendar;
import cv.lecturesight.scheduler.ical.VEvent;
import cv.lecturesight.util.DummyInterface;
import cv.lecturesight.util.conf.Configuration;
import cv.lecturesight.util.metrics.MetricsService;

import lombok.Setter;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A service that loads a schedule from a iCal file and starts/stops object
 * tracking and camera operator accordingly.
 */
public class DutyScheduler implements ArtifactInstaller, DummyInterface {

  final static String PROPKEY_LEADTIME = "tracker.leadtime";
  final static String PROPKEY_FILENAME = "schedule.file";
  final static String PROPKEY_TZOFFSET = "timezone.offset";
  final static String PROPKEY_AGENTNAME = "agent.name";
  final static String PROPKEY_ENABLE = "enable";
  @Setter
  Configuration config;
  @Setter
  HeartBeat heart;
  @Setter
  CameraOperator operator;
  @Setter
  MetricsService metrics;

  boolean enable = true;
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private EventExecutor eventExecutor = new EventExecutor();
  private EventList events = new EventList();
  private String scheduleFileName;
  private String scheduleFileAbsolutePath;

  private BundleContext bundleContext;

  protected void activate(ComponentContext cc) {

    bundleContext = cc.getBundleContext();

    // look for schedule file and load events if existing
    scheduleFileName = config.get(PROPKEY_FILENAME);
    File scheduleFile = new File(scheduleFileName);
    scheduleFileAbsolutePath = scheduleFile.getAbsolutePath();

    // Is the scheduler enabled?
    enable = config.getBoolean(PROPKEY_ENABLE);
    if (!enable) {
      Logger.info("Activated. Scheduler is not enabled.");
      return;
    }

    // Tracking and camera operator are initially stopped
    Logger.info("Stopping object tracking and camera operator");
    stop();

    // activate the event executor
    executor.scheduleAtFixedRate(eventExecutor, 5, 1, TimeUnit.SECONDS);
    Logger.info("Activated. Listening for changes on " + scheduleFileAbsolutePath);
  }

  protected void deactivate(ComponentContext cc) {
    executor.shutdownNow();     // shut down the event executor
    Logger.info("Deactivated.");
  }

  /**
   * System status
   */
  public String getStatus() {
    return (heart.isRunning() && operator.isRunning()) ? "active" : "idle";
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
    Logger.info("Loading schedule from " + file.getName());

    Date now = new Date();

    long timeZoneOffset = config.getLong(PROPKEY_TZOFFSET);  // get time zone offset
    timeZoneOffset *= 1000 * 60 * 60;                        // make time zone offset hours
    String agentName = config.get(PROPKEY_AGENTNAME);        // get agent name
    int trackerLeadTime = config.getInt(PROPKEY_LEADTIME);   // get tracker lead time
    trackerLeadTime *= 1000;                                 // tracker lead time is in seconds

    synchronized (events) {
      try {
        List<Event> newEvents = new LinkedList<Event>();
        List<VEvent> eventList = ICalendar.parseVEvents(new FileInputStream(file));

        for (VEvent vevent : eventList) {
          String location = vevent.getLocation();
          if (agentName.isEmpty() || (location != null && location.equals(agentName))) {

            Date startDate = new Date(vevent.getStart().getTime() + timeZoneOffset);
            Date stopDate = new Date(vevent.getEnd().getTime() + timeZoneOffset);

            // create start events, apply configured time zone offset to UTC dates from iCal
            Event startTracker = new Event(startDate.getTime(), Event.Action.START_TRACKING, vevent.getUID());
            Event startOperator = new Event(startDate.getTime() + trackerLeadTime, Event.Action.START_OPERATOR, vevent.getUID());

            // Is this event in progress?
            if (now.after(startDate) && now.before(stopDate)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(stopDate);
                cal.add(Calendar.SECOND, -10);
              if (now.before(cal.getTime())) {
                Logger.info("Immediate start for event in progress: Start: {} End: {}  UID: {}", startDate, stopDate, vevent.getUID());
                fireEvent(startTracker);
                fireEvent(startOperator);
              } else {
                Logger.info("Ingoring event in progress which finishes within 10s: Start: {} End: {}  UID: {}", startDate, stopDate, vevent.getUID());
              }
            } else {
              Logger.info("Created recording event: Start: {} End: {}  UID: {}", startDate, stopDate, vevent.getUID());
              newEvents.add(startTracker);
              newEvents.add(startOperator);
            }

            // create stop events, apply configured time zone offset to UTC dates from iCal
            Event stopTracker = new Event(stopDate.getTime(), Event.Action.STOP_TRACKING, vevent.getUID());
            newEvents.add(stopTracker);
            Event stopOperator = new Event(stopDate.getTime() - 1, Event.Action.STOP_OPERATOR, vevent.getUID());
            newEvents.add(stopOperator);

            Logger.info("Created recording event: Start: {} End: {}  UID: {}", startDate, stopDate, vevent.getUID());
          }
        }
        events.clear();                             // clear schedule
        eventExecutor.reset();                      // reset the event executor
        events.addAll(newEvents);                   // load new events
        events.removeBefore(System.currentTimeMillis()); // discard events from the past

      } catch (Exception e) {
        Logger.error("Unable to load calendar. ", e);
        throw new RuntimeException("Unable to load calendar. ", e);
      }
    }
  }

  /**
   * Ensures that object tracking is running.
   */
  public void startTracking() {
    try {
      if (!heart.isRunning()) {
        if (!heart.isReady()) {
          heart.init();
        }
        heart.go();
        Logger.info("Object Tracker activated.");
      } else {
        Logger.info("Object Tracker is already active.");
      }
    } catch (Exception e) {
      Logger.error("Unexpected error in startTracking.", e);
    }
  }

  /**
   * Ensures that objects tracking is not running.
   */
  public void stopTracking() {
    try {
      heart.stop();
      Logger.info("Stopped Object Tracking");
    } catch (Exception e) {
      Logger.error("Unexpected error in stopTracking.", e);
    }
  }

  /**
   * Start and stop camera operator
   */
  public void startOperator() {
    operator.start();
  }

  public void stopOperator() {
    operator.stop();
  }

  @Override
  public void install(File file) throws Exception {
    if (!enable) return;

    clearSchedule();
    loadEvents(file);
  }

  @Override
  public void update(File file) throws Exception {
    if (!enable) return;

    clearSchedule();
    loadEvents(file);
  }

  @Override
  public void uninstall(File file) throws Exception {
    if (!enable) return;

    clearSchedule();
  }

  @Override
  public boolean canHandle(File file) {
    return enable ? file.getAbsolutePath().equals(scheduleFileAbsolutePath) : false;
  }

  /*
   * Commands
   */
  public void start() {
    metrics.reset();
    startTracking();
    startOperator();
  }

  public void stop() {
    stopOperator();
    stopTracking();
    metrics.pause();
    metrics.save();
  }

  public void status() {
    //CHECKSTYLE:OFF
    System.out.println(getStatus());
    //CHECKSTYLE:ON
  }

  /**
   * Ensure that action associated with current event was set in motion.
   */
  void fireEvent(Event event) {
    Logger.debug("Firing action " + event.getAction().name() + " for time " + event.getTime());
    switch (event.getAction()) {

      case START_TRACKING:
        metrics.reset();
        startTracking();
        break;

      case STOP_TRACKING:
        stopTracking();
        metrics.pause();
        metrics.save(event.getUID());
        break;

      case START_OPERATOR:
        startOperator();
        break;

      case STOP_OPERATOR:
        stopOperator();
        break;

      default:
        break;
    }
  }

  /**
   * Periodically called
   * <code>Runnable</code> that is responsible for starting and stopping the
   * tracking and camera operator.
   */
  class EventExecutor implements Runnable {

    /**
     * Resets this object into original state.
     */
    public void reset() {
    }

    @Override
    public void run() {

      // still alive?
      if (!heart.isAlive()) {
        Logger.error("Heartbeat is dead: shutting down");
        stop();
        try {
          bundleContext.getBundle(0).stop();
        } catch (Exception e) {
          // Ignore
        }
      }

      synchronized (events) {
        long now = System.currentTimeMillis();   // get current time
        Event current = events.getNextAfter(0);   // get earliest event

        boolean eventActivity = false;

        // Fire pending events if there are any
        while ((current != null)  && (current.getTime() <= now)) {
          eventActivity = true;
          fireEvent(current);
          events.remove(current);
          now = System.currentTimeMillis();
          current = events.getNextAfter(0);
        }

        // Step when inactive so that overview images are snapshotted (if configured). This means that
        // overview images will continue to be processed and displayed at 1fps (execution interval),
        // without any analysis or camera movement taking place.
        if (!eventActivity && !heart.isRunning()) {
          // TODO This appears to cause issues with NVIDIA cards
          // heart.step(1);
        }
      }
    }
  }
}
