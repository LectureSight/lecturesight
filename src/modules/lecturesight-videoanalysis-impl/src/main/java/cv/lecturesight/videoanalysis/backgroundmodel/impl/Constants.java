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
package cv.lecturesight.videoanalysis.backgroundmodel.impl;

public class Constants {

  final static String SIGNAME_DONE_DIFF = "cv.lecturesight.background.diff.DONE";
  final static String SIGNAME_DO_UPDATE = "cv.lecturesight.background.update.DO";
  final static String SIGNAME_DONE_UPDATE = "cv.lecturesight.background.update.DONE";
  final static String PROPKEY_THRESH_LOW = "background.threshold.low";
  final static String PROPKEY_THRESH_MID = "background.threshold.mid";
  final static String PROPKEY_THRESH_HIGH = "background.threshold.high";
  final static String PROPKEY_UPDATE_ALPHA = "background.update.alpha";
  final static String WINDOWNAME_DIFF = "map.bg.diff";
  final static String WINDOWNAME_MODEL = "model.bg";
  final static String WINDOWNAME_UPDATEMAP = "map.bg.update";

}
