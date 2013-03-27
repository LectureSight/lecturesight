package cv.lecturesight.main;

import cv.lecturesight.util.Log;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.dutyscheduler", immediate = true)
@Service
public class DutyScheduler implements ArtifactInstaller {

  private Log log = new Log("Duty Scheduler");
  
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  protected void activate(ComponentContext cc) {
    
    log.info("Activated. Listening for iCal file.");
  }
  
  protected void deactivate(ComponentContext cc) {
    log.info("Deactivated.");
  }
  
  private void clearSchedule() {
    
  }
  
  private void loadFromICal(File file) {
    
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
  
  class Event implements Comparable {
    private Long start;
    private Long duration;
    
    public Event(long start, long duration) {
      this.start = start;
      this.duration = duration;
    }
    
    public long getStart() {
      return start;
    }
    
    public long getDuration() {
      return duration;
    }
    
    public long getEnd() {
      return start + duration;
    }

    @Override
    public int compareTo(Object o) {
      Long otherStart = ((Event)o).getStart();
      return start.compareTo(otherStart);
    }
  }
 
  class EventQueue {
    
    private Queue<Event> queue = new ConcurrentLinkedQueue<Event>();
    
    public void clear() {
      queue.clear();
    }
    
  }
}
