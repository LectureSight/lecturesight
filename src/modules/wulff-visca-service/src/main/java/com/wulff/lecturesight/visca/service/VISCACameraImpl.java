package com.wulff.lecturesight.visca.service;

import com.wulff.lecturesight.visca.api.CameraPosition;
import com.wulff.lecturesight.visca.protocol.ByteUtils;
import com.wulff.lecturesight.visca.protocol.Message;
import com.wulff.lecturesight.visca.protocol.VISCA;
import cv.lecturesight.ptz.api.CameraListener;
import cv.lecturesight.ptz.api.PTZCamera;
import cv.lecturesight.ptz.api.PTZCameraProfile;
import cv.lecturesight.util.geometry.Position;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class VISCACameraImpl implements PTZCamera {

  int address;
  int numSockets;
  
  String verROM;
  String model_name;

  Limits lim_pan, lim_tilt, lim_zoom;
  Limits speed_pan, speed_tilt, speed_zoom;

  CameraState state;
  CameraPosition target;

  VISCAServiceImpl parent;

  PTZCameraProfile profile;

  LinkedList<Message> pendingMsg; // queue of commands that should be issued
  LinkedList<Message> issuedMsg;  // fifo of commands that have been send and are waiting for ACK

  Message[] sockets;    // model that mirrors the state of the camera's command buffer

  long lastUpdate;      // time millis of last position update
  
  List<CameraListener> observers;

  public VISCACameraImpl(int address, int numSockets, String verROM, Properties profile) {
    this.address = address;
    this.state = new CameraState();
    this.numSockets = numSockets;
    this.pendingMsg = new LinkedList<Message>();
    this.issuedMsg = new LinkedList<Message>();
    this.sockets = new Message[numSockets+1];
    this.verROM = verROM;
    this.lastUpdate = 0l;
    this.observers = new LinkedList<CameraListener>();
    loadProfile(profile);
  }

  final void loadProfile(Properties profile) {
    this.profile = new PTZCameraProfile(profile);
    model_name = stringOrDie(Constants.PROFKEY_MODEL_NAME, profile);
    lim_pan = new Limits(intOrDie(Constants.PROFKEY_PAN_MIN, profile), intOrDie(Constants.PROFKEY_PAN_MAX, profile));
    lim_tilt = new Limits(intOrDie(Constants.PROFKEY_TILT_MIN, profile), intOrDie(Constants.PROFKEY_TILT_MAX, profile));
    lim_zoom = new Limits(intOrDie(Constants.PROFKEY_ZOOM_MIN, profile), intOrDie(Constants.PROFKEY_ZOOM_MAX, profile));
    speed_pan = new Limits(0, intOrDie(Constants.PROFKEY_PAN_MAXSPEED, profile));
    speed_tilt = new Limits(0, intOrDie(Constants.PROFKEY_TILT_MAXSPEED, profile));
    speed_zoom = new Limits(0, intOrDie(Constants.PROFKEY_ZOOM_MAXSPEED, profile));
  }

  int intOrDie(String key, Properties props) {
    if (!props.containsKey(key)) {
      String msg = "Failed loading value " + key + " from camera profile. Key not existing.";
      System.out.println(msg);  // TODO do via logger
      throw new IllegalArgumentException(msg);
    }
    return Integer.parseInt(props.getProperty(key));
  }

  String stringOrDie(String key, Properties props) {
    if (!props.containsKey(key)) {
      String msg = "Failed loading value " + key + " from camera profile. Key not existing.";
      System.out.println(msg);  // TODO do via logger
      throw new IllegalArgumentException(msg);
    }
    return props.getProperty(key);
  }

  public void setSocket(int socket, Message msg) {
    sockets[socket] = msg;
  }

  public Message getSocket(int socket) {
    return sockets[socket];
  }

  public boolean interfaceReady() {
    for (int i = 1; i < sockets.length; i++) {    // start with index 1 since 0 is a pseudo-socket (for inquiries)
      if (sockets[i] == null) {
        return true;
      }
    }
    return false;
  }

  CameraPosition clampPosition(CameraPosition p) {
    return new CameraPosition(lim_pan.clamp(p.x()), lim_tilt.clamp(p.y()));
  }

  @Override
  public void zoom(int zoom) {
    Message msg = VISCA.CMD_ZOOM.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan target
    byte[] a = ByteUtils.s2b((short)zoom);
    pkg[4] = a[0];
    pkg[5] = a[1];
    pkg[6] = a[2];
    pkg[7] = a[3];

    pendingMsg.add(msg);
  }

  @Override
  public String getName() {
    return model_name;
  }

  @Override
  public PTZCameraProfile getProfile() {
    return profile;
  }

  @Override
  public void reset() {
  }

  @Override
  public void stopMove() {
    Message msg = VISCA.CMD_STOP_MOVE.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)VISCA.DEFAULT_SPEED;
    pkg[5] = (byte)VISCA.DEFAULT_SPEED;  
    pendingMsg.add(msg);    
  }

  @Override
  public void moveHome() {
    Message msg = VISCA.CMD_MOVE_HOME.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;    
    pendingMsg.add(msg);
  }

  @Override
  public void moveUp(int tiltSpeed) {
    Message msg = VISCA.CMD_MOVE_UP.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)VISCA.DEFAULT_SPEED;
    pkg[5] = (byte)tiltSpeed;  
    pendingMsg.add(msg);    
  }

  @Override
  public void moveDown(int tiltSpeed) {
    Message msg = VISCA.CMD_MOVE_DOWN.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)VISCA.DEFAULT_SPEED;
    pkg[5] = (byte)tiltSpeed;  
    pendingMsg.add(msg);    
  }

  @Override
  public void moveLeft(int panSpeed) {
    Message msg = VISCA.CMD_MOVE_LEFT.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)VISCA.DEFAULT_SPEED;
    pendingMsg.add(msg);    
  }

  @Override
  public void moveRight(int panSpeed) {
    Message msg = VISCA.CMD_MOVE_RIGHT.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)VISCA.DEFAULT_SPEED;
    pendingMsg.add(msg);    
  }

  @Override
  public void moveUpLeft(int panSpeed, int tiltSpeed) {
    Message msg = VISCA.CMD_MOVE_UP_LEFT.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)tiltSpeed;
    pendingMsg.add(msg);
  }

  @Override
  public void moveUpRight(int panSpeed, int tiltSpeed) {
    Message msg = VISCA.CMD_MOVE_UP_RIGHT.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)tiltSpeed;
    pendingMsg.add(msg);
  }

  @Override
  public void moveDownLeft(int panSpeed, int tiltSpeed) {
    Message msg = VISCA.CMD_MOVE_DOWN_LEFT.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)tiltSpeed;
    pendingMsg.add(msg);
  }

  @Override
  public void moveDownRight(int panSpeed, int tiltSpeed) {
    Message msg = VISCA.CMD_MOVE_DOWN_RIGHT.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)tiltSpeed;
    pendingMsg.add(msg);
  }

  @Override
  public void moveAbsolute(int panSpeed, int tiltSpeed, Position target) {
    
    // cancel current movement
//    parent.cancelMovement(address);
//    parent.clearInterface(address);
    
    // set new movement target and speeds
    Message msg = VISCA.CMD_MOVE_ABSOULTE.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)tiltSpeed;

    // set pan target
    int val = target.getX();
    int pan = val > 0x7fff ? val - 0xffff : val;
    byte[] a = ByteUtils.s2b((short)pan);
    pkg[6] = a[0];
    pkg[7] = a[1];
    pkg[8] = a[2];
    pkg[9] = a[3];

    // set pan target
    val = target.getY();
    int tilt = val > 0x7fff ? val - 0xffff : val;
    a = ByteUtils.s2b((short)tilt);
    pkg[10] = a[0];
    pkg[11] = a[1];
    pkg[12] = a[2];
    pkg[13] = a[3];

    pendingMsg.add(msg);
  }

  @Override
  public void moveRelative(int panSpeed, int tiltSpeed, Position target) {
    // set new movement target and speeds
    Message msg = VISCA.CMD_MOVE_RELATIVE.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set pan/tilt speed
    pkg[4] = (byte)panSpeed;
    pkg[5] = (byte)tiltSpeed;

    // set pan target
    int val = target.getX();
    int pan = val > 0x7fff ? val - 0xffff : val;
    byte[] a = ByteUtils.s2b((short)pan);
    pkg[6] = a[0];
    pkg[7] = a[1];
    pkg[8] = a[2];
    pkg[9] = a[3];

    // set pan target
    val = target.getY();
    int tilt = val > 0x7fff ? val - 0xffff : val;
    a = ByteUtils.s2b((short)tilt);
    pkg[10] = a[0];
    pkg[11] = a[1];
    pkg[12] = a[2];
    pkg[13] = a[3];

    pendingMsg.add(msg);
  }
  
  @Override
  public void clearLimits() {
    
    // clear down-left limit
    Message msg_dl = VISCA.CMD_LIMIT_CLEAR.clone();
    byte[] pkg = msg_dl.getBytes();
    pkg[0] += this.address;
    pendingMsg.add(msg_dl);
    
    // clear down-left limit
    Message msg_ur = VISCA.CMD_LIMIT_CLEAR.clone();
    pkg = msg_ur.getBytes();
    pkg[0] += this.address;
    pkg[5] = 1;
    pendingMsg.add(msg_ur);
  }

  @Override
  public void setLimitUpRight(int pan, int tilt) {
    Message msg = VISCA.CMD_LIMIT_SET.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set direction
    pkg[5] = 1;

    // set pan target
    pan = pan > 0x7fff ? pan - 0xffff : pan;
    byte[] a = ByteUtils.s2b((short)pan);
    pkg[6] = a[0];
    pkg[7] = a[1];
    pkg[8] = a[2];
    pkg[9] = a[3];

    // set pan target
    tilt = tilt > 0x7fff ? tilt - 0xffff : tilt;
    a = ByteUtils.s2b((short)tilt);
    pkg[10] = a[0];
    pkg[11] = a[1];
    pkg[12] = a[2];
    pkg[13] = a[3];

    pendingMsg.add(msg);
  }

  @Override
  public void setLimitDownLeft(int pan, int tilt) {
    Message msg = VISCA.CMD_LIMIT_SET.clone();
    byte[] pkg = msg.getBytes();

    // set address
    pkg[0] += this.address;

    // set direction
    pkg[5] = 0;

    // set pan target
    pan = pan > 0x7fff ? pan - 0xffff : pan;
    byte[] a = ByteUtils.s2b((short)pan);
    pkg[6] = a[0];
    pkg[7] = a[1];
    pkg[8] = a[2];
    pkg[9] = a[3];

    // set pan target
    tilt = tilt > 0x7fff ? tilt - 0xffff : tilt;
    a = ByteUtils.s2b((short)tilt);
    pkg[10] = a[0];
    pkg[11] = a[1];
    pkg[12] = a[2];
    pkg[13] = a[3];

    pendingMsg.add(msg);
  }

  @Override
  public Position getPosition() {
    return new Position(this.state.currentPosition().x(), this.state.currentPosition().y());
  }

  @Override
  public void stopZoom() {
  }

  @Override
  public void zoomIn(int speed) {
  }

  @Override
  public void zoomOut(int speed) {
  }

  @Override
  public int getZoom() {
    return 0;               // TODO implement!
  }

  public void cancel() {
    parent.cancelMovement(address-1);
  }

  @Override
  public void addCameraListener(CameraListener l) {
    observers.add(l);
  }

  @Override
  public void removeCameraListener(CameraListener l) {
    observers.remove(l);
  }

  void notifyCameraListeners() {
    Position pos = new Position(state.position.x(), state.position.y());
    for (CameraListener cl : observers) {
      cl.positionUpdated(pos);
    }
  }
}