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
package cv.lecturesight.videoanalysis.change.impl;

public class Constants {

  final static String SIGNAME_DONE_DETECTION = "cv.lecturesight.change.detect.DONE";
  final static String SIGNAME_DONE_CLEANING = "cv.lecturesight.change.clean.DONE";
  final static String PROPKEY_THRESH = "changedetect.threshold";
  final static String PROPKEY_DISPLAY_RAW = "changedetect.display.changemap";
  final static String PROPKEY_DISPLAY_DILATED = "changedetect.display.dilated";
  final static String WINDOWNAME_CHANGE_RAW = "change.raw";
  final static String WINDOWNAME_CHANGE_DILATED = "change.dilated";
  
}
