package cv.lecturesight.ui.impl;

import cv.lecturesight.ui.DisplayRegistration;
import cv.lecturesight.ui.DisplayWindow;
import java.util.Iterator;
import java.util.Map;

public class DisplayCommands {

  public static final String[] commands = {"list", "show", "hide"};
  private Map<Integer, DisplayRegistration> displays;

  public DisplayCommands(Map<Integer, DisplayRegistration> displays) {
    this.displays = displays;
  }

  public void list() {
    StringBuilder sb = new StringBuilder();
    sb.append("   Id   State          Name\n");
    for (Iterator<DisplayRegistration> it = displays.values().iterator(); it.hasNext();) {
      DisplayRegistration reg = it.next();
      String id = Integer.toString(reg.getID());
      DisplayWindow window = reg.getWindow();
      String active = window.isActive() ? "active" : "inactive";
      //String title = reg.getSID() + "(" + window.getTitle() + ")";
      String title = reg.getSID();
      sb.append("[");
      sb.append(rightAlign(id, 4));
      sb.append("] [");
      sb.append(leftAlign(active, 13));
      sb.append("] ");
      sb.append(title);
      sb.append("\n");
    }
    System.out.println(sb.toString());
  }

  private String rightAlign(String in, int places) {
    String out = "";
    int fill = places - in.length();
    while (fill-- > 0) {
      out += " ";
    }
    return out + in;
  }

  private String leftAlign(String in, int places) {
    int fill = places - in.length();
    while (fill-- > 0) {
      in += " ";
    }
    return in;
  }

  public void show(String[] args) {
    try {
      int id = Integer.parseInt(args[0]);
      if (displays.containsKey(id)) {
        DisplayWindow window = displays.get(id).getWindow();
        window.show();
      } else {
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      System.out.println("Usage: show <ID>");
    }
  }

  public void hide(String[] args) {
    try {
      int id = Integer.parseInt(args[0]);
      if (displays.containsKey(id)) {
        DisplayWindow window = displays.get(id).getWindow();
        window.hide();
      } else {
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      System.out.println("Usage: hise <ID>");
    }
  }
}
