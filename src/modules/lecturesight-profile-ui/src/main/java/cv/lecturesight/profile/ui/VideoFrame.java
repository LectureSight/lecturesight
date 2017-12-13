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
package cv.lecturesight.profile.ui;

import org.pmw.tinylog.Logger;

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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Alex
 */
public class VideoFrame extends JPanel {

  AreaComponent a;
  int marker;
  Color color;
  JLabel video;
  List<AreaComponent> ignore;
  List<AreaComponent> important;
  List<AreaComponent> person;
  List<AreaComponent> trigger;

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
      Logger.error("Error", ex);
    }

    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if(marker != 10) {
          a = new AreaComponent(e.getPoint(), color, VideoFrame.this);
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

  public void removeArea(AreaComponent a) {
    this.remove(a);
    repaint();
    Iterator iter = getActualList().iterator();
    while(iter.hasNext()) {
      if(iter.next() == a)
        iter.remove();
    }
  }

  public List<AreaComponent> getActualList() {
    switch(marker) {
      case 0:
        return ignore;
      case 1:
        return important;
      case 2:
        return person;
      case 3:
        return trigger;
      default:
        return null;
    }
  }

  public Dimension frameDimension() {
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

}
