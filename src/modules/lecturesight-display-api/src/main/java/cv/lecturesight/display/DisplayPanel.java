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
package cv.lecturesight.display;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import javax.swing.JLabel;

public abstract class DisplayPanel extends JLabel {

  private CustomRenderer renderer = null;
  protected boolean isRecording = false;
  protected File recordingDir = null;

  public abstract Dimension getImageDimension();

  public boolean hasCustomRenderer() {
    return renderer != null;
  }

  public CustomRenderer getCustomRenderer() {
    return renderer;
  }

  public void setCustomRenderer(CustomRenderer renderer) {
    this.renderer = renderer;
  }

  public Point getPositionInImage(Point p) {
    Dimension iDim = getImageDimension();
    Dimension cDim = this.getSize();
    Point pout = new Point(p);

    // the image is centred if cDim.x|y > iDim.x|y otherwise origins are coincident
    if ( cDim.width > iDim.width || cDim.height > iDim.height ) {
      pout.x -= (cDim.width - iDim.width) / 2;
      pout.y -= (cDim.height - iDim.height) / 2;
    }

    return pout;
  }

  public void setRecordingDir(String path) {
    String prefix = System.getProperty("recording.dir");
    if (prefix != null) {
      recordingDir = new File(prefix + File.separator + path);
    } else {
      recordingDir = new File("record" + File.separator + path);
    }
  }

  public File getRecordingDir() {
    return recordingDir;
  }

  public boolean isRecording() {
    return isRecording;
  }

  public void setRecording(boolean isRecording) {
    this.isRecording = isRecording;
  }
}
