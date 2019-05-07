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

import cv.lecturesight.display.DisplayService;
import cv.lecturesight.framesource.FrameSourceProvider;
import cv.lecturesight.gui.api.UserInterface;
import cv.lecturesight.profile.api.SceneProfile;
import cv.lecturesight.profile.api.SceneProfileListener;
import cv.lecturesight.profile.api.SceneProfileManager;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;

import javax.swing.JPanel;

/**
 * UserInterface adapter for the Scene Profile Editor UI. In addition, this
 * implements the <code>SceneProfileListener</code> necessary to act on profiles
 * being added or removed.
 *
 */
public class SceneProfileUI implements UserInterface, SceneProfileListener {

  final static String UI_TITLE = "Scene Profile Editor";  // window name
  @Setter
  DisplayService dsps;                        // we need the display service
  @Setter
  FrameSourceProvider fsp;                    // reference the FrameSourceProvider so
  // this gets only activated if there is video input
  @Setter
  SceneProfileManager spm;                    // ..and the scene profile manager
  private SceneProfileEditorPanel editor;     // our editor UI panel

  protected void activate(ComponentContext cc) throws Exception {
    editor = new SceneProfileEditorPanel(this); // create new UI panel
    editor.setProfileList(spm.getProfiles());   // load list of profiles
    editor.reset(spm.getActiveProfile());       // make UI load current profile
    spm.registerProfileListener(this);          // register at SPM
  }

  protected void deactivate(ComponentContext cc) throws Exception {
    spm.unregisterProfileListener(this);        // unregister from SPM
  }

  @Override
  public String getTitle() {
    return UI_TITLE;
  }

  @Override
  public JPanel getPanel() {
    return editor;
  }

  @Override
  public boolean isResizeable() {
    return true;
  }

  @Override
  public void profileActivated(SceneProfile profile) {
     if (editor.profile == profile) {
        editor.reset(profile);
     }
  }

  @Override
  public void profileInstalled(SceneProfile profile) {
    editor.setProfileList(spm.getProfiles());
  }

  @Override
  public void profileUpdated(SceneProfile profile) {
    if (editor.profile == profile) {
      editor.reset(profile);
    }
  }

  @Override
  public void profileRemoved(SceneProfile profile) {
    editor.setProfileList(spm.getProfiles());
  }
}
