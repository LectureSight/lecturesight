package cv.lecturesight.regiontracker;

import java.util.List;

public interface ObjectIdentityService {

  void update(List<Region> singletons);
  Region match(Region matchee, List<Region> others, float threshold);
  
}
