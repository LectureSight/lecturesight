package com.wulff.lecturesight.viscaoverip.service;

import com.wulff.lecturesight.visca.api.VISCAService;
import com.wulff.lecturesight.viscaoverip.protocol.Message;
import com.wulff.lecturesight.viscaoverip.protocol.VISCA;
import cv.lecturesight.util.DummyInterface;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component(name = "cv.lecturesight.ptz.steering.commands", immediate = true)
@Service()
@Properties({
  @Property(name = "osgi.command.scope", value = "cam"),
  @Property(name = "osgi.command.function", value = {"home","move"})
})
public class ConsoleCommands implements DummyInterface {

  @Reference
  VISCAService parent;

  // TODO add integer IDs for cameras so that console commands are compatible
  // with serial VISCA service
  public void home(String[] args) {
//    int adr = Integer.parseInt(args[0]);
//    VISCACameraImpl cam = ((VISCAServiceImpl)parent).cameras[adr-1];
//    if (cam != null) {
//      Message msg = VISCA.CMD_MOVE_HOME.clone();
//      msg.setAddress(adr);
//      cam.pendingMsg.add(msg);
//    } else {
//      System.out.println("Camera #" + adr + " not registered.");
//    }
    System.out.println("Currently not implemented! (TODO add integer IDs for cameras so that console commands are compatible with serial VISCA service)");
  }

  public void move(String[] args) {
//    int adr = Integer.parseInt(args[0]);
//    String move = args[1].trim().toUpperCase();
//    Message msg;
//
//    if (move.equals("UP")) {
//      msg = VISCA.CMD_MOVE_UP.clone();
//      msg.getBytes()[4] = 0;
//      msg.getBytes()[4] = 1;
//    }
//
//    else if (move.equals("DOWN")) {
//      msg = VISCA.CMD_MOVE_DOWN.clone();
//      msg.getBytes()[4] = 0;
//      msg.getBytes()[4] = 1;
//    }
//
//    else if (move.equals("LEFT")) {
//      msg = VISCA.CMD_MOVE_LEFT.clone();
//      msg.getBytes()[4] = 1;
//      msg.getBytes()[4] = 0;
//    }
//
//    else if (move.equals("RIGHT")) {
//      msg = VISCA.CMD_MOVE_RIGHT.clone();
//      msg.getBytes()[4] = 1;
//      msg.getBytes()[4] = 0;
//    }
//
//    else {
//      System.out.println("Unknown command " + move);
//      return;
//    }
//
//    VISCACameraImpl cam = ((VISCAServiceImpl)parent).cameras[adr-1];
//    if (cam != null) {
//      msg.setAddress(adr);
//      cam.pendingMsg.add(msg);
//    } else {
//      System.out.println("Camera #" + adr + " not registered.");
//    }
    System.out.println("Currently not implemented! (TODO add integer IDs for cameras so that console commands are compatible with serial VISCA service)");

  }

}
