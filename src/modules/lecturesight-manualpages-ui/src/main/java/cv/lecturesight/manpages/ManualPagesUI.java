package cv.lecturesight.manpages;

import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;

@Component(name = "lecturesight.cameracalibration", immediate = true)
@Service
public class ManualPagesUI implements UserInterface, BundleListener {

  Log log = new Log("Manual Pages UI");
  ManPagesModel model = new ManPagesModel();
  ManualPagesPanel ui = new ManualPagesPanel(this);

  protected void activate(ComponentContext cc) throws Exception {
    cc.getBundleContext().addBundleListener(this);
  }
  
  @Override
  public String getTitle() {
    return Constants.UI_TITLE;
  }

  @Override
  public JPanel getPanel() {
    return ui;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }

  @Override
  public void bundleChanged(BundleEvent be) {
    switch(be.getType()) {
      case BundleEvent.STARTED:
      case BundleEvent.UPDATED:
        installManPages(be.getBundle());
        break;
      case BundleEvent.STOPPED:
        uninstallManPages(be.getBundle());
        break;
    }
  }

  private void installManPages(Bundle bundle) {
    log.debug("Searching for manual pages in bundle " + bundle.getSymbolicName());
    Enumeration pe = bundle.findEntries("manual", "*.md", false);
    
    if (pe != null && pe.hasMoreElements()) {
      model.addBundleNode(bundle);
      
      List<ManPage> pages = new LinkedList<ManPage>();
      int num = 0;
      for (;pe.hasMoreElements();) {
        URL pageUrl = (URL) pe.nextElement();
        ManPage page = new ManPage(urlReadClose(pageUrl));
        pages.add(page);
        num++;
      }
      model.addPages(bundle, pages);
      log.debug("Added " + num + " page(s) from bundle " + bundle.getSymbolicName());
    }
  }

  String urlReadClose(URL url) {
    String out = "";
    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
      String line;
      StringBuilder sb = new StringBuilder();
      while ((line = r.readLine()) != null) {
        sb.append(line).append("\n");
      }
      r.close();
      out = sb.toString();    // TODo use finally block
    } catch(IOException e) {
      log.warn("IOException while reading " + url.toString());
      throw new RuntimeException(e);
    }
    return out;
  }
  
  private void uninstallManPages(Bundle bundle) {
  }
}
