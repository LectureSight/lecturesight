package cv.lecturesight.scheduler;

import cv.lecturesight.main.HeartBeat;
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
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * A service that loads a schedule from a iCal file and starts/stops object
 * tracking and camera control accordingsly.
 */
@Component(name = "lecturesight.dutyscheduler", immediate = true)
@Service
public class DutyScheduler implements ArtifactInstaller {

  final static String PROPKEY_LEADTIME = "trackeing.leadtime";
  final static String PROPKEY_FILENAME = "schedule.file";
  final static String PROPKEY_TZOFFSET = "timezone.offset";
  final static String PROPKEY_AGENTNAME = "agent.name";
  final static String SCHEDULE_DIRECTORY = "schedule";
  private Log log = new Log("Duty Scheduler");
  @Reference
  Configuration config;
  @Reference
  HeartBeat heart;
  private String scheduleFileName;
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private EventExecutor eventExecutor = new EventExecutor();
  private EventList events = new EventList();

  protected void activate(ComponentContext cc) {
    // look for schedule file and load events if existing
    scheduleFileName = config.get(PROPKEY_FILENAME);
    File scheduleFile = new File(SCHEDULE_DIRECTORY + File.separator + scheduleFileName);
    if (scheduleFile.exists()) {
      loadEvents(scheduleFile);
    }

    // activate the event executor
    executor.scheduleAtFixedRate(eventExecutor, 5, 1, TimeUnit.SECONDS);
    log.info("Activated. Listening for changes on " + SCHEDULE_DIRECTORY + File.separator + scheduleFileName);
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
    String agentName = config.get(PROPKEY_AGENTNAME);        // get agent name

    synchronized (events) {
      try {
        List<Event> newEvents = new LinkedList<Event>();
        List<VEvent> eventList = ICalendar.parseVEvents(new FileInputStream(file));

        for (VEvent vevent : eventList) {
          String location = vevent.getLocation();
          if (agentName.isEmpty() || (location != null && location.equals(agentName))) {
            
            // create start event, apply configured time zone offset to UTC dates from iCal
            Date startDate = vevent.getStart();
            Event startEvent = new Event(startDate.getTime() + timeZoneOffset, Event.Action.START_TRACKING);
            newEvents.add(startEvent);

            // create stop event, apply configured time zone offset to UTC dates from iCal
            Date stopDate = vevent.getEnd();
            Event stopEvent = new Event(stopDate.getTime() + timeZoneOffset, Event.Action.STOP_TRACKING);
            newEvents.add(stopEvent);

            log.debug("Created event.  Start: " + startDate.toString() + "  End: " + stopDate.toString() + " (times are UTC)");
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
    if (!heart.isRunning()) {
      heart.go();
    log.info("STARTING OBJECT TRACKING");
    }
  }

  /**
   * Ensures that objects tracking is not running.
   */
  private void ensureTrackingOFF() {
    if (heart.isRunning()) {
      heart.stop();
    log.info("STOPPING OBJECT TRACKING");
    }
  }

  /**
   * Ensures that camera control is running.
   */
  private void ensureCameraControlON() {
  }

  /**
   * Ensures that camera control is not running.
   */
  private void ensureCameraControlOFF() {
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
    return file.getName().equals(scheduleFileName) && file.getParent().equals(SCHEDULE_DIRECTORY);
  }

  /**
   * Periodically called <code>Runable</code> that is responsible for starting 
   * and stopping the tracking/camera control.
   */
  class EventExecutor implements Runnable {

    Event current = null;   // current event
    Event next = null;      // next event to come
    long now;

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
          if (current != null) {           // remove last from events
            events.remove(current);
          }
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
      }
    }
  }
}
