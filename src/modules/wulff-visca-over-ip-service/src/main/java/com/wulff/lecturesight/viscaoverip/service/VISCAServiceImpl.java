package com.wulff.lecturesight.viscaoverip.service;

import com.wulff.lecturesight.visca.api.CameraPosition;
import com.wulff.lecturesight.viscaoverip.protocol.*;
import com.wulff.lecturesight.visca.api.VISCAService;
import com.wulff.lecturesight.viscaoverip.protocol.VISCA.MessageType;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.util.conf.Configuration;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.pmw.tinylog.Logger;

@Component(name = "com.bwulff.lecturesight.visca", immediate = true)
@Service
public class VISCAServiceImpl implements VISCAService {
   
  ComponentContext cc;
  
  final int VISCAPort = 52381;

  boolean camera_alive = false;

  @Reference
  Configuration config;   // service configuration

  // Camera profiles
  Properties defaultProfile;
  Map<String, Properties> cameraProfiles = new HashMap<String, Properties>();

  DatagramSocket UDPSocket;
  InetAddress broadcastAdr = null;

  private byte[] buffer = new byte[1024];

  HashMap<InetAddress,VISCACameraImpl> cameras = new HashMap<InetAddress,VISCACameraImpl>();

  int updateInterval = 200;   // min number of millisec. between state updates on a camera
  int senderInterval = 20;
  int config_timeout = 3000;  // ms within which camera must reply to initial inquiry

  boolean updatePollFocus = false;

  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
  ScheduledFuture updaterHandle;
  ScheduledFuture senderHandle;

  /**
   * OSGI service activation method.
   *
   * @param cc
   */
  protected void activate(ComponentContext cc) throws Exception {
    this.cc = cc;

    Logger.debug("Activated");
    
    broadcastAdr = InetAddress.getByName("255.255.255.255");

    // load device profiles
    loadProfiles(cc);

    // initialize UDP socket listener
    init_UDPlistener();

    // start with sending the network discovery command
    send_Discovery();

    // start update thread
    updaterHandle = executor.scheduleAtFixedRate(new CameraStateUpdater(),
            updateInterval, updateInterval, TimeUnit.MILLISECONDS);

    // start sender thread
    senderHandle = executor.scheduleAtFixedRate(new CameraCommandSender(),
            senderInterval, senderInterval, TimeUnit.MILLISECONDS);

    try {
	    Thread.sleep(config_timeout);

	    if (camera_alive) {
		Logger.info("Completed VISCA camera initialization");
	    } else {
		Logger.error("Unable to initialize VISCA camera (no response within " + config_timeout + "ms)");
		// cc.getBundleContext().getBundle(0).stop(); 
	    }
    } catch (Exception e) {
	Logger.warn("Exception testing for camera startup" + e.getMessage());
    }
  }

  /**
   * OSGI service de-activation method.
   *
   * @param cc
   */
  protected void deactivate(ComponentContext cc) {
    try {
      Thread.sleep(500);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.SECONDS);
      Thread.sleep(1500);
    } catch (Exception e) {
      Logger.debug("Unable to terminate scheduled processes cleanly");
    }

    // Unregister camera
    ServiceReference serviceReference = cc.getBundleContext().getServiceReference(PTZCamera.class.getName());
    cc.getBundleContext().ungetService(serviceReference);

    deinit_UDPlistener();

    Logger.debug("Deactivated");
  }

  void init_UDPlistener() throws SocketException {
      UDPSocket = new DatagramSocket(VISCAPort);
  }

  void deinit_UDPlistener() {
      UDPSocket.close();
  }

  /**
   * Load the camera model profiles from the bundle resources into memory.
   *
   * @param context
   */
  void loadProfiles(ComponentContext context) {
    // load device profiles
    Enumeration entryURLs = context.getBundleContext().getBundle().findEntries("profiles", "*.properties", false);
    while (entryURLs.hasMoreElements()) {
      URL url = (URL) entryURLs.nextElement();
      try {
        Properties props = new Properties();
        props.load(url.openStream());
        if (props.containsKey(Constants.PROFKEY_MODEL_ID)) {
          String idStr = props.getProperty(Constants.PROFKEY_MODEL_ID);
          if (idStr.equals("DEFAULT")) {
            defaultProfile = props;
            Logger.info("Registered default camera profile");
          } else {
            cameraProfiles.put(idStr, props);
            Logger.info("Registered camera profile for " + props.getProperty(Constants.PROFKEY_VENDOR_NAME) + " " + props.getProperty(Constants.PROFKEY_MODEL_NAME));
          }
        } else {
          Logger.warn("Camera profile " + url.toString() + " does not contain model ID!");
        }
      } catch (IOException e) {
        Logger.warn("Failed to load device profile from " + url.toString() + " : " + e.getMessage());
      }
    }
  }

  /**
   * Sends the content of <code>b</code> over the serial port. Method is
   * synchronized so that competing calls are enqueued.
   *
   * @param a
   */
  synchronized void send(byte[] b, InetAddress adr) {
    Logger.trace(" >>" + ByteUtils.byteArrayToHex(b, -1));
    try {
      DatagramPacket sendPacket = new DatagramPacket(b, b.length, adr, VISCAPort);
      UDPSocket.send(sendPacket);
    } catch (Exception e) {
      Logger.error("Error while sending data: " + ByteUtils.byteArrayToHex(b, -1), e);
    }
  }

  /**
   * Sends the AddressSet broadcast message requesting all cameras to reply with
   * their address.
   *
   */
  void send_Discovery() {
    Logger.info("Sending network discovery broadcast message.");
    send(VISCA.NET_ADDRESS_SET.getBytes(), broadcastAdr);
  }

  /**
   * Sends the CAM_VersionInq command to the specified camera.
   *
   * @param adr address of the camera to be queried
   */
  void send_CamInfo(InetAddress adr) {
    Logger.info("Sending camera info inquiry command to " + adr.getHostAddress());
    Message msg = VISCA.INQ_CAM_VERSION.clone();
    msg.getBytes()[0] += 1;
    send(msg.getBytes(), adr);
  }

  void cancelMovement(int cidx) {
    
    // TODO remove movement commands from pendingMsg!
    // TODO what to do with commands in issuedMsg (that did not receive ACK yet)

    Logger.debug("Cancel movement");
    
    VISCACameraImpl camera;
    if ((camera = cameras[cidx-1]) != null) {
      for (int i=1; i < camera.sockets.length; i++) {   // start with index 1 since 0 is a pseudo-socket (for inquiries)
        Message m = camera.sockets[i];
        if (m != null && m.getMessageType() == MessageType.MOVEMENT) {
          Message cancel = VISCA.NET_COMMAND_CANCEL.clone();
          byte[] pkg = cancel.getBytes();
          pkg[0] += 1;
          pkg[1] += i;
          send(pkg, camera.address);
        }
      }
    }
  }
  
  void clearInterface(InetAddress adr) {
    Logger.debug("Clear interface");
    Message msg = VISCA.NET_IF_CLEAR.clone();
    msg.getBytes()[0] += 1;
    send(msg.getBytes(), adr);
  }
  
  /**
   * Creates a camera object that represents a registered VISCA devices and puts
   * it into the array representing the seven possible addresses.
   *
   * @param adr address of device
   * @param msg VersionInq message from device
   */
  private void createCamera(InetAddress adr, byte[] msg) {

    // extract camera information from VISCA message
    String model = "0x" + ByteUtils.byteToHex(msg[4]) + ByteUtils.byteToHex(ByteUtils.low(msg[5]));
    String rom_ver = "0x" + ByteUtils.byteToHex(msg[6]) + ByteUtils.byteToHex(msg[7]);
    int sockets = msg[8];

    // try to find camera profile, if none found use default profile
    Properties profile;
    if (cameraProfiles.containsKey(model)) {
      profile = cameraProfiles.get(model);
    } else {
      profile = defaultProfile;
    }

    String modelStr = profile.getProperty("camera.vendor.name") + " " + profile.getProperty("camera.model.name");
    Logger.info("Registered " + modelStr + " (model ID " + model + ", ROM version " + rom_ver + ") at address #" + adr);

    // register camera
    VISCACameraImpl camera = new VISCACameraImpl(adr, sockets, rom_ver, profile);
    camera.parent = this;
    cameras.put(adr, camera);

    // register camera as OSGI service
    String camName = camera.model_name + " [" + adr + "]";
    Dictionary props = new Hashtable();
    props.put("adr", adr);                  // TODO look up / change places where former 'id' key is used
    props.put("name", camName);
    props.put("type", "VISCA");
    props.put("port", config.get(Constants.PROPKEY_PORT_DEVCICE));

    cc.getBundleContext().registerService(PTZCamera.class.getName(), camera, props);
    camera_alive = true;
  }

  void copyPart(byte[] src, byte[] dest, int offset, int length) {
      int glength = offset + length;
      int dp = 0;
      for (int sp = offset; sp < glength; sp++) {
          dest[dp++] = src[sp];
      }
  }
  
  /**
   * Called in the event of serial input. Parses the message and acts accordingly.
   *
   * @param arg0
   */
  public void packetReceived(DatagramPacket packet) {
    
      copyPart(packet.getData(), buffer, packet.getOffset(), packet.getLength());
    
      // debug output
      Logger.trace(" <<" + ByteUtils.byteArrayToHex(buffer, packet.getLength()));

      // did we receive an error?
      if (ByteUtils.high(buffer[1]) == 6) {
        handleError(packet.getAddress(), buffer);
      } // did we receive ACK/Completion message?
      else if (len == 3) {
        handleACKCompletion(packet.getAddress(), buffer);
      } // did we receive a position update?
      else if (len == 11 && buffer[1] == (byte) 0x50) {
        handlePositionUpdate(packet.getAddress(), buffer);
      } // did we receive an ADDRESS_SET reply?
      else if (buffer[0] == (byte) 0x88 && buffer[1] == (byte) 0x30) {
        handleDiscoveryReply(packet.getAddress(), buffer);
      } // did we receive a CAM_VersionInq reply?
      else if (buffer[1] == (byte) 0x50 && buffer[2] == (byte) 0x00 && len == 10) {
        handleCameraInfoReply(packet.getAddress(), buffer);
      } // did we receive a Focus inquiry reply?
      else if (buffer[1] == (byte) 0x50 && len == 7) {
        handleCameraFocusReply(packet.getAddress(), buffer);
      }
  }

  private void handleError(InetAddress adr, byte[] message) {
    String msg = "Camera at " + adr.getHostAddress() + " gave error: ";
    VISCACameraImpl camera = cameras.get(adr);
    switch (buffer[2]) {
      case 0x02:
        msg += "Syntax Error";
        break;
      case 0x03:
        msg += "Command Buffer Full";
        break;
      case 0x04:
        int socket = ByteUtils.low(buffer[1]);
        camera.setSocket(socket, null);
        msg += "Command Canceled (socket: " + socket + ")";
        break;
      case 0x05:
        msg += "No Socket (socket: " + ByteUtils.low(buffer[1]) + ")";
        break;
      case 0x41:
        msg += "Command Not Executable (socket: " + ByteUtils.low(buffer[1]) + ")";
        break;
      default:
        msg += "Unknown error code " + ByteUtils.byteToHex(buffer[2]);
    }
    Logger.warn(msg);
  }

  private void handleACKCompletion(InetAddress adr, byte[] buffer) {
    String msg = "";
    VISCACameraImpl cam = cameras.get(adr);
    int socket = ByteUtils.low(buffer[1]);
    if (ByteUtils.high(buffer[1]) == 4) {

      // set socket of camera used
      try {
        Message m;
        if ((m = cam.issuedMsg.poll()) != null) {
          cam.setSocket(socket, m);
        } else {
          throw new IllegalStateException("Received ACK for #" + adr.getHostAddress() + " but no command to be assigned.");
        }
      } catch (NullPointerException e) {
        Logger.error("Failed to set socket state on #" + adr.getHostAddress() + ", camera object not initialized.");
      }

      msg = "ACK";
    } else if (ByteUtils.high(buffer[1]) == 5) {

      // set socket of camera unused
      if (socket > 0) {
        try {
          cam.setSocket(socket, null);
        } catch (NullPointerException e) {
          Logger.error("Failed to set socket state on #" + adr.getHostAddress() + ", camera object not initialized.");
        }
      }

      msg = "Completion";
    }
    msg += " from #" + adr + " (socket: " + socket + ")";
    
    Logger.trace(msg);    
  }

  int last_pan = -1;
  int last_tilt = -1;

  private void handlePositionUpdate(InetAddress adr, byte[] buffer) {

    Logger.trace("Received camera position update");

    int pan = ((buffer[2] & 0x0f) << 12) + ((buffer[3] & 0x0f) << 8) + ((buffer[4] & 0x0f) << 4) + (buffer[5] & 0x0f);
    int tlt = ((buffer[6] & 0x0f) << 12) + ((buffer[7] & 0x0f) << 8) + ((buffer[8] & 0x0f) << 4) + (buffer[9] & 0x0f);

    // this assumes that the camera uses the lower half of the 16bit value range
    // for right side and upper half for left side
    pan = pan > 0x7fff ? pan - 0xffff : pan;

    // same as above for tilt
    tlt = tlt > 0x7fff ? tlt - 0xffff : tlt;

    try {
      VISCACameraImpl camera = cameras.get(adr);
      CameraPosition pos = camera.state.currentPosition();

      if (last_pan != pan || last_tilt != tlt) {
	Logger.debug("Camera position updated: last_pan=" + last_pan + " last_tilt=" + last_tilt + " new pan=" + pan + " new tilt=" + tlt);
        last_pan = pan;
        last_tilt = tlt;
      }
      pos.set(pan, tlt);
      camera.notifyCameraListeners();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleCameraFocusReply(InetAddress adr, byte[] msg) {

    int focus = ((buffer[2] & 0x0f) << 12) + ((buffer[3] & 0x0f) << 8) + ((buffer[4] & 0x0f) << 4) + (buffer[5] & 0x0f);

    VISCACameraImpl camera = cameras.get(adr);
    camera.state.focus = focus;

    Logger.trace("Received camera focus position update: camera " + adr + " focus " + focus);
  }

  private void handleDiscoveryReply(InetAddress adr, byte[] msg) {
    Logger.info("Camera discovered on address " + adr);
    send_CamInfo(adr);
  }

  private void handleCameraInfoReply(InetAddress adr, byte[] msg) {
    Logger.info("Received camera info from #" + adr);
    createCamera(adr, ByteUtils.trimArray(buffer, buffer.length));
  }

  /**
   * Threads that frequently sends inquiries to all registered cameras.
   *
   */
  class CameraStateUpdater implements Runnable {

    Message inq_msg = VISCA.INQ_PAN_TILT_POS.clone();
    Message inq_focus = VISCA.INQ_FOCUS_POS.clone();

    @Override
    public void run() {

      try {
        
          for (InetAddress adr : cameras.keySet()) {
              VISCACameraImpl camera = cameras.get(adr);
              
              Logger.trace("Requesting camera position update");
              inq_msg.getBytes()[0] = (byte) (VISCA.ADR_CAMERA_N + 1);
              send(inq_msg.getBytes(), adr);
              
              if (updatePollFocus) {
                  Logger.trace("Requesting camera focus update");
                  inq_focus.getBytes()[0] = (byte) (VISCA.ADR_CAMERA_N + 1);
                  send(inq_focus.getBytes(), adr);
              }
          }
      } catch (Exception e) {
	 throw new IllegalStateException("Exception running camera state updater. ", e);
      }
    }
  }

  /**
   * Thread that sends commands to the registered cameras.
   *
   */
  class CameraCommandSender implements Runnable {

    @Override
    public void run() {
        for (InetAddress adr : cameras.keySet()) {
            VISCACameraImpl camera = cameras.get(adr);
            Message msg;
            if (camera != null && camera.interfaceReady() && (msg = camera.pendingMsg.poll()) != null) {
                camera.issuedMsg.add(msg);
                send(msg.getBytes(), adr);
            }
        }
    }
  }

}
