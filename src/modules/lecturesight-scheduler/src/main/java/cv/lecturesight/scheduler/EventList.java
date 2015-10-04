package cv.lecturesight.scheduler;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An sorted list of
 * <code>Event</code> from which an event nearest to a certain point in time can
 * be retrieved.
 */
public class EventList {

  /* <code>TreeSet</code> of <code>Event</code>, implements <code>SortedSet</code>
   * so it's <code>Iterator</code> returns elements in natural order.
   */
  Set<Event> events = new TreeSet<Event>();

  /**
   * Returns the event from this list that is the closest before
   * <code>time</code>,
   * <code>null</code> if no such element could be found.
   *
   * @param time point in time
   * @return Event closest before time
   */
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

  /**
   * Returns the next
   * <code>Event</code> after
   * <code>time</code>,
   * <code>null</code> if no such element could be found.
   *
   * @param time point in time
   * @return Event next event after time
   */
  synchronized Event getNextAfter(long time) {
    for (Iterator<Event> it = events.iterator(); it.hasNext();) {
      Event event = it.next();
      if (time <= event.getTime()) {  // element after time?
        return event;                 // return first occurance
      }
    }
    return null;      // nothing found, so we return null
  }

  /**
   * Remove all events from the beginning of the list to the last event before
   * <code>time</code>. Doing so when a list of events has be loaded from a
   * calendar removes all events that will never be relevant. This will lead to
   * <code>getLastBefore</code> and
   * <code>getFirstAfter</code> working in only one step in their search loop.
   */
  synchronized void cutHead(long time) {
    Event last = getLastBefore(time);
    if (last != null) {
      List<Event> remove = new LinkedList<Event>();
      for (Iterator<Event> it = events.iterator(); it.hasNext();) {
        Event event = it.next();
        if (event.getTime() <= last.getTime() && event != last) {
          remove.add(event);
        }
      }
      if (remove.size() > 0) {
        events.removeAll(remove);
      }
    }
  }

  /**
   * Remove all events before <code>time</code>. Doing so when a list of events 
   * has be loaded from a calendar removes all events that will never be relevant.
   */
  synchronized void removeBefore(long time) {
      List<Event> remove = new LinkedList<Event>();
      for (Iterator<Event> it = events.iterator(); it.hasNext();) {
        Event event = it.next();
        if (event.getTime() < time) {
          remove.add(event);
        } else
          break;
      }
      if (remove.size() > 0) {
        events.removeAll(remove);
      }
  }

  /**
   * Add an event to the list.
   *
   * @param event to be added to this list
   */
  synchronized void add(Event e) {
    events.add(e);
  }

  /**
   * Add all events from a collection of events to this list.
   *
   * @param events to be added to this list
   */
  synchronized void addAll(Collection<Event> events) {
    this.events.addAll(events);
  }

  /**
   * Removes an event from this list.
   *
   * @param event to be removed
   */
  synchronized void remove(Event event) {
    events.remove(event);
  }

  /**
   * Removes all events from the list.
   */
  synchronized void clear() {
    events.clear();
  }
}
