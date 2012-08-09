package cv.lecturesight.ptz.steering.impl;

import cv.lecturesight.ptz.steering.impl.CameraMovementModel;
import cv.lecturesight.util.geometry.CoordinatesNormalization;
import cv.lecturesight.util.geometry.NormalizedPosition;
import cv.lecturesight.util.geometry.Position;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

public class CameraControlPanel extends JPanel implements MouseListener {
  
  private Color targetColor = Color.yellow;
  private Color positionColor = Color.cyan;
  private Font font = new Font("Monospaced", Font.PLAIN, 10);
  private CoordinatesNormalization normalizer = new CoordinatesNormalization(400,400);
  private CameraMovementModel model;
  
  public CameraControlPanel(CameraMovementModel model) {
    this.model = model;
    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    this.addMouseListener(this);
  }
  
  @Override
  public void paint(Graphics g) {
    updateSize();
    int rootX = getWidth() / 2;
    int rootY = getHeight() / 2;
    
    g.setFont(font);
    
    // clear panel
    g.setColor(Color.black);
    g.fillRect(0, 0, getWidth(), getHeight());
    
    // coordinate cross
    g.setColor(Color.green);
    g.drawLine(1, rootY, getWidth()-2, rootY);
    
    g.drawLine(1, rootY-3, 1, rootY + 3);
    g.drawLine(rootX/2, rootY-3, rootX/2, rootY + 3);
    g.drawString("-1.0", 2, rootY - 2);
    g.drawLine(getWidth()-2, rootY-3, getWidth()-2, rootY + 3);
    g.drawLine(rootX + (rootX/2), rootY-3, rootX + (rootX/2), rootY + 3);
    g.drawString("1.0", getWidth() - 20, rootY + 11);
    
    g.drawLine(rootX, 1, rootX, getHeight()-2);
    
    g.drawLine(rootX - 3, 1, rootX + 3, 1);
    g.drawLine(rootX - 3, rootY/2, rootX + 3, rootY/2);
    g.drawString("-1.0", rootX + 2, 12);
    g.drawLine(rootX - 3, getHeight()-2, rootX + 3, getHeight()-2);
    g.drawLine(rootX - 3, rootY + (rootY/2), rootX + 3, rootY + (rootY/2));
    g.drawString("1.0", rootX - 23, getHeight() - 3);
    
    Position tpos = normalizer.fromNormalized(model.getTargetPosition());
    Position apos = normalizer.fromNormalized(model.getActualPosition());
    g.setColor(Color.pink);
    g.drawLine(tpos.getX(), tpos.getY(), apos.getX(), apos.getY());
    drawCursor(g, tpos.getX(), tpos.getY(), targetColor);
    g.setColor(targetColor);
    g.drawString(model.getTargetPosition().toString(), tpos.getX() + 5, tpos.getY() - 1);
    drawCursor(g, apos.getX(), apos.getY(), positionColor);
    g.setColor(positionColor);
    g.drawString(model.getActualPosition().toString(), apos.getX() + 5, apos.getY() + 10);
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

  private void updateSize() {
    normalizer.setMaxX(getWidth());
    normalizer.setMaxY(getHeight());
  }
  
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(400,400);
  }
  
  @Override
  public void mouseClicked(MouseEvent e) {
    NormalizedPosition tpos = new NormalizedPosition(
            normalizer.normalizeX(e.getX()),
            normalizer.normalizeY(e.getY()));
    model.setTargetPosition(tpos);
    repaint();
  }

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
}
