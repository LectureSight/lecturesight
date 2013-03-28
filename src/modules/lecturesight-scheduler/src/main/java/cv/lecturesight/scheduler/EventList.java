package cv.lecturesight.scheduler;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** An sorted list of <code>Event</code> from which an event nearest to a 
 * certain point in time can be retrieved.
 */
public class EventList {
  
  Set<Event> events = new TreeSet<Event>();

  synchronized Event getLastBefore(long time) {
    Event lastBefore = null;
    for (Iterator<Event> it = events.iterator(); it.hasNext();) {
      Event event = it.next();
      if (event.getTime() <= time) {
        lastBefore = event;
      } else {
        break;
      }
    }
    return lastBefore;
  }

  synchronized Event getNextAfter(long time) {
    for (Iterator<Event> it = events.iterator(); it.hasNext();) {
      Event event = it.next();
      if (time <= event.getTime()) {
        return event;
      }
    }
    return null;        
  }
  
  /** Remove all events from the beginning of the list to the last event before
   * <code>time</code>. Doing so when a list of events has be loaded form a 
   * calendar removes all events that will never be relevant. This will lead to
   * <code>getLastBefore</code> and <code>getFirstAfter</code> working in only 
   * one step in their search loop.
   */
  synchronized void cutHead(long time) { 
    Event last = getLastBefore(time);
    List<Event> remove = new LinkedList<Event>();
    for (Iterator<Event> it = events.iterator(); it.hasNext();) {
      Event event = it.next();
      if (event != last) {
        remove.add(event);
      }
    }
    if (remove.size() > 0) {
      events.removeAll(remove);
    }
  }
  
  synchronized void add(Event e) {
    events.add(e);
  }
  
  synchronized void addAll(Collection<Event> events) {
    this.events.addAll(events);
  }
  
  synchronized void remove(Event event) {
    events.remove(event);
  }
  
  synchronized void clear() {
    events.clear();
  }
}
