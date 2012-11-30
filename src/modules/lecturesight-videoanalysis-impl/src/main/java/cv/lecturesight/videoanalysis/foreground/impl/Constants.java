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
package cv.lecturesight.videoanalysis.foreground.impl;

public class Constants {

  final static String PROPKEY_DISPLAY_UPDATEMAP = "foreground.display.updatemap";
  final static String PROPKEY_DISPLAY_FOREGROUNDMAP = "foreground.display.foregroundmap";
  final static String PROPKEY_CCL_MAXBLOBS = "foreground.ccl.maxblobs";
  final static String PROPKEY_CCL_MINSIZE = "foreground.ccl.blobsize.min";
  final static String PROPKEY_CCL_MAXSIZE = "foreground.ccl.blobsize.max";
  final static String PROPKEY_DECAY_THRESHRATIO = "foreground.decay.threshold";
  final static String PROPKEY_DECAY_ALPHA = "foreground.decay.alpha";
  final static String PROPKEY_PHANTOMDECAY_THRESH = "foreground.decay.phantom.thresh";
  final static String SIGNAME_DONE_UPDATE = "cv.lecturesight.foregorund.update.DONE";
  final static String SIGNAME_DONE_CLEANING = "cv.lecturesight.foreground.cleaning.DONE";
  final static String WINDOWNAME_UPDATEMAP = "fg.updatemap";
  final static String WINDOWNAME_FOREGROUNDMAP = "fg.map";
  
}
