package cv.lecturesight.main.scheduler;

import cv.lecturesight.main.HeartBeat;
import cv.lecturesight.util.Log;
import cv.lecturesight.util.conf.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.dutyscheduler", immediate = true)
@Service
public class DutyScheduler implements ArtifactInstaller {

  final static String PROPKEY_LEADTIME = "scheduler.tracker.leadtime";
  private Log log = new Log("Duty Scheduler");
  @Reference
  Configuration config;
  @Reference
  HeartBeat heart;
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private CalendarBuilder cBuilder = new CalendarBuilder();
  private EventList events = new EventList();

  protected void activate(ComponentContext cc) {
    executor.scheduleAtFixedRate(new EventExecutor(), 5, 1, TimeUnit.SECONDS);
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
        Calendar calendar = cBuilder.build(new FileInputStream(file));
        ComponentList list = calendar.getComponents(net.fortuna.ical4j.model.Component.VEVENT);
        
        events.clear();
        for (Iterator<net.fortuna.ical4j.model.Component> it = list.iterator(); it.hasNext();) {
          VEvent vevent = (VEvent)it.next();
          
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
    return file.getName().equalsIgnoreCase("schedule.ics");
  }

  /** <code>Runnable</code> that is responsible for starting and stopping the
   * tracking.
   */
  class EventExecutor implements Runnable {

    Event current = null;
    Event next = null;

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
          if (current != null) {           // remove last from eventQueue to optimize search performance
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
