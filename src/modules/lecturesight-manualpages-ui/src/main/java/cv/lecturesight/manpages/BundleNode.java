package cv.lecturesight.manpages;

import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.Bundle;

/** Model class that represents an OSGI bundle holding a list of manual pages.
 * 
 * @author wulff
 */
public class BundleNode {

  // source bundle
  Bundle bundle;
  
  // list of contained manual pages
  List<ManPage> pages = new LinkedList<ManPage>();
  
  public BundleNode(Bundle bundle) {
    this.bundle = bundle;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other instanceof BundleNode) {
      return this.bundle.getSymbolicName().equals(
              ((BundleNode)other).bundle.getSymbolicName());
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    Dictionary d = bundle.getHeaders();
    String name = (String)d.get("Bundle-Name");
    if (name == null) {
      name = bundle.getSymbolicName();
    }
    return name;
  }
}
