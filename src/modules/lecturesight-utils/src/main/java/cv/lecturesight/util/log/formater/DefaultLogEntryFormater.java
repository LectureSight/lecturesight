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
package cv.lecturesight.util.log.formater;

import cv.lecturesight.util.log.listener.LogEntryFormater;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

public class DefaultLogEntryFormater implements LogEntryFormater {

  private final static char NEWLINE = '\n';

  @Override
  public String formatEntry(LogEntry entry) {
    // print log message
    StringBuilder sb = new StringBuilder();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    sb.append(sdf.format(new Date(entry.getTime()))).append(" ");

    sb.append(formatLevel(entry.getLevel())).append(" ");

    sb.append(formatReference(entry));

    sb.append(entry.getMessage());

    //System.out.println("\033[34mThis is blue.\033[0m\n");

    // print Exception if present
    Throwable th = entry.getException();
    if (th != null) {
      sb.append(NEWLINE);
      StackTraceElement[] trace = th.getStackTrace();
      for (StackTraceElement elm : trace) {
        sb.append(elm.toString());
        sb.append(NEWLINE);
      }
      sb.append(NEWLINE);
    }

    return sb.toString();
  }

  public String formatLevel(int level) {
    switch (level) {
      case LogService.LOG_DEBUG:
        return "DEBUG";
      case LogService.LOG_ERROR:
        return "ERROR";
      case LogService.LOG_INFO:
        return "INFO ";
      case LogService.LOG_WARNING:
        return "WARN ";
      default:
        return "[ ? ] ";  // should never happen
    }
  }

  public String formatReference(LogEntry entry) {
    String out = "";
    try {
      ServiceReference ref = entry.getServiceReference();
      out = (String) ref.getProperty("service.pid");
      out += " : ";
    } catch (Exception e) {
    }
    return out;
  }
}
