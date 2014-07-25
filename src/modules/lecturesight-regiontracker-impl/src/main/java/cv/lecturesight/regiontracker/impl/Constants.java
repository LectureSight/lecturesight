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
package cv.lecturesight.regiontracker.impl;

public class Constants {

  final static String PROPKEY_OBJECTS_MAX = "maxnum";
  final static String PROPKEY_SIZE_MIN = "size.min";
  final static String PROPKEY_SIZE_MAX = "size.max";
  final static String PROPKEY_WIDTH_MIN = "width.min";
  final static String PROPKEY_WIDTH_MAX = "width.max";
  final static String PROPKEY_HEIGHT_MIN = "height.min";
  final static String PROPKEY_HEIGHT_MAX = "height.max";
  final static String PROPKEY_TIMETOLIVE = "ttl";
  final static String PROPKEY_DEBUG = "debug";
  final static String WINDOWNAME_OVERLAP = "map.interfame.overlap";
  final static String SIGNAME_DONE_COMPUTE_OVERLAP = "regiontracker.COMPUTE_OVERLAP_DONE";
  final static String SIGNAME_DONE_CORRELATION = "regiontracker.CORRELATION_DONE";
  final static int    pairsBufferLength = 1024;
}
