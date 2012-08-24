package cv.lecturesight.gui.impl;

import cv.lecturesight.gui.api.UserInterface;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;

public class MainGUIFrame extends javax.swing.JFrame {

  Map<UserInterface, Component> registrations = new HashMap<UserInterface, Component>();

  public MainGUIFrame() {
    // set operating system look-and-feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    initComponents();
  }

  public void addUI(UserInterface ui, String title) {
    tabs.add(title, ui.getPanel());
  }

  public void removeUI(UserInterface ui) {
    if (registrations.containsKey(ui)) {
      tabs.remove(registrations.get(ui));
    }
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    tabs = new javax.swing.JTabbedPane();
    menu = new javax.swing.JMenuBar();
    jMenu1 = new javax.swing.JMenu();
    jMenu2 = new javax.swing.JMenu();

    setTitle("LectureSight 0.3-SNAPSHOT");

    jMenu1.setText("File");
    menu.add(jMenu1);

    jMenu2.setText("Edit");
    menu.add(jMenu2);

    setJMenuBar(menu);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenu jMenu1;
  private javax.swing.JMenu jMenu2;
  private javax.swing.JMenuBar menu;
  private javax.swing.JTabbedPane tabs;
  // End of variables declaration//GEN-END:variables
}
