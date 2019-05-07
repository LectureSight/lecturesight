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
package cv.lecturesight.heartbeat;

import cv.lecturesight.heartbeat.api.HeartBeat;
import cv.lecturesight.util.DummyInterface;

import lombok.Setter;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

/** Implementation of Service API
 *
 */
public class ConsoleCommands implements DummyInterface {

  @Setter
  private HeartBeat main;

  protected void activate(ComponentContext context) {
    Logger.info("Commands activated");
  }

  public void health(String[] param) {
    // CHECKSTYLE:OFF
    System.out.println(main.isAlive() ? "alive" : "dead");
    // CHECKSTYLE:ON
  }

  public void run(String[] param) {
    try {
      ensureReady();
      main.go();
    } catch (RuntimeException e) {
      Logger.error("Failed to initialize HeartBeat: ", e);
    }
  }

  public void stop(String[] param) {
    if (main.isRunning()) {
      main.stop();
      main.deinit();
    }
  }

  public void pause(String[] param) {
    if (main.isRunning()) {
      try {
        main.stop();
      } catch (RuntimeException e) {
        Logger.error("Failed to pause HeartBeat", e);
      }
    }
  }

  public void restart(String[] para) {
    if (main.isRunning()) {
      try {
        main.stop();
        main.deinit();
      } catch (RuntimeException e) {
        Logger.error("Failed to stop HeartBeat", e);
      }
    }
    try {
      ensureReady();
      main.go();
    } catch (RuntimeException e) {
      Logger.error("Failed to start HeartBeat", e);
    }
  }

  public void step(String[] param) {
    int i = 1;
    if (param.length > 0) {
      try {
        i = Integer.parseInt(param[0]);
      } catch (NumberFormatException e) {
        Logger.warn("Usage: ls:step <number of steps>");
      }
    }
    try {
      ensureReady();
      main.step(i);
    } catch (IllegalStateException e) {
      Logger.error("Failed to step", e);
    }
  }

  private void ensureReady() throws RuntimeException {
    if (!main.isReady()) {
      main.init();
    }
  }
}
