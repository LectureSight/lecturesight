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
package cv.lecturesight.ptz.steering.gui;

import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayPanel;
import cv.lecturesight.display.DisplayRegistration;
import cv.lecturesight.display.DisplayRegistrationListener;
import cv.lecturesight.objecttracker.ObjectTracker;
import cv.lecturesight.objecttracker.TrackerObject;
import cv.lecturesight.operator.CameraOperator;
import cv.lecturesight.ptz.steering.api.CameraSteeringWorker;
import cv.lecturesight.ptz.steering.api.UISlave;
import cv.lecturesight.util.geometry.CoordinatesNormalization;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;
import org.pmw.tinylog.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JPanel;

public class CameraControlPanel extends JPanel implements UISlave, MouseListener, CustomRenderer, DisplayRegistrationListener{

  private static final int DEFAULT_WIDTH = 640;
  private static final int DEFAULT_HEIGHT = 360;

  private CoordinatesNormalization normalizer = new CoordinatesNormalization(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  private CameraSteeringWorker camera;
  private CameraOperator operator;

  private Display cameraDisplay;
  private DisplayPanel cameraDisplayPanel;
  private CameraMovementUI parent;

  // graphics stuff
  private final Color targetColor = Color.yellow;
  private final Color trackedColor = Color.orange;
  private final Color positionColor = Color.cyan;
  private final Color frameColor = Color.cyan;
  private final Font font = new Font("Monospaced", Font.PLAIN, 10);

  public CameraControlPanel(CameraMovementUI parent, CameraSteeringWorker camera, CameraOperator operator) {
    this.parent = parent;
    this.camera = camera;
    this.operator = operator;
    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    try {
      cameraDisplay = parent.dsps.getDisplayBySID("cam.overview.input");
      cameraDisplayPanel = cameraDisplay.getDisplayPanel();
      cameraDisplayPanel.setCustomRenderer(this);
      cameraDisplayPanel.addMouseListener(this);
      this.setLayout(new BorderLayout());
      this.add(cameraDisplayPanel, BorderLayout.CENTER);
      cameraDisplay.activate();
    } catch (IllegalArgumentException e) {
      Logger.info("cam.overview.input not available");
      cameraDisplay = null;
      this.addMouseListener(this);
    }
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if (cameraDisplay == null) {
      render(g);
    }
  }

  @Override
  public void render(Graphics g) {
    updateSize();   // update normalizer in case display size changed

    int width = getWidth();
    int height = getHeight();
    int rootX = getWidth() / 2;
    int rootY = getHeight() / 2;

    float frameWidth = camera.getFrameWidth();

    g.setFont(font);

    if(cameraDisplay == null) {
      // clear panel
      g.setColor(Color.black);
      g.fillRect(0, 0, width, height);
    }

    // draw movement indicator
    g.drawRect(1, 1, 20, 10);
    g.setColor(camera.isMoving() ? Color.green : Color.black);
    g.fillRect(2, 2, 19, 9);
    g.setColor(camera.isMoving() ? Color.black : Color.green);
    g.drawString(camera.isMoving() ? "MOV" : "STL", 2, 10);

    // coordinate cross
    // x axis
    g.setColor(Color.green);
    g.drawLine(1, rootY, getWidth()-2, rootY);

    g.drawLine(1, rootY-3, 1, rootY + 3);
    g.drawLine(rootX/2, rootY-3, rootX/2, rootY + 3);
    g.drawString(Integer.toString(camera.getPanMin()), 2, rootY - 2);
    g.drawLine(getWidth()-2, rootY-3, getWidth()-2, rootY + 3);
    g.drawLine(rootX + (rootX/2), rootY-3, rootX + (rootX/2), rootY + 3);
    g.drawString(Integer.toString(camera.getPanMax()), getWidth() - 30, rootY + 11);

    // y axis
    g.drawLine(rootX, 1, rootX, getHeight()-2);

    g.drawLine(rootX - 3, 1, rootX + 3, 1);
    g.drawLine(rootX - 3, rootY/2, rootX + 3, rootY/2);
    g.drawString(Integer.toString(camera.getTiltMax()), rootX + 2, 12);
    g.drawLine(rootX - 3, getHeight()-2, rootX + 3, getHeight()-2);
    g.drawLine(rootX - 3, rootY + (rootY/2), rootX + 3, rootY + (rootY/2));
    g.drawString(Integer.toString(camera.getTiltMin()), rootX + 2, getHeight() - 3);

    // get camera and target position
    NormalizedPosition tposn = camera.getTargetPosition();
    String targetPosStr = camera.toCameraCoordinates(tposn).toString();

    tposn.setY(tposn.getY() * -1);                      // compute screen position
    Position tpos = normalizer.fromNormalized(tposn);

    NormalizedPosition aposn = camera.getActualPosition();
    String cameraPosStr = camera.toCameraCoordinates(aposn).toString();

    aposn.setY(aposn.getY() * -1);                      // compute screen position
    Position apos = normalizer.fromNormalized(aposn);

    // draw camera and target position
    g.setColor(Color.pink);
    g.drawLine(tpos.getX(), tpos.getY(), apos.getX(), apos.getY());

    g.setColor(targetColor);
    drawCursor(g, tpos.getX(), tpos.getY(), targetColor);
    g.drawString(targetPosStr, tpos.getX() + 5, tpos.getY() - 1);

    g.setColor(positionColor);
    drawCursor(g, apos.getX(), apos.getY(), positionColor);
    g.drawString(cameraPosStr, apos.getX() + 5, apos.getY() + 10);

    // Draw left and right frame boundaries
    g.setColor(frameColor);

    NormalizedPosition frameLeft = new NormalizedPosition(aposn.getX() - frameWidth / 2, aposn.getY());
    NormalizedPosition frameRight = new NormalizedPosition(aposn.getX() + frameWidth / 2, aposn.getY());

    int frameHeight = height / 6;

    if (frameLeft.getX() > -1) {
      Position frameLeftN = normalizer.fromNormalized(frameLeft);
      g.drawLine(frameLeftN.getX(), Math.max(frameLeftN.getY() - frameHeight / 2, 0), frameLeftN.getX(), Math.min(frameLeftN.getY() + frameHeight / 2, height-1));
    }

    if (frameRight.getX() < 1) {
      Position frameRightN = normalizer.fromNormalized(frameRight);
      g.drawLine(frameRightN.getX(), Math.max(frameRightN.getY() - frameHeight / 2, 0), frameRightN.getX(), Math.min(frameRightN.getY() + frameHeight / 2, height-1));
    }

    // Draw targets considered in-frame
    List<TrackerObject> targets = operator.getFramedTargets();
    for (TrackerObject t : targets) {
      // draw target
      Position fpos = (Position) t.getProperty(ObjectTracker.OBJ_PROPKEY_CENTROID);
      drawTarget(g, fpos.getX(), fpos.getY(), trackedColor);
    }

  }

  private void drawCursor(Graphics g, int x, int y, Color color) {
    Color tmp = g.getColor();
    g.setColor(color);
    g.drawLine(x, y, x, y);
    g.drawLine(x, y-4, x-4, y);
    g.drawLine(x, y-4, x+4, y);
    g.drawLine(x, y+4, x-4, y);
    g.drawLine(x, y+4, x+4, y);
    g.setColor(tmp);
  }

  private void drawTarget(Graphics g, int x, int y, Color color) {
    Color tmp = g.getColor();
    g.setColor(color);
    g.fillOval(x-4, y-4, 8, 8);
    g.setColor(tmp);
  }

  private void updateSize() {
    normalizer.setMaxX(getWidth());
    normalizer.setMaxY(getHeight());
  }

  @Override
  public Dimension getPreferredSize() {
    if (cameraDisplay != null) {
      return cameraDisplay.getSize();
    } else {
      return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    NormalizedPosition tpos = new NormalizedPosition(
            normalizer.normalizeX(e.getX()),
            normalizer.normalizeY(e.getY()));
    tpos.setY(tpos.getY() * -1);
    camera.setTargetPosition(tpos);
    repaint();
  }

  @Override
  public void refresh() {
    repaint();
  }

  // <editor-fold defaultstate="collapsed">
  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
  // </editor-fold>

  @Override
  public void displayAdded(DisplayRegistration reg) {
    if ("cam.overview.input".equals(reg.getSID())) {
      try {
        Logger.info("Display cam.overview.input available");
        this.removeMouseListener(this);
        cameraDisplay = parent.dsps.getDisplayBySID("cam.overview.input");
        cameraDisplayPanel = cameraDisplay.getDisplayPanel();
        cameraDisplayPanel.setCustomRenderer(this);
        cameraDisplayPanel.addMouseListener(this);
        this.setLayout(new BorderLayout());
        this.add(cameraDisplayPanel, BorderLayout.CENTER);
        cameraDisplay.activate();
      } catch (IllegalArgumentException e) {
        cameraDisplay = null;
        this.addMouseListener(this);
      }
    }
  }

  @Override
  public void displayRemoved(DisplayRegistration reg) {
    if ("cam.overview.input".equals(reg.getSID())) {
      cameraDisplay = null;
      this.remove(cameraDisplayPanel);
      this.addMouseListener(this);
    }
  }
}
