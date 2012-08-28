package cv.lecturesight.display.impl;

import cv.lecturesight.display.Display;
import java.util.Iterator;
import java.util.Map;

public class DisplayCommands {

  public static final String[] commands = {"list", "show", "hide"};
  private Map<Integer, DisplayRegistrationImpl> displays;

  public DisplayCommands(Map<Integer, DisplayRegistrationImpl> displays) {
    this.displays = displays;
  }

  public void list() {
    StringBuilder sb = new StringBuilder();
    sb.append("   Id   State          Name\n");
    for (Iterator<DisplayRegistrationImpl> it = displays.values().iterator(); it.hasNext();) {
      DisplayRegistrationImpl reg = it.next();
      String id = Integer.toString(reg.getID());
      Display window = reg.getDisplay();
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
        Display window = displays.get(id).getDisplay();
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
        Display window = displays.get(id).getDisplay();
        window.hide();
      } else {
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      System.out.println("Usage: hise <ID>");
    }
  }
}
