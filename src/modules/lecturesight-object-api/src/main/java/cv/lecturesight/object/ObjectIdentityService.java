package cv.lecturesight.object;

import java.util.List;

public interface ObjectIdentityService {

  TrackerObject match(TrackerObject matchee, List<TrackerObject> others, float threshold);
  
}
