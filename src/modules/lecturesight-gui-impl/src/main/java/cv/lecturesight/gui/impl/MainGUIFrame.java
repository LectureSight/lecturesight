package cv.lecturesight.gui.impl;

import cv.lecturesight.display.DisplayRegistration;
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

  public void addController(UserInterface ui, String title) {
  }

  public void removeController(UserInterface ui) {
    
  }
  
  public void addDisplay(DisplayRegistration reg) {
    
  }
  
  public void removeDisplay(DisplayRegistration reg) {
    
  }

  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        desktop = new javax.swing.JDesktopPane();
        mainMenu = new javax.swing.JMenuBar();
        profileMenu = new javax.swing.JMenu();
        controllersMenu = new javax.swing.JMenu();
        displaysMenu = new javax.swing.JMenu();

        setTitle("LectureSight 0.3-SNAPSHOT");

        desktop.setDesktopManager(null);

        profileMenu.setText("Profile");
        mainMenu.add(profileMenu);

        controllersMenu.setText("Controllers");
        mainMenu.add(controllersMenu);

        displaysMenu.setText("Displays");
        mainMenu.add(displaysMenu);

        setJMenuBar(mainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu controllersMenu;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JMenu displaysMenu;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenu profileMenu;
    // End of variables declaration//GEN-END:variables
}
