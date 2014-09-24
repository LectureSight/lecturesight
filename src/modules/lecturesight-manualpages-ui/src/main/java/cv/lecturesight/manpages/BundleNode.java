package cv.lecturesight.manpages;

import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.Bundle;

public class BundleNode {

  Bundle bundle;
  List<ManPage> pages = new LinkedList<ManPage>();
  
  public BundleNode(Bundle bundle) {
    this.bundle = bundle;
  }
  
  public String toString() {
    Dictionary d = bundle.getHeaders();
    String name = (String)d.get("Bundle-Name");
    if (name == null) {
      name = bundle.getSymbolicName();
    }
    return name;
  }
}
