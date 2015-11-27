package cv.lecturesight.manpages;

import cv.lecturesight.gui.api.UserInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

@Component(name = "lecturesight.cameracalibration", immediate = true)
@Service
@Properties({
  @Property(name = "osgi.command.scope", value = "man"),
  @Property(name = "osgi.command.function", value = {"list"})
})
public class ManualPagesUI implements UserInterface, BundleListener {

  ManPagesModel model = new ManPagesModel();
  ManualPagesPanel ui = new ManualPagesPanel(this);

  protected void activate(ComponentContext cc) throws Exception {
    cc.getBundleContext().addBundleListener(this);
    
    // search for man pages of already installed bundles
    Logger.info("Searching for manual pages.");
    for (Bundle b : cc.getBundleContext().getBundles()) {
      installManPages(b);
    }
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
    Logger.debug("Searching for manual pages in bundle " + bundle.getSymbolicName());
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
      Logger.debug("Added " + num + " page(s) from bundle " + bundle.getSymbolicName());
    }
  }

  private void uninstallManPages(Bundle bundle) {
    Logger.debug("Uninstalling manual pages from bundle " + bundle.getSymbolicName());
    model.removeBundleNode(bundle);
  }
  
  /** Reads the content retrieved from <code>url</code> via a BufferedReader
   * into a String and closes the stream quietly. The method also silences 
   * IOExceptions.
   * 
   * @param url
   * @return Content retrieved from url
   */ 
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
      Logger.warn("IOException while reading " + url.toString());
      throw new RuntimeException(e);
    }
    return out;
  }
  
  public void list(String args[]) {
    StringBuilder sb = new StringBuilder();
    sb.append("Manual Pages:\n\n");
    for (BundleNode bn : model.nodes) {
      sb.append("  ").append(bn.toString()).append(" (").append(bn.pages.size()).append(")\n");
      for (ManPage m : bn.pages) {
        sb.append("      ").append(m.getTitle()).append("\n");
      }
    }
    System.out.println(sb.toString());
  }
}
