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
package cv.lecturesight.util.metrics;

import java.io.File;
import org.pmw.tinylog.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class MetricsServiceImpl implements MetricsService {
  
  private BundleContext bcontext;

  public MetricsServiceImpl() {
    Logger.info("Metrics Service");
  } 

  @Override
  public void reset() {
    Logger.info("reset");
  }

  @Override
  public void save() {
    Logger.info("save");
  }

  @Override
  public void show() {
    Logger.info("show");
  }


  @Override
  public void setDescription(String key, String desc) {
    Logger.info("Set description for: " + key + " to: " + desc);
  }

  @Override
  public void incCounter(String key) {
    Logger.info("Increment counter: " + key);

  }

  @Override
  public void setValue(String key, long value) {
    Logger.info("Set value for: " + key + " to " + value);
  }


}
