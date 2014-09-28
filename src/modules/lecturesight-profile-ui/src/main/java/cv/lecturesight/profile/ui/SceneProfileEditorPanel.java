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

import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayPanel;
import cv.lecturesight.profile.api.ProfileSerializerException;
import cv.lecturesight.profile.api.Zone;
import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileSerializer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author wulff
 */
public class SceneProfileEditorPanel extends javax.swing.JPanel implements CustomRenderer {

  final static int NEW_AREA_SIZE = 5;
  private SceneProfileUI parent;
  private Display cameraDisplay;
  private DisplayPanel cameraDisplayPanel;
  SceneProfile profile;
  private ObjectSelection selection = null;
  private EditorMouseHandler mouseHandler;
  private Dimension imageDim;

  enum DraggingType {

    NONE, WHOLE, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
  }

  // Stuff for drawing
  private final static Font font = new Font("Monospaced", Font.PLAIN, 10);
  private Color red_solid = new Color(255, 80, 80, 255);
  private Color red_transparent = new Color(255, 0, 0, 100);
  private Color green_solid = new Color(0, 255, 0, 255);
  private Color green_transparent = new Color(0, 255, 0, 100);
  private Color yellow_solid = new Color(255, 255, 0, 255);
  private Color yellow_transparent = new Color(255, 255, 0, 180);
  private Color blue_solid = new Color(0, 0, 255, 255);
  private Color blue_transparent = new Color(0, 0, 255, 180);

  /**
   * Creates new form SceneProfileEditorPanel
   */
  public SceneProfileEditorPanel() {
    initComponents();
  }

  public SceneProfileEditorPanel(SceneProfileUI parent) {
    this.parent = parent;
    initComponents();
    cameraDisplay = parent.dsps.getDisplayBySID("cam.overview.input");
    imageDim = cameraDisplay.getSize();
    cameraDisplayPanel = cameraDisplay.getDisplayPanel();
    cameraDisplayPanel.setCustomRenderer(this);
    mouseHandler = new EditorMouseHandler();
    cameraDisplayPanel.addMouseListener(mouseHandler);
    cameraDisplayPanel.addMouseMotionListener(mouseHandler);
    cameraDisplayHolder.setLayout(new BorderLayout());
    cameraDisplayHolder.add(cameraDisplayPanel, BorderLayout.CENTER);
    cameraDisplay.activate();
  }

  void removeCameraDisplay() {
    cameraDisplay.deactivate();
    cameraDisplayHolder.remove(cameraDisplayPanel);
  }

  boolean currentProfileIsDefault() {
    return profile.name.equals("default");
  }

  /**
   * Resets the editor and loads what is in <code>profile</code>.
   *
   * @param profile
   */
  void reset(SceneProfile profile) {
    this.profile = profile;
    selection = null;
    if (currentProfileIsDefault()) {
      defaultProfileNotification.setVisible(true);
      saveButton.setEnabled(false);
    } else {
      defaultProfileNotification.setVisible(false);
      saveButton.setEnabled(true);
    }
  }

  void resetSelection() {
    selection = null;
  }

  void resetToolSelection() {
    toolButtonPointer.setSelected(true);
  }

  void setProfileList(List<SceneProfile> profiles) {
    // create new model for combo box and set it
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    int num = 0;
    for (SceneProfile p : profiles) {
      model.addElement(new SceneProfileListItem(p));
      num++;
    }
    parent.log.debug(num + " profiles available");
    profileChooser.setModel(model);

    // make sure that currently edited profile is still in list, load currently
    // active profile if it was deleted
    SceneProfile next_profile = parent.spm.getActiveProfile();
    for (SceneProfile p : profiles) {
      if (profile == p) {
        setProfileSelection(profile);
        return;
      }
    }
    profile = next_profile;
    setProfileSelection(profile);  
  }
  
  /** make sure profile selector is in correct state
   * 
   * @param profile 
   */
  void setProfileSelection(SceneProfile profile) {
    ComboBoxModel model = profileChooser.getModel();
    for (int i = 0; i < model.getSize(); i++) {
      if (((SceneProfileListItem)model.getElementAt(i)).profile.name.equals(profile.name)) {
        profileChooser.setSelectedIndex(i);
      }
    }
  }

  Zone addZone(String name, Zone.Type type, int x, int y, int w, int h) {
    Zone new_zone = new Zone(name, type, x, y, w, h);
    profile.putZone(new_zone);
    return new_zone;
  }

  void showErrorDialog(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Augments the image from the DisplayService with rendering of the areas from
   * the scene profile.
   *
   * @param g
   */
  @Override
  public void render(Graphics g) {
    for (Zone z : profile.zones) {
      switch (z.getType()) {
        case IGNORE:
          drawZone(g, z, red_solid, red_transparent, (selection != null && z == selection.zone));
          break;
        case TRACK:
          drawZone(g, z, green_solid, green_transparent, (selection != null && z == selection.zone));
          break;
        case TRIGGER:
          drawTriggerZone(g, z, yellow_transparent, (selection != null && z == selection.zone));
          break;
        case PERSON:
          drawTriggerZone(g, z, blue_transparent, (selection != null && z == selection.zone));
          break;
      }
    }
  }

  /**
   * Draws the representation of an <code>Area</code> with the given Graphics
   * context.
   *
   * @param g
   * @param a
   * @param borderColor
   * @param fillColor
   * @param selected
   */
  private void drawZone(Graphics g, Zone a, Color borderColor, Color fillColor, boolean selected) {
    // draw area
    g.setColor(fillColor);
    g.fillRect(a.x, a.y, a.width, a.height);

    // draw area name if existing
    if (!a.name.trim().isEmpty()) {
      g.setColor(borderColor);
      g.setFont(font);
      g.drawString(a.name, a.x + 5, a.y + 10);
    }

    // draw handles if selected
    if (selected) {
      g.setColor(fillColor);
      g.fillRect(a.x - 3, a.y - 3, 5, 5);
      g.fillRect(a.x + a.width - 3, a.y - 3, 5, 5);
      g.fillRect(a.x - 3, a.y + a.height - 3, 5, 5);
      g.fillRect(a.x + a.width - 3, a.y + a.height - 3, 5, 5);
      g.setColor(borderColor);
      g.drawRect(a.x - 3, a.y - 3, 5, 5);
      g.drawRect(a.x + a.width - 3, a.y - 3, 5, 5);
      g.drawRect(a.x - 3, a.y + a.height - 3, 5, 5);
      g.drawRect(a.x + a.width - 3, a.y + a.height - 3, 5, 5);
    }
  }

  private void drawTriggerZone(Graphics g, Zone a, Color borderColor, boolean selected) {
    // draw area
    g.setColor(borderColor);
    g.drawRect(a.x, a.y, a.width, a.height);

    // draw area name if existing
    if (!a.name.trim().isEmpty()) {
      g.setColor(borderColor);
      g.setFont(font);
      g.drawString(a.name, a.x + 5, a.y + 10);
    }

    // draw handles if selected
    if (selected) {
      g.setColor(borderColor);
      g.drawRect(a.x - 3, a.y - 3, 5, 5);
      g.drawRect(a.x + a.width - 3, a.y - 3, 5, 5);
      g.drawRect(a.x - 3, a.y + a.height - 3, 5, 5);
      g.drawRect(a.x + a.width - 3, a.y + a.height - 3, 5, 5);
    }
  }

  /**
   * Returns true if <code>p</code> is inside a given rectangle
   *
   * @param p
   * @param rx
   * @param ry
   * @param rw
   * @param rh
   * @return
   */
  private boolean isInside(Point p, int rx, int ry, int rw, int rh) {
    return p.x >= rx && p.x <= rx + rw && p.y >= ry && p.y <= ry + rh;
  }

  /**
   * Inner class that handles all the mouse interactivity of the editor panel.
   *
   */
  private class EditorMouseHandler extends MouseAdapter {

    private Point lastPos = new Point(0, 0);

    @Override
    public void mousePressed(MouseEvent e) {
      Point pos = cameraDisplayPanel.getPositionInImage(e.getPoint());
      lastPos = pos;

      // do we have to create a new area?
      if (toolButtonIgnore.isSelected()) {
        selection = new ObjectSelection(
                addZone("ignore", Zone.Type.IGNORE, pos.x, pos.y, NEW_AREA_SIZE, NEW_AREA_SIZE),
                DraggingType.DOWN_RIGHT);
        resetToolSelection();
        return;
      } else if (toolButtonTracking.isSelected()) {
        selection = new ObjectSelection(
                addZone("track", Zone.Type.TRACK, pos.x, pos.y, NEW_AREA_SIZE, NEW_AREA_SIZE),
                DraggingType.DOWN_RIGHT);
        resetToolSelection();
        return;
      } else if (toolButtonTrigger.isSelected()) {
        selection = new ObjectSelection(
                addZone("trigger", Zone.Type.TRIGGER, pos.x, pos.y, NEW_AREA_SIZE, NEW_AREA_SIZE),
                DraggingType.DOWN_RIGHT);
        resetToolSelection();
        return;
      } else if (toolButtonMeasure.isSelected()) {
        selection = new ObjectSelection(
                addZone("person", Zone.Type.PERSON, pos.x, pos.y, NEW_AREA_SIZE, NEW_AREA_SIZE),
                DraggingType.DOWN_RIGHT);
        resetToolSelection();
        return;
      }

      // or are we dragging area or handle?
      if (toolButtonPointer.isSelected() && selection != null) {
        int ax = selection.zone.x;
        int ay = selection.zone.y;
        int aw = selection.zone.width;
        int ah = selection.zone.height;

        if (isInside(pos, ax - 3, ay - 3, 5, 5)) {                  // upper left handle
          selection.dragging = DraggingType.UP_LEFT;
          setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
          return;
        } else if (isInside(pos, ax + aw - 3, ay - 3, 5, 5)) {      // upper right handle
          selection.dragging = DraggingType.UP_RIGHT;
          setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
          return;
        } else if (isInside(pos, ax - 3, ay + ah - 3, 5, 5)) {      // lower left handle
          selection.dragging = DraggingType.DOWN_LEFT;
          setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
          return;
        } else if (isInside(pos, ax + aw - 3, ay + ah - 3, 5, 5)) { // lower right handle
          selection.dragging = DraggingType.DOWN_RIGHT;
          setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
          return;
        } else if (isInside(pos, ax, ay, aw, ah)) {                 // area itself
          selection.dragging = DraggingType.WHOLE;
          setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          return;
        }
      }

      // or was a zone selected?
      // test for trigger zones first since they are most likely inside tacking zones
      for (Zone z : profile.zones) {
        if (z.getType().equals(Zone.Type.TRIGGER)
                && isInside(pos, z.x, z.y, z.width, z.height)) {
          if (selection == null || selection.zone != z) {
            selection = new ObjectSelection(z, DraggingType.WHOLE);
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          }
          return;
        }
      }
      for (Zone z : profile.zones) {
        if (!z.getType().equals(Zone.Type.TRIGGER)
                && isInside(pos, z.x, z.y, z.width, z.height)) {
          if (selection == null || selection.zone != z) {
            selection = new ObjectSelection(z, DraggingType.WHOLE);
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          }
          return;
        }
      }

      // nothing was hit, so we reset the selection
      resetSelection();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      // reset dragging mode and mouse cursor to normal
      if (selection != null) {
        selection.dragging = DraggingType.NONE;
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      Point pos = cameraDisplayPanel.getPositionInImage(e.getPoint());

      int dx = pos.x - lastPos.x;
      int dy = pos.y - lastPos.y;

      int new_x, new_y, new_width, new_height;
      if (selection != null) {
        switch (selection.dragging) {

          case WHOLE:
            new_x = selection.zone.x + dx;
            new_y = selection.zone.y + dy;

            if (new_x >= 0 && new_y >= 0
                    && new_x + selection.zone.width <= imageDim.width
                    && new_y + selection.zone.height <= imageDim.height) {
              selection.zone.x = new_x;
              selection.zone.y = new_y;
            }
            break;

          case UP_LEFT:
            new_x = selection.zone.x + dx;
            new_y = selection.zone.y + dy;
            new_width = selection.zone.width - dx;
            new_height = selection.zone.height - dy;

            if (new_x >= 0 && new_y >= 0
                    && new_width > 3 && new_height > 3) {
              selection.zone.x = new_x;
              selection.zone.y = new_y;
              selection.zone.width = new_width;
              selection.zone.height = new_height;
            }
            break;

          case UP_RIGHT:
            new_y = selection.zone.y + dy;
            new_width = selection.zone.width + dx;
            new_height = selection.zone.height - dy;

            if (new_y >= 0
                    && new_width > 3 && new_height > 3
                    && selection.zone.x + new_width <= imageDim.width) {
              selection.zone.y = new_y;
              selection.zone.width = new_width;
              selection.zone.height = new_height;
            }
            break;

          case DOWN_LEFT:
            new_x = selection.zone.x + dx;
            new_width = selection.zone.width - dx;
            new_height = selection.zone.height + dy;

            if (new_x >= 0
                    && new_width > 3 && new_height > 3
                    && selection.zone.y + new_height <= imageDim.height) {
              selection.zone.x = new_x;
              selection.zone.width = new_width;
              selection.zone.height = new_height;
            }
            break;

          case DOWN_RIGHT:
            new_width = selection.zone.width + dx;
            new_height = selection.zone.height + dy;

            if (new_width > 3 && new_height > 3
                    && selection.zone.x + new_width <= imageDim.width
                    && selection.zone.y + new_height <= imageDim.height) {
              selection.zone.width = new_width;
              selection.zone.height = new_height;
            }
            break;
        }
      }
      lastPos = pos;
    }
  }

  /**
   * inner class that implements the selection data model
   *
   */
  private class ObjectSelection {

    DraggingType dragging;
    Zone zone;

    public ObjectSelection(Zone area, DraggingType type) {
      this.dragging = type;
      this.zone = area;
    }
  }

  private class SceneProfileListItem {

    String name;
    SceneProfile profile;

    public SceneProfileListItem(SceneProfile profile) {
      this.name = profile.name;
      this.profile = profile;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    typeButtonGroup = new javax.swing.ButtonGroup();
    toolBar = new javax.swing.JToolBar();
    ProfileChooserLabel = new javax.swing.JLabel();
    profileChooser = new javax.swing.JComboBox();
    newProfileButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();
    jSeparator1 = new javax.swing.JToolBar.Separator();
    propertiesButton = new javax.swing.JButton();
    defaultProfileNotification = new javax.swing.JLabel();
    cameraDisplayHolder = new javax.swing.JPanel();
    typeButtonContainer = new javax.swing.JPanel();
    toolButtonIgnore = new javax.swing.JToggleButton();
    toolButtonTracking = new javax.swing.JToggleButton();
    toolButtonTrigger = new javax.swing.JToggleButton();
    toolButtonMeasure = new javax.swing.JToggleButton();
    toolButtonPointer = new javax.swing.JToggleButton();
    editButton = new javax.swing.JButton();
    deleteButton = new javax.swing.JButton();

    toolBar.setRollover(true);
    toolBar.setMargin(new java.awt.Insets(2, 2, 2, 2));

    ProfileChooserLabel.setText("Profile:");
    toolBar.add(ProfileChooserLabel);

    profileChooser.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        profileChooserActionPerformed(evt);
      }
    });
    toolBar.add(profileChooser);

    newProfileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/map--plus.png"))); // NOI18N
    newProfileButton.setFocusable(false);
    newProfileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    newProfileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    newProfileButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        newProfileButtonActionPerformed(evt);
      }
    });
    toolBar.add(newProfileButton);

    saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/disk.png"))); // NOI18N
    saveButton.setToolTipText("Save current profile");
    saveButton.setFocusable(false);
    saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveButtonActionPerformed(evt);
      }
    });
    toolBar.add(saveButton);
    toolBar.add(jSeparator1);

    propertiesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/property.png"))); // NOI18N
    propertiesButton.setToolTipText("Edit profile properties (disbaled, not stale yet)");
    propertiesButton.setEnabled(false);
    propertiesButton.setFocusable(false);
    propertiesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    propertiesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    propertiesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        propertiesButtonActionPerformed(evt);
      }
    });
    toolBar.add(propertiesButton);

    defaultProfileNotification.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    defaultProfileNotification.setIcon(new javax.swing.ImageIcon(getClass().getResource("/exclamation-small.png"))); // NOI18N
    defaultProfileNotification.setText("System default profile cannot be saved!");
    toolBar.add(defaultProfileNotification);

    cameraDisplayHolder.setBackground(new java.awt.Color(1, 1, 1));

    javax.swing.GroupLayout cameraDisplayHolderLayout = new javax.swing.GroupLayout(cameraDisplayHolder);
    cameraDisplayHolder.setLayout(cameraDisplayHolderLayout);
    cameraDisplayHolderLayout.setHorizontalGroup(
      cameraDisplayHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );
    cameraDisplayHolderLayout.setVerticalGroup(
      cameraDisplayHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );

    typeButtonGroup.add(toolButtonIgnore);
    toolButtonIgnore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cross-circle-frame.png"))); // NOI18N
    toolButtonIgnore.setToolTipText("Create Ignore Zone");
    toolButtonIgnore.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toolButtonIgnoreActionPerformed(evt);
      }
    });

    typeButtonGroup.add(toolButtonTracking);
    toolButtonTracking.setIcon(new javax.swing.ImageIcon(getClass().getResource("/plus-circle-frame.png"))); // NOI18N
    toolButtonTracking.setToolTipText("Create Tracking Zone");
    toolButtonTracking.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toolButtonTrackingActionPerformed(evt);
      }
    });

    typeButtonGroup.add(toolButtonTrigger);
    toolButtonTrigger.setIcon(new javax.swing.ImageIcon(getClass().getResource("/switch.png"))); // NOI18N
    toolButtonTrigger.setToolTipText("Create Trigger Zone");
    toolButtonTrigger.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toolButtonTriggerActionPerformed(evt);
      }
    });

    typeButtonGroup.add(toolButtonMeasure);
    toolButtonMeasure.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ruler-triangle.png"))); // NOI18N
    toolButtonMeasure.setToolTipText("Create Measure Zone");
    toolButtonMeasure.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toolButtonMeasureActionPerformed(evt);
      }
    });

    typeButtonGroup.add(toolButtonPointer);
    toolButtonPointer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cursor.png"))); // NOI18N
    toolButtonPointer.setSelected(true);
    toolButtonPointer.setToolTipText("Pointer Tool");

    editButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tag--pencil.png"))); // NOI18N
    editButton.setToolTipText("Edit Zone Name");
    editButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        editButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout typeButtonContainerLayout = new javax.swing.GroupLayout(typeButtonContainer);
    typeButtonContainer.setLayout(typeButtonContainerLayout);
    typeButtonContainerLayout.setHorizontalGroup(
      typeButtonContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(typeButtonContainerLayout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(typeButtonContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(toolButtonPointer, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(toolButtonIgnore, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(toolButtonTrigger, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(toolButtonMeasure, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(toolButtonTracking, javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(editButton, javax.swing.GroupLayout.Alignment.TRAILING))
        .addContainerGap())
    );
    typeButtonContainerLayout.setVerticalGroup(
      typeButtonContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(typeButtonContainerLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(toolButtonPointer)
        .addGap(18, 18, 18)
        .addComponent(toolButtonIgnore)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolButtonTracking)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolButtonTrigger)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolButtonMeasure)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 300, Short.MAX_VALUE)
        .addComponent(editButton))
    );

    deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bin-metal.png"))); // NOI18N
    deleteButton.setToolTipText("Delete Zone");
    deleteButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        deleteButtonActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 880, Short.MAX_VALUE)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(cameraDisplayHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(typeButtonContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(layout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addComponent(deleteButton)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(typeButtonContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(deleteButton))
          .addComponent(cameraDisplayHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
    if (!currentProfileIsDefault()) {
      parent.spm.setActiveProfile(profile);
      parent.spm.saveProfile(profile);
    }
  }//GEN-LAST:event_saveButtonActionPerformed

  private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    if (selection != null) {
      profile.removeZone(selection.zone);
      resetSelection();
    }
  }//GEN-LAST:event_deleteButtonActionPerformed

  private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
    if (selection != null) {
      String new_name = JOptionPane.showInputDialog(this, "Enter name for area: ", "Edit Area", 1);
      if (new_name != null) {
        selection.zone.name = new_name;
      }
    }
  }//GEN-LAST:event_editButtonActionPerformed

  private void toolButtonIgnoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolButtonIgnoreActionPerformed
    resetSelection();
  }//GEN-LAST:event_toolButtonIgnoreActionPerformed

  private void toolButtonTrackingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolButtonTrackingActionPerformed
    resetSelection();
  }//GEN-LAST:event_toolButtonTrackingActionPerformed

  private void toolButtonTriggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolButtonTriggerActionPerformed
    resetSelection();
  }//GEN-LAST:event_toolButtonTriggerActionPerformed

  private void toolButtonMeasureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolButtonMeasureActionPerformed
    resetSelection();
  }//GEN-LAST:event_toolButtonMeasureActionPerformed

  private void profileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileChooserActionPerformed
    SceneProfile p = ((SceneProfileListItem) profileChooser.getSelectedItem()).profile;
    reset(p);
    parent.spm.setActiveProfile(p);
  }//GEN-LAST:event_profileChooserActionPerformed

  private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
    ProfilePreferencesDialog dialog = new ProfilePreferencesDialog(null, profile);
    dialog.setVisible(true);
  }//GEN-LAST:event_propertiesButtonActionPerformed

  private String sanitizeFilename(String in) {
    return in.toLowerCase()
            .trim()
            .replaceAll("\\s", "_")
            .replaceAll("\\.scn$", "") + ".scn";
  }

  private void newProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProfileButtonActionPerformed
    File profileDir = new File(System.getProperty("user.dir") + File.separator + "profiles");   // TODO find out where artifact installer is looking at

    String name = JOptionPane.showInputDialog(this, "Enter a display name for new profile: ", "Create Profile", 1);
    if (name.trim().isEmpty()) {
      return;
    }
    String filename = sanitizeFilename(JOptionPane.showInputDialog(this, "Enter a file name for new profile: ", "Create Profile", 1));

    if (filename.equals(".scn")) {
      return;
    }

    File file = new File(profileDir.getAbsolutePath() + File.separator + filename);
    if (file.exists()) {
      if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null,
              "A file with the name " + filename + " already exists. Do you want to replace it?",
              "Overwrite existing file?", JOptionPane.YES_NO_OPTION)) {
        return;
      }
    }

    try {
      FileOutputStream os = new FileOutputStream(file);
      parent.log.info("Writing new scene profile to " + file.getAbsolutePath());
      SceneProfile newProfile = new SceneProfile(name, "");
      SceneProfileSerializer.serialize(newProfile, os);
      os.close();
      
    } catch (IOException e) {
      String msg = "Error while writing profile. " + e.getMessage();
      parent.log.error(msg, e);
      showErrorDialog(msg);
      
    } catch (ProfileSerializerException e) {
      String msg = "Error while serializing profile. " + e.getMessage();
      parent.log.error(msg, e);
      showErrorDialog(msg);
    }
  }//GEN-LAST:event_newProfileButtonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel ProfileChooserLabel;
  private javax.swing.JPanel cameraDisplayHolder;
  private javax.swing.JLabel defaultProfileNotification;
  private javax.swing.JButton deleteButton;
  private javax.swing.JButton editButton;
  private javax.swing.JToolBar.Separator jSeparator1;
  private javax.swing.JButton newProfileButton;
  private javax.swing.JComboBox profileChooser;
  private javax.swing.JButton propertiesButton;
  private javax.swing.JButton saveButton;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JToggleButton toolButtonIgnore;
  private javax.swing.JToggleButton toolButtonMeasure;
  private javax.swing.JToggleButton toolButtonPointer;
  private javax.swing.JToggleButton toolButtonTracking;
  private javax.swing.JToggleButton toolButtonTrigger;
  private javax.swing.JPanel typeButtonContainer;
  private javax.swing.ButtonGroup typeButtonGroup;
  // End of variables declaration//GEN-END:variables

}
