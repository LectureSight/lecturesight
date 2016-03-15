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
package cv.lecturesight.framesource.impl;

import java.util.LinkedHashMap;
import java.util.Map;

public class FrameSourceDescriptor {

  private String type;
  private String locator;
  private Map<String,String> config = new LinkedHashMap<String,String>();

  public FrameSourceDescriptor(String fsdStr) throws FrameSourceDescriptorParsingException {
    parseSourceDescriptorString(fsdStr);
  }

  private void parseSourceDescriptorString(String input) throws FrameSourceDescriptorParsingException {

    String[] parts = input.split("\\:\\/\\/", 2);
    if (parts.length == 2) {
      type = parts[0];

      // find config string
      int cfgStart = parts[1].indexOf("[");
      int cfgEnd = parts[1].indexOf("]");
      if (cfgStart != -1) {
        locator = parts[1].substring(0, cfgStart);
        if (cfgEnd != -1) {
          String confStr = parts[1].substring(cfgStart+1, cfgEnd);
          parseConfigString(confStr);
        } else {
          throw new FrameSourceDescriptorParsingException("Frame source configuration not closed");
        }
      } else {
        locator = parts[1];
      }

    } else {
      throw new FrameSourceDescriptorParsingException("Malformed frame source descriptor");
    }
  }

  public void parseConfigString(String confStr) {
    String[] parts = confStr.split("\\;");
    for (String prop : parts) {
      int eq = prop.indexOf("=");
      if (eq != -1) {
        config.put(prop.substring(0,eq), prop.substring(eq+1));
      } else {
        config.put(prop, "true");
      }
    }
  }

  public String getType() {
    return type;
  }

  public String getLocator() {
    return locator;
  }

  public Map<String,String> getConfiguration() {
    return config;
  }
}
