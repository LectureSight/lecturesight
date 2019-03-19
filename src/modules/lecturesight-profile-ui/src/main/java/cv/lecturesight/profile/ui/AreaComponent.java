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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Alex
 */
public class AreaComponent extends JPanel {

  Point origin;
  Point two;
  int width;
  int height;
  Rectangle r;
  int cursor = 0;
  Point start_movement;
  JLabel delete;
  VideoFrame parent;
  int action_type = 0;

  public AreaComponent(Point e, Color c, VideoFrame vf) {
    super();
    origin = new Point(e.x, e.y);
    width = 0;
    height = 0;
    r = new Rectangle(e.x, e.y, 0, 0);

    parent = vf;

    delete = new JLabel();
    delete.setText("x");
    delete.setFont(new Font("Arial", Font.BOLD, 10));
    delete.setForeground(Color.WHITE);
    delete.setBounds(4,2,10,10);
    add(delete);

    this.setBounds(new Rectangle(origin.x, origin.y, width, height));
    this.setPreferredSize(new Dimension(width, height));
    this.setBorder(BorderFactory.createLineBorder(c));
    this.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
    this.setVisible(true);

    addMouseListener(new MouseAdapter() {

      @Override
      public void mouseEntered(MouseEvent e) {
        cursor = AreaComponent.this.getCursor().getType();
        AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        AreaComponent.this.setCursor(Cursor.getPredefinedCursor(cursor));
      }

      @Override
      public void mousePressed(MouseEvent e) {
        start_movement = e.getPoint();
        action_type = 10;

        int diff = 10;
        int xu = start_movement.x;
        int xo = Math.abs(r.width-start_movement.x);
        int yu = start_movement.y;
        int yo = Math.abs(r.height-start_movement.y);

        if(xu < diff) action_type = 0;
        if(xo < diff) action_type = 1;
        if(yu < diff) action_type = 2;
        if(yo < diff) action_type = 3;
        if(xu < diff && yu < diff) action_type = 4;
        if(xu < diff && yo < diff) action_type = 5;
        if(xo < diff && yo < diff) action_type = 6;
        if(xo < diff && yu < diff) action_type = 7;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        Point end_movement = e.getPoint();
        int x = -(start_movement.x - end_movement.x);
        int y = -(start_movement.y - end_movement.y);

        if(action_type == 10) {
          moveArea(x,y);
        }
        else
          resizeArea(x,y);

        width = r.width;
        height = r.height;
        origin = r.getLocation();
      }
    });

    addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        Point end_movement = e.getPoint();
        int x = -(start_movement.x - end_movement.x);
        int y = -(start_movement.y - end_movement.y);

        if(action_type == 10) {
          moveArea(x,y);
        }
        else
          resizeArea(x,y);
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        int diff = 10;
        Point p = e.getPoint();

        int xu = p.x;
        int xo = Math.abs(r.width-p.x);
        int yu = p.y;
        int yo = Math.abs(r.height-p.y);

        cursor = AreaComponent.this.getCursor().getType();
        if(xu < diff) {
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        }
        if(xo < diff) {
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        }
        if(yu < diff) {
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        }
        if(yo < diff) {
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        }
        if(xu < diff && yu < diff)
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        if(xu < diff && yo < diff)
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        if(yu < diff && xo < diff)
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        if(yo < diff && xo < diff)
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        if(xu > diff && xo > diff && yu > diff && yo > diff) {
          AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
      }
    });

    delete.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        parent.removeArea(AreaComponent.this);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        AreaComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  public void update(Point e) {
    two = new Point(e.x, e.y);
    int min_x;
    int min_y;
    int max_x;
    int max_y;
    if(origin.x < two.x) {
      min_x = origin.x;
      max_x = two.x;
    }
    else {
      min_x = two.x;
      max_x = origin.x;
    }
    if(origin.y < two.y) {
      min_y = origin.y;
      max_y = two.y;
    }
    else {
      min_y = two.y;
      max_y = origin.y;
    }
    width = max_x - min_x;
    height = max_y - min_y;
    r.setBounds(min_x, min_y, width, height);
    this.setPreferredSize(new Dimension(width, height));
    this.setBounds(r);
  }

  public void moveArea(int x, int y) {
    origin.x += x;
    origin.y += y;
    r.x = origin.x;
    r.y = origin.y;
    setBounds(r);
  }

  public void resizeArea(int x, int y) {
    int width_n = (int) r.getWidth();
    int height_n = (int) r.getHeight();
    origin = r.getLocation();
    switch(action_type) {
      case 0:
        origin.x += x;
        width -= x;
        width_n = width;
        break;
      case 1:
        width_n = width + x;
        break;
      case 2:
        origin.y += y;
        height -= y;
        height_n = height;
        break;
      case 3:
        height_n = height + y;
        break;
      case 4:
        origin.x += x;
        origin.y += y;
        height -= y;
        height_n = height;
        width -= x;
        width_n = width;
        break;
      case 5:
        origin.x +=x;
        width -= x;
        width_n = width;
        height_n = height+y;
        break;
      case 6:
        width_n = width+x;
        height_n = height+y;
        break;
      case 7:
        origin.y += y;
        height -= y;
        height_n = height;
        width_n = width+x;
        break;
      default:
        break;
    }
    r.setBounds(origin.x, origin.y, width_n, height_n);
    setBounds(r);
  }

  public double diagonal() {
    return Math.sqrt(r.width*r.width+r.height*r.height);
  }

  public boolean nearToBorder(Point p) {
    int diff = 5;
    return(Math.abs(r.getMinX()-p.x) < diff || Math.abs(r.getMaxX()-p.x) < diff
    || Math.abs(r.getMinY() - p.y) < diff || Math.abs(r.getMaxY() - p.y) < diff);
  }
}
