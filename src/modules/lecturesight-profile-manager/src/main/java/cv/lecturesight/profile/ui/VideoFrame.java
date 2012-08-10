/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cv.lecturesight.profile.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Alex
 */
public class VideoFrame extends JPanel {

  Area a;
  int marker;
  Color color;
  JLabel video;
  List<Area> ignore;
  List<Area> important;
  List<Area> person;
  List<Area> trigger;

  public VideoFrame(Color c, int m) {

    this.marker = m;
    color = c;

    ignore = new ArrayList();
    important = new ArrayList();
    person = new ArrayList();
    trigger = new ArrayList();

    try {
      BufferedImage img = ImageIO.read(new File("scene.png"));
      video = new JLabel(new ImageIcon(img));
      video.setBounds(0, 0, img.getWidth(), img.getHeight());
      video.setVisible(true);
      add(video);
      setComponentZOrder(video, 0);
      setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
    } catch (IOException ex) {
      Logger.getLogger(SceneAreasUI.class.getName()).log(Level.SEVERE, null, ex);
    }

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if(marker != 10) {
          a = new Area(e.getPoint(), color, VideoFrame.this);
          VideoFrame.this.add(a);
          VideoFrame.this.setComponentZOrder(a, 0);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if(marker != 10) {
          if(a.diagonal() > 0.5) {
            getActualList().add(a);
          }
        }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        if(marker != 10)
          VideoFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        VideoFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });

    addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        if(marker != 10)
          a.update(e.getPoint());
      }
    });
  }

  public void changeType(int m, Color c) {
    marker = m;
    color = c;
  }

  public void removeArea(Area a) {
    this.remove(a);
    repaint();
    Iterator iter = getActualList().iterator();
    while(iter.hasNext()) {
      if(iter.next() == a)
        iter.remove();
    }
  }

  public List<Area> getActualList() {
    switch(marker) {
      case 0:
        return ignore;
      case 1:
        return important;
      case 2:
        return person;
      case 3:
        return trigger;
    }
    return null;
  }

  public Dimension frame_dimension() {
    return new Dimension(getWidth(), getHeight());
  }

  void clear() {
    this.removeAll();
    add(video);
    ignore.clear();
    important.clear();
    person.clear();
    repaint();
  }


//  @Override
//  public void paintComponent(Graphics g) {
//    super.paintComponent(g);
//  }

}