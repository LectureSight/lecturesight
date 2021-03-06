/* Copyright (C) 2012 Benjamin Wulff
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
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
