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
    optionsPanel1 = new javax.swing.JPanel();

    outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

    javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
    outputPanel.setLayout(outputPanelLayout);
    outputPanelLayout.setHorizontalGroup(
      outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 630, Short.MAX_VALUE)
    );
    outputPanelLayout.setVerticalGroup(
      outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 341, Short.MAX_VALUE)
    );

    optionsPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));

    javax.swing.GroupLayout optionsPanel1Layout = new javax.swing.GroupLayout(optionsPanel1);
    optionsPanel1.setLayout(optionsPanel1Layout);
    optionsPanel1Layout.setHorizontalGroup(
      optionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 630, Short.MAX_VALUE)
    );
    optionsPanel1Layout.setVerticalGroup(
      optionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 55, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(optionsPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(optionsPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel optionsPanel1;
  private javax.swing.JPanel outputPanel;
  // End of variables declaration//GEN-END:variables
}
