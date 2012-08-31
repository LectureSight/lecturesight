package cv.lecturesight.display.impl;

import cv.lecturesight.display.Display;
import java.util.HashMap;
import java.util.Map;

public class DisplayCommands {

  static final String[] commands = {"list", "show", "hide"};
  private DisplayServiceFactory parent;
  private Map<Integer,DisplayWindow> displayWindows = new HashMap<Integer,DisplayWindow>();
  
  public DisplayCommands(DisplayServiceFactory parent) {
    this.parent = parent;
  }

  public void list() {
    StringBuilder sb = new StringBuilder();
    sb.append("   Id   State          Name\n");
    for (DisplayRegistrationImpl reg : parent.displays.keySet()) {
      String id = Integer.toString(reg.getID());
      Display display = parent.displays.get(reg);
      String active = display.isActive() ? "active" : "inactive";
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
      if (displayWindows.containsKey(id)) {
        displayWindows.get(id).setVisible(true);
      } else {
        for (DisplayRegistrationImpl reg : parent.displays.keySet()) {
          if (reg.getID() == id) {
            DisplayWindow window = new DisplayWindow(reg.getSID(), parent.displays.get(reg));
            displayWindows.put(id, window);
          }
        }
      }
    } catch (Exception e) {
      System.out.println("usage: display:show <id>");
    }
  }

  public void hide(String[] args) {
    try {
      int id = Integer.parseInt(args[0]);
      if (displayWindows.containsKey(id)) {
        DisplayWindow window = displayWindows.get(id);
        window.setVisible(false);
        window.display.deactivate();
        displayWindows.remove(id);
      }
    } catch (Exception e) {
      System.out.println("usage: display:hide <id>");
    }    
  }
}
