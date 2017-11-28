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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/**
 *
 * @author Alex
 */
public class SceneAreasUI extends JPanel{

  VideoFrame video_frame;
  LayoutManager manager;
  JButton jButton1;
  JButton jButton2;
  JButton jButton3;
  JButton jButton4;
  JButton jButton5;
  int marker = 0;
  JPanel button_container;
  JPanel bottom;
  JSlider slider;
  JLabel slider_value;
  Color c1;
  Color c2;
  Color c3;
  Color c4;

  public SceneAreasUI() {

    setBackground(new Color(255,255,255));

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    this.setMinimumSize(new Dimension(675, 500));

    c1 = Color.RED;
    c2 = Color.GREEN;
    c3 = Color.BLUE;
    c4 = Color.YELLOW;

    video_frame = new VideoFrame(c1,marker);
    video_frame.setBounds(0,0,480,270);
    video_frame.setVisible(true);

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.VERTICAL;
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.insets = new Insets(10, 15, 0, 0);
    this.add(video_frame, c);

    button_container = new JPanel();
    button_container.setLayout(new FlowLayout(FlowLayout.LEFT));
    button_container.setVisible(true);

    jButton1 = new AreaButton(c1);
    jButton1.setText("ignore area");
    jButton1.setToolTipText("You can choose to ignore areas in the scene, which are not important for the recording of the lecture.");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        video_frame.changeType(0, c1);
        marker = 10;
      }
    });

    button_container.add(jButton1);

    jButton2 = new AreaButton(c2);
    jButton2.setText("important area");
    jButton2.setToolTipText("Use this control element in order to highligh areas of high importance for the lecture recording");
    jButton2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        video_frame.changeType(1, c2);
      }
    });

    button_container.add(jButton2);

    jButton3 = new AreaButton(c3);
    jButton3.setText("select person");
    jButton3.setToolTipText("You can select a person, if visible, in order to give LectureSight a rough estimation of person dimensions in the given setting");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        video_frame.changeType(2, c3);
      }
    });

    button_container.add(jButton3);

    jButton4 = new AreaButton(c4);
    jButton4.setText("trigger area");
    jButton4.setToolTipText("select a triggering area");
    jButton4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        video_frame.changeType(3, c4);
      }
    });

    button_container.add(jButton4);
    jButton1.setPreferredSize(new Dimension(140, 30));
    jButton2.setPreferredSize(new Dimension(140, 30));
    jButton3.setPreferredSize(new Dimension(140, 30));
    jButton4.setPreferredSize(new Dimension(140, 30));


    jButton5 = new JButton();
    jButton5.setText("reset");
    jButton5.setToolTipText("Reset actual settings");
    jButton5.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        video_frame.clear();
        marker = 10;
      }
    });

    //    JPanel reset_panel = new JPanel();
    //    reset_panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    //    reset_panel.setPreferredSize(new Dimension(100, video_frame.getHeight()));
    //
    //    reset_panel.add(jButton5);

    Component cb = Box.createGlue();
    cb.setPreferredSize(new Dimension(100, 95));

    button_container.add(cb);
    button_container.add(jButton5);

    button_container.setPreferredSize(new Dimension(150, video_frame.getHeight()));

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.NORTHWEST;
    add(button_container, c);

    bottom = new JPanel();
    bottom.setPreferredSize(new Dimension(600, 180));
    bottom.setVisible(true);

    bottom.setLayout(new GridBagLayout());
    GridBagConstraints c_1 = new GridBagConstraints();

    JLabel camera_header = new JLabel();
    camera_header.setText("Camera options");
    camera_header.setFont(new Font("Arial", 0, 16));
    camera_header.setVisible(true);
    camera_header.setPreferredSize(new Dimension(200, 35));
    camera_header.setVerticalAlignment(SwingConstants.CENTER);

    c_1.gridx = 0;
    c_1.gridy = 0;

    bottom.add(camera_header, c_1);

    JPanel slider_panel = new JPanel();
    slider_panel.setLayout(new FlowLayout(FlowLayout.CENTER));
    slider_panel.setPreferredSize(new Dimension(200, 110));
    slider_panel.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,10)));

    JLabel slider_label = new JLabel();
    slider_label.setPreferredSize(new Dimension(150, 30));
    slider_label.setHorizontalAlignment(SwingConstants.CENTER);
    slider_label.setText("camera velocity");
    slider_label.setFont(new Font("Arial", 0, 12));
    slider_label.setVisible(true);

    slider_panel.add(slider_label);

    JPanel slider_box = new JPanel();

    slider = new JSlider();
    slider.setMinimum(0);
    slider.setMaximum(16);
    slider.setVisible(true);
    slider.setPreferredSize(new Dimension(140, 30));

    JLabel slider_min = new JLabel();
    slider_min.setText(Integer.toString(slider.getMinimum()));
    slider_min.setFont(new Font("Arial", Font.ITALIC, 10));
    slider_min.setHorizontalAlignment(SwingConstants.CENTER);
    slider_min.setPreferredSize(new Dimension(15, 30));

    slider_box.add(slider_min);
    slider_box.add(slider);

    JLabel slider_max = new JLabel();
    slider_max.setText(Integer.toString(slider.getMaximum()));
    slider_max.setFont(new Font("Arial", Font.ITALIC, 10));
    slider_max.setHorizontalAlignment(SwingConstants.CENTER);
    slider_max.setPreferredSize(new Dimension(15, 30));

    slider_box.add(slider_max);

    slider_panel.add(slider_box);

    slider_value = new JLabel();
    slider_value.setText(Integer.toString(slider.getValue())+".0");
    slider_value.setSize(new Dimension(50, 14));
    slider_value.setForeground(Color.MAGENTA);
    slider_value.setBorder(BorderFactory.createLineBorder(Color.black));
    slider_value.setFont(new Font("Arial", Font.ITALIC, 10));
    slider_value.setVerticalAlignment(SwingConstants.TOP);
    slider_value.setVisible(false);

    slider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        //slider_value.setText(String.valueOf(((JSlider) evt.getSource()).getValue())+".0");
      }
    });

    slider_panel.add(slider_value);

    c_1.gridx = 0;
    c_1.gridy = 1;
    c_1.gridheight = 2;

    bottom.add(slider_panel, c_1);

    Component b1 = Box.createGlue();
    b1.setBackground(c1);
    c_1.gridx = 0;
    c_1.gridy = 2;
    c_1.weighty = 1.0;

    bottom.add(b1, c_1);

    Component b = Box.createHorizontalStrut(400);
    c_1.gridx = 1;
    c_1.gridy = 0;
    c_1.gridheight = 3;
    c_1.weightx = 1.0;

    bottom.add(b, c_1);

    c.gridwidth = 2;
    c.gridx = 0;
    c.gridy = 1;
    c.weighty = 1.0;

    add(bottom, c);
  }
}
