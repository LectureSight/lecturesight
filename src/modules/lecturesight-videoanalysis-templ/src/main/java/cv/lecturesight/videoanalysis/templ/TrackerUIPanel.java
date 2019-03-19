package cv.lecturesight.videoanalysis.templ;

import cv.lecturesight.display.CustomRenderer;
import cv.lecturesight.display.Display;
import cv.lecturesight.display.DisplayPanel;
import cv.lecturesight.videoanalysis.templ.VideoAnalysisTemplateMatching.Target;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class TrackerUIPanel extends javax.swing.JPanel implements CustomRenderer {

  VideoAnalysisTemplateMatching parent;
  Display display;
  Display inputDisplay;
  DisplayPanel displayPanel;
  Font font = new Font("Monospaced", Font.PLAIN, 10);
  Font smallFont = new Font("Monospaced", Font.PLAIN, 9);
  DecimalFormat df = new DecimalFormat("#0", DecimalFormatSymbols.getInstance(Locale.US));
  final int FPS_SAMPLES = 60;
  double[] frameTime = new double[FPS_SAMPLES];
  int sample_i = 0;
  double fps = 0.0;
  long lastFrameNumber = 0;

  public TrackerUIPanel() {
    initComponents();
  }

  public TrackerUIPanel(VideoAnalysisTemplateMatching parent) {
    initComponents();
    this.inputDisplay = parent.dsps.getDisplayBySID("cam.overview.input");
    this.parent = parent;
    display = parent.dsps.getDisplayBySID("visual");
    displayPanel = display.getDisplayPanel();
    displayPanel.setCustomRenderer(this);
    this.setBackground(Color.black);
    this.setLayout(new BorderLayout());
    this.add(displayPanel, BorderLayout.CENTER);
    this.setSize(display.getSize());
    this.setPreferredSize(display.getSize());
  }

  @Override
  public void render(Graphics g) {

    g.setFont(font);

    // render() may be called if other windows obscure this window, so only update
    // frame timing if it's actually for a new frame
    if (lastFrameNumber != parent.fsrc.getFrameNumber()) {
      double thisFrameTime = System.currentTimeMillis();
      frameTime[sample_i++] = thisFrameTime;

      if (sample_i == FPS_SAMPLES) {
        sample_i = 0;
      }

      if (sample_i % 5 == 0) {
        double earliestFrame = frameTime[sample_i];
        fps = (FPS_SAMPLES - 1) * 1000 / (thisFrameTime - earliestFrame);
      }
      lastFrameNumber = parent.fsrc.getFrameNumber();
    }

    g.drawString("  frame : " + Long.toString(parent.fsrc.getFrameNumber()), 3, 12);
    g.drawString("    fps : " + df.format(fps), 3, 22);

    g.setFont(smallFont);

    StringBuilder targetList = new StringBuilder();

    for (Target t : parent.targets) {
      if (t != null) {

        if (targetList.length() > 0) {
          targetList.append(" ");
          targetList.append(t.seq);
        } else {
          targetList.append(t.seq);
        }

        int halfTargetSize = parent.TARGET_SIZE / 2;

        // change (distance from previous position)
        g.setColor(Color.yellow);
        g.drawRect(t.updatebox.x, t.updatebox.y, t.updatebox.width(), t.updatebox.height());
        g.drawLine(t.x, t.y, t.x + t.vx, t.y + t.vy);
        g.drawString(Integer.toString((int) t.vt), t.x + halfTargetSize + 6, t.y - halfTargetSize + 16);

        // search box
        g.setColor(Color.cyan);
        g.drawRect(t.searchbox.x, t.searchbox.y, t.searchbox.width(), t.searchbox.height());

        // template match
        g.setColor(Color.green);
        g.drawRect(t.x - halfTargetSize, t.y - halfTargetSize, parent.TARGET_SIZE, parent.TARGET_SIZE);
        g.drawString(Integer.toString(t.matchscore), t.x + halfTargetSize + 6, t.y - halfTargetSize + 6);

        drawDiamond(g, t.x, t.y);

        // object data (id, sequence)
        g.setColor(Color.white);
        g.drawString(Integer.toString(t.id) + ":" + Integer.toString(t.seq), t.x - halfTargetSize, t.y - halfTargetSize - 2);
      }
    }

    g.setFont(font);
    if (parent.numTargets > 0) {
      g.drawString("targets : " + parent.numTargets + " (" + targetList + ")", 3, 32);
    } else {
      g.drawString("targets : 0", 3, 32);
    }

  }

  void drawDiamond(Graphics g, int pos_x, int pos_y) {
    g.drawLine(pos_x, pos_y - 1, pos_x - 1, pos_y);
    g.drawLine(pos_x, pos_y - 1, pos_x + 1, pos_y);
    g.drawLine(pos_x, pos_y + 1, pos_x - 1, pos_y);
    g.drawLine(pos_x, pos_y + 1, pos_x + 1, pos_y);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 300, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
