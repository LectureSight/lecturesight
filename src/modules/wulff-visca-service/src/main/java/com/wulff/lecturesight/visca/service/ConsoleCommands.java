package com.wulff.lecturesight.visca.service;

import com.wulff.lecturesight.visca.api.VISCAService;
import com.wulff.lecturesight.visca.protocol.Message;
import com.wulff.lecturesight.visca.protocol.VISCA;
import cv.lecturesight.util.DummyInterface;
import lombok.Setter;

public class ConsoleCommands implements DummyInterface {

  @Setter
  VISCAService parent;

  public void home(String[] args) {
    int adr = Integer.parseInt(args[0]);
    VISCACameraImpl cam = ((VISCAServiceImpl)parent).cameras[adr-1];
    if (cam != null) {
      Message msg = VISCA.CMD_MOVE_HOME.clone();
      msg.setAddress(adr);
      cam.pendingMsg.add(msg);
    } else {
      System.out.println("Camera #" + adr + " not registered.");
    }
  }

  public void move(String[] args) {
    int adr = Integer.parseInt(args[0]);
    String move = args[1].trim().toUpperCase();
    Message msg;

    if (move.equals("UP")) {
      msg = VISCA.CMD_MOVE_UP.clone();
      msg.getBytes()[4] = 0;
      msg.getBytes()[4] = 1;
    }

    else if (move.equals("DOWN")) {
      msg = VISCA.CMD_MOVE_DOWN.clone();
      msg.getBytes()[4] = 0;
      msg.getBytes()[4] = 1;
    }

    else if (move.equals("LEFT")) {
      msg = VISCA.CMD_MOVE_LEFT.clone();
      msg.getBytes()[4] = 1;
      msg.getBytes()[4] = 0;
    }

    else if (move.equals("RIGHT")) {
      msg = VISCA.CMD_MOVE_RIGHT.clone();
      msg.getBytes()[4] = 1;
      msg.getBytes()[4] = 0;
    }

    else {
      System.out.println("Unknown command " + move);
      return;
    }

    VISCACameraImpl cam = ((VISCAServiceImpl)parent).cameras[adr-1];
    if (cam != null) {
      msg.setAddress(adr);
      cam.pendingMsg.add(msg);
    } else {
      System.out.println("Camera #" + adr + " not registered.");
    }
  }

}
