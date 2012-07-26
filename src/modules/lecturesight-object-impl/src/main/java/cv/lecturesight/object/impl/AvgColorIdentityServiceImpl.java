package cv.lecturesight.object.impl;

import cv.lecturesight.object.ObjectIdentityService;
import cv.lecturesight.object.TrackerObject;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component(name="lecturesight.objects.identity",immediate=true)
@Service
public class AvgColorIdentityServiceImpl implements ObjectIdentityService {

  @Override
  public TrackerObject match(TrackerObject matchee, List<TrackerObject> others, float threshold) {
    return null;
  }

  @Override
  public void update(List<TrackerObject> singletons) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
