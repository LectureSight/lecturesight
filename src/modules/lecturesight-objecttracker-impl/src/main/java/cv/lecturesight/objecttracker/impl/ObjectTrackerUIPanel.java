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
package cv.lecturesight.objecttracker.impl;

import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayPanel;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.util.geometry.BoundingBox;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class ObjectTrackerUIPanel extends JPanel implements CustomRenderer {

  static final String OBJ_PROPKEY_COLOR = "obj.color";
  private Display display;
  private DisplayPanel displayPanel;
  private ObjectTracker oTracker;
  private Font font = new Font("Monospaced", Font.PLAIN, 10);

  public ObjectTrackerUIPanel(Display display, ObjectTracker oTracker) {
    this.display = display;
    this.oTracker = oTracker;
    initComponents();
    displayPanel = display.getDisplayPanel();
    displayPanel.setCustomRenderer(this);
    outputPanel.setLayout(new BorderLayout());
    outputPanel.add(displayPanel, BorderLayout.CENTER);
  }

  @Override
  public void render(Graphics g) {

    // draw ObjectTracker data
    List<TrackerObject> objects = oTracker.getCurrentlyTracked();
    Map<Integer, TrackerObject> all_o = oTracker.getAllObjects();
    for (TrackerObject object : objects) {
      g.setColor((Color) object.getProperty(OBJ_PROPKEY_COLOR));

      BoundingBox box = (BoundingBox) object.getProperty("obj.bbox");
      g.drawRect(box.getMin().getX(), box.getMin().getY(), box.getWidth(), box.getHeight());

      String info = Integer.toString(object.getId());
      g.setFont(font);
      g.drawString(info, box.getMin().getX(), box.getMin().getY() - 1);

      if (object.hasProperty("head.center")) {
        g.setColor(Color.cyan);
        BoundingBox hbox = (BoundingBox) object.getProperty("head.boundingbox");
        g.drawRect(box.getMin().getX() + hbox.getMin().getX(),
                box.getMin().getY() + hbox.getMin().getY(),
                hbox.getWidth(), hbox.getHeight());
      }

//      int x = box.getMin().getX();
//      int y = box.getMax().getY() + 8;
//      g.setFont(smallFont);
//      for (Iterator<String> pit = object.getProperties().keySet().iterator(); pit.hasNext();) {
//        String key = pit.next();
//        Object val = object.getProperty(key);
//        String prop = key + ": " + val.toString();
//        g.drawString(prop, x, y);
//        y += 10;
//      }
    }

    // draw frame information
    g.setColor(Color.white);
    g.setFont(font);
    g.drawString("objects : " + objects.size(), 2, 12);
    g.drawString("tracked objects : " + all_o.size(), 2, 22);
  }

  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outputPanel = new javax.swing.JPanel();
        viewTabs = new javax.swing.JTabbedPane();

        outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        viewTabs.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel outputPanel;
    private javax.swing.JTabbedPane viewTabs;
    // End of variables declaration//GEN-END:variables
}
