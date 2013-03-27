package cv.lecturesight.main.scheduler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class EventQueue {
  
  Set<Event> events = new TreeSet<Event>();

  Event getLastBefore(long time) {
    Event lastBefore = null;
    for (Iterator<Event> it = events.iterator(); it.hasNext();) {
      Event event = it.next();
      if (event.getTime() < time) {
        lastBefore = event;
      }
    }
    return lastBefore;
  }

  Event getNextAfter(long time) {
    for (Iterator<Event> it = events.iterator(); it.hasNext();) {
      Event event = it.next();
      if (time < event.getTime()) {
        return event;
      }
    }
    return null;        
  }
  
  void add(Event e) {
    events.add(e);
  }
  
  void addAll(Collection<Event> events) {
    this.events.addAll(events);
  }
  
  void remove(Event event) {
    events.remove(event);
  }
}
