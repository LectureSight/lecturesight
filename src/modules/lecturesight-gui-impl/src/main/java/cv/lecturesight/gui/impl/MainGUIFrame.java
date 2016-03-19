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
package cv.lecturesight.gui.impl;

import cv.lecturesight.gui.api.UserInterface;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.osgi.framework.BundleContext;
import org.pmw.tinylog.Logger;

public class MainGUIFrame extends javax.swing.JFrame implements ActionListener,InternalFrameListener {

  Map<JMenuItem, UserInterface> servicesMenuItems = new HashMap<JMenuItem, UserInterface>();
  Map<UserInterface, JInternalFrame> visibleUIs = new HashMap<UserInterface, JInternalFrame>();

  private int menuItems = 0;
  private BundleContext bundleContext;
  
  public MainGUIFrame(BundleContext bc) {
    bundleContext = bc;

    // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    } catch (Exception e) {
    }
    initComponents();
  }

  public void addServiceUI(UserInterface ui) {
    JMenuItem item = new JMenuItem(ui.getTitle());
    item.addActionListener(this);
    servicesMenu.insert(item, menuItems++);
    servicesMenuItems.put(item, ui);
  }

  public void removeServiceUI(UserInterface ui) {
    List<JMenuItem> itemsToRemove = new ArrayList<JMenuItem>();

    for (JMenuItem item : servicesMenuItems.keySet()) {
      if (servicesMenuItems.get(item) == ui) {
        itemsToRemove.add(item);
      }
    }

    for (JMenuItem item : itemsToRemove) {
      servicesMenuItems.remove(item);
      servicesMenu.remove(item);
    }
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    JMenuItem source = (JMenuItem)e.getSource();
    UserInterface ui = servicesMenuItems.get(source);
    if (!visibleUIs.containsKey(ui)) {
      JInternalFrame iframe = new JInternalFrame();
      iframe.setTitle(ui.getTitle());
      iframe.setBackground(Color.black);
      JPanel content = ui.getPanel();
      iframe.getContentPane().add(content);
      iframe.setSize(content.getPreferredSize());
      iframe.setResizable(true);
      iframe.setClosable(true);
      iframe.setIconifiable(true);
      iframe.setMaximizable(true);
      iframe.addInternalFrameListener(this);
      iframe.pack();
      iframe.setVisible(true);
      desktop.add(iframe);
      content.repaint();
      iframe.moveToFront();
      visibleUIs.put(ui, iframe);
    }
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    desktop = new javax.swing.JDesktopPane();
    mainMenu = new javax.swing.JMenuBar();
    servicesMenu = new javax.swing.JMenu();

    setTitle("LectureSight 0.3");

    desktop.setDesktopManager(null);

    servicesMenu.setText("Services");
    mainMenu.add(servicesMenu);

    setJMenuBar(mainMenu);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
    );

    // Quit menu item
    JMenuItem item = new JMenuItem("Quit LectureSight");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
            // Shut down
            try {
               Logger.info("Normal shutdown (from menu)");
               bundleContext.getBundle(0).stop();
            } catch (Exception e) {
               Logger.warn(e, "Abnormal shutdown");
               System.exit(1);
            }
        }

    });
    servicesMenu.addSeparator();
    servicesMenu.add(item);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JDesktopPane desktop;
  private javax.swing.JMenuBar mainMenu;
  private javax.swing.JMenu servicesMenu;
  // End of variables declaration//GEN-END:variables

  @Override
  public void internalFrameOpened(InternalFrameEvent e) {
  }

  @Override
  public void internalFrameClosing(InternalFrameEvent e) {
    JInternalFrame iframe = (JInternalFrame)e.getSource();
    UserInterface toRemove = null;
    for (UserInterface ui : visibleUIs.keySet()) {          // FIXME this is ugly
      if (visibleUIs.get(ui) == iframe) {
        toRemove = ui;
      }
    }
    if (toRemove != null) {
      visibleUIs.remove(toRemove);
    }
  }

  @Override
  public void internalFrameClosed(InternalFrameEvent e) {
  }

  @Override
  public void internalFrameIconified(InternalFrameEvent e) {
  }

  @Override
  public void internalFrameDeiconified(InternalFrameEvent e) {
  }

  @Override
  public void internalFrameActivated(InternalFrameEvent e) {
  }

  @Override
  public void internalFrameDeactivated(InternalFrameEvent e) {
  }

}
