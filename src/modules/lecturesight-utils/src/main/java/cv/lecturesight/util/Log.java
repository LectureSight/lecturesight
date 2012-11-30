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
package cv.lecturesight.util;

import org.osgi.service.log.LogService;

public class Log {

  private static LogService service = null;
  private String moduleName;

  public Log() {
    moduleName = "unknown";
  }

  public Log(String name) {
    moduleName = name;
  }

  public void info(String message) {
    if (service != null) {
      service.log(LogService.LOG_INFO, build(message));
    } else {
      System.out.println(build(message));
    }
  }

  public void debug(String message) {
    if (service != null) {
      service.log(LogService.LOG_DEBUG, build(message));
    } else {
      System.out.println(build(message));
    }
  }

  public void warn(String message) {
    if (service != null) {
      service.log(LogService.LOG_WARNING, build(message));
    } else {
      System.out.println(build(message));
    }
  }

  public void error(String message) {
    if (service != null) {
      service.log(LogService.LOG_ERROR, build(message));
    } else {
      System.err.println(build(message));
    }
  }

  public void error(String message, Throwable th) {
    if (service != null) {
      service.log(LogService.LOG_ERROR, message, th);
    } else {
      System.err.println(build(message));
      th.printStackTrace();
    }
  }

  private String build(String message) {
    StringBuilder sb = new StringBuilder();
    sb.append(moduleName);
    sb.append(" : ");
    sb.append(message);
    return sb.toString();
  }

  static void setLogService(LogService log) {
    service = log;
  }
}
