/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class Area extends JPanel {

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

  public Area(Point e, Color c, VideoFrame vf) {
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
        cursor = Area.this.getCursor().getType();
        Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        Area.this.setCursor(Cursor.getPredefinedCursor(cursor));
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
          move_area(x,y);
        }
        else
          resize_area(x,y);

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
          move_area(x,y);
        }
        else
          resize_area(x,y);
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        int diff = 10;
        Point p = e.getPoint();

        int xu = p.x;
        int xo = Math.abs(r.width-p.x);
        int yu = p.y;
        int yo = Math.abs(r.height-p.y);

        cursor = Area.this.getCursor().getType();
        if(xu < diff) {
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        }
        if(xo < diff) {
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
        }
        if(yu < diff) {
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        }
        if(yo < diff) {
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        }
        if(xu < diff && yu < diff)
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
        if(xu < diff && yo < diff)
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
        if(yu < diff && xo < diff)
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
        if(yo < diff && xo < diff)
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        if(xu > diff && xo > diff && yu > diff && yo > diff) {
          Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
      }
    });

    delete.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        parent.removeArea(Area.this);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        Area.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
  }

  public void update(Point e) {
    two = new Point(e.x, e.y);
    int min_x, min_y, max_x, max_y;
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

  public void move_area(int x, int y) {
    origin.x += x;
    origin.y += y;
    r.x = origin.x;
    r.y = origin.y;
    setBounds(r);
  }

  public void resize_area(int x, int y) {
    int width_n = (int) r.getWidth(), height_n = (int) r.getHeight();
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
    }
    r.setBounds(origin.x, origin.y, width_n, height_n);
    setBounds(r);
  }

  public double diagonal() {
    return Math.sqrt(r.width*r.width+r.height*r.height);
  }

  public boolean near_to_border(Point p) {
    int diff = 5;
    return(Math.abs(r.getMinX()-p.x) < diff || Math.abs(r.getMaxX()-p.x) < diff
    || Math.abs(r.getMinY() - p.y) < diff || Math.abs(r.getMaxY() - p.y) < diff);
  }
}