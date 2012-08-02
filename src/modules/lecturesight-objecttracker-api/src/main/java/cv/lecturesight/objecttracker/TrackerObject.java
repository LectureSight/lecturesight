package cv.lecturesight.objecttracker;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wulff
 */
public class TrackerObject {
  
  private static int nextId = 1;
  private int id = nextId++;
  private long firstSeen, lastSeen;
  private HashMap<String,Object> properties = new HashMap<String,Object>();
  
  public TrackerObject() {
    firstSeen = System.currentTimeMillis();
    lastSeen = firstSeen;
  }
  
  public TrackerObject(long time) {
    firstSeen = time;
    lastSeen = firstSeen;
  }

  public int getId() {
    return id;
  }
  
  public long firstSeen() {
    return firstSeen;
  }
  
  public void setLastSeen(long time) {
    lastSeen = time;
  }
  
  public long lastSeen() {
    return lastSeen;
  }
  
  public boolean hasProperty(String key) {
    return properties.containsKey(key);
  }
  
  public Object getProperty(String key) {
    return properties.get(key); 
  }
  
  public Map<String,Object> getProperties() {
    return (Map<String,Object>)properties.clone();
  }
  
  public void setProperty(String key, Object value) {
    properties.put(key, value);
  }
}
