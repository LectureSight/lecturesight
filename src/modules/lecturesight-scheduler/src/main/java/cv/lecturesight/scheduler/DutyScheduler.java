package cv.lecturesight.scheduler;

import cv.lecturesight.heartbeat.api.HeartBeat;
import cv.lecturesight.scheduler.ical.ICalendar;
import cv.lecturesight.scheduler.ical.VEvent;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.dutyscheduler", immediate = true)
@Service
public class DutyScheduler implements ArtifactInstaller {

  final static String PROPKEY_LEADTIME = "trackeing.leadtime";
  final static String PROPKEY_FILENAME = "schedule.file";
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
//    scheduleFileName = config.get(PROPKEY_FILENAME);
//    File scheduleFile = new File(SCHEDULE_DIRECTORY + File.separator + scheduleFileName);
//    if (scheduleFile.exists()) {
//      loadEvents(scheduleFile);
//    }
    executor.scheduleAtFixedRate(eventExecutor, 5, 1, TimeUnit.SECONDS);
    log.info("Activated. Listening for iCal file.");
  }

  protected void deactivate(ComponentContext cc) {
    executor.shutdownNow();
    log.info("Deactivated.");
  }

  private void clearSchedule() {
    events.clear();
  }

  private void loadEvents(File file) {
    log.info("Loading schedule from " + file.getName());
    
    synchronized (events) {
      try {
        List<VEvent> eventList = ICalendar.parseVEvents(new FileInputStream(file));
        
        events.clear();     // clear schedule before loading new events
        for (VEvent vevent : eventList) {
          
          // create start event
          Date startDate = vevent.getStart();
          Event startEvent = new Event(startDate.getTime(), Event.Action.START_TRACKING);
          events.add(startEvent);
          
          // create stop event
          Date stopDate = vevent.getEnd();
          Event stopEvent = new Event(stopDate.getTime(), Event.Action.STOP_TRACKING);
          events.add(stopEvent);
          
          log.debug("Created event.  Start: " + startDate.toString() + "  End: " + stopDate.toString());
          
          eventExecutor.reset();
        }
        
      } catch (Exception e) {
        log.error("Unable to load calendar. ", e);
        throw new RuntimeException("Unable to load calendar. ", e);
      }
    }
  }

  private void ensureTrackingON() {
    if (!heart.isRunning()) {
      heart.go();
      log.info("STARTING OBJECT TRACKING");
    }
  }

  private void ensureTrackingOFF() {
    if (heart.isRunning()) {
      heart.stop();
      log.info("STOPPING OBJECT TRACKING");
    }
  }

  private void ensureCameraControlON() {
  }

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

  /** <code>Runnable</code> that is responsible for starting and stopping the
   * tracking.
   */
  class EventExecutor implements Runnable {

    Event current = null;
    Event next = null;

    public void reset() {
      current = null;
      next = null;
    }
    
    @Override
    public void run() {
      long now = System.currentTimeMillis();

      synchronized (events) {
        // try to load last event if we don't have one
        if (current == null) {
          current = events.getLastBefore(now);
        }

        // ensure action associated with last event was set in motion
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

        // check if <code>next</code> has become <code>current</code>
        if (next != null && now >= next.getTime()) {
          if (current != null) {           // remove last from events
            events.remove(current);
          }
          current = next;
          next = null;
        }

        // try to load next event if we don't have one
        if (next == null) {
          next = events.getNextAfter(now);
        }
      }
    }
  }
}
