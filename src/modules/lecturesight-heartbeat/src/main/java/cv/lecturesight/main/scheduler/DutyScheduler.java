package cv.lecturesight.main.scheduler;

import cv.lecturesight.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.dutyscheduler", immediate = true)
@Service
public class DutyScheduler implements ArtifactInstaller {

  private Log log = new Log("Duty Scheduler");
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
  private CalendarBuilder cBuilder = new CalendarBuilder();
  private EventQueue eventQueue = new EventQueue();

  protected void activate(ComponentContext cc) {
    log.info("Activated. Listening for iCal file.");
  }

  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated.");
  }

  private void clearSchedule() {
    // synchronize with executor!
  }

  private void loadFromICal(File file) {
    log.info("Loading schedule from " + file.getName());

    try {
      Calendar calendar = cBuilder.build(new FileInputStream(file));
      ComponentList list = calendar.getComponents(net.fortuna.ical4j.model.Component.VEVENT);
    } catch (Exception e) {
      log.error("Unable to load calendar. ", e);
      throw new RuntimeException("Unable to load calendar. ", e);
    }
  }

  private void ensureTrackingON() {
    log.info("Ensure Tracking ON");
  }
  
  private void ensureTrackingOFF() {
    log.info("Ensure Tracking OFF");
  }
  
  private void ensureCameraControlON() {
    log.info("Ensure Camera Control ON");
  }
  
  private void ensureCameraControlOFF() {
    log.info("Ensure Camera Control OFF");
  }

  @Override
  public void install(File file) throws Exception {
    clearSchedule();
    loadFromICal(file);
  }

  @Override
  public void update(File file) throws Exception {
    clearSchedule();
    loadFromICal(file);
  }

  @Override
  public void uninstall(File file) throws Exception {
    clearSchedule();
  }

  @Override
  public boolean canHandle(File file) {
    return file.getName().equalsIgnoreCase("schedule.ics");
  }

  /**
   * <
   * code>Runnable</code> that is responsible for starting and stopping the
   * tracking. This is done by caching the last event before current time and
   * the next event after current time.
   */
  class EventExecutor implements Runnable {

    Event last = null;
    Event next = null;

    @Override
    public void run() {
      long now = System.currentTimeMillis();

      // try to load last event if we don't have one
      if (last == null) {
        last = eventQueue.getLastBefore(now);
      }

      // check if action associated with last event was set in motion
      if (last != null && now >= last.getTime()) {
        switch (last.getAction()) {
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

      // check if <code>next</code> has become <code>last</code>
      if (next != null && now >= next.getTime()) {
        if (last != null) {           // remove last from eventQueue to optimize search performance
          eventQueue.remove(last);
        }
        last = next;
        this.run();   // this should only be necessary with long pool period or very fine-grained schedule
      }
      
      // try to load next event if we don't have one
      if (next == null) {
        next = eventQueue.getNextAfter(now);
      }
    }
  }
}
