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
package cv.lecturesight.profile.api;

import cv.lecturesight.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/** Utility class for de-/serializing SceneProfile descriptions.
 *
 * @author wulff
 */
public class SceneProfileSerializer implements BundleActivator {
  
  static Log log = new Log("Scene Profile Manager");
  static JAXBContext context = null;
  static private Marshaller serializer;
  static private Unmarshaller deserializer;

  @Override
  public void start(BundleContext bc) throws Exception {
    context = JAXBContext.newInstance(SceneProfile.class);
    serializer = context.createMarshaller();
    deserializer = context.createUnmarshaller();
  }

  @Override
  public void stop(BundleContext bc) throws Exception {
    context = null;
    serializer = null;
    deserializer = null;
  }
  
  public static void serialize(SceneProfile profile, OutputStream os) throws ProfileSerializerException {
    if (serializer==null) {
      throw new ProfileSerializerException("Serializer not initilaized");
    }
    try {
      serializer.marshal(profile, os);
    } catch (JAXBException e) {
      String msg = "Unable to deserialize SceneProfile: " + e;
      log.warn(msg);
      throw new IllegalArgumentException(msg);
    }
  }
    
  public static SceneProfile deserialize(InputStream is) throws ProfileSerializerException {
    if (deserializer==null) {
      throw new ProfileSerializerException("Deserializer not initilaized");
    }
    try {
      SceneProfile out = (SceneProfile) deserializer.unmarshal(is);
      is.close();
      return out;
    } catch (JAXBException e) {
      String msg = "Unable to deserialize SceneProfile: " + e;
      log.error(msg, e);
      throw new IllegalArgumentException(msg);
    } catch (IOException e) {
      log.warn("Unable to close InputStream: " + e.getMessage());
    }
    return null;
  }
}
