package cv.lecturesight.object;

import java.util.List;

public interface ObjectIdentityService {

  void update(List<TrackerObject> singletons);
  TrackerObject match(TrackerObject matchee, List<TrackerObject> others, float threshold);
  
}
