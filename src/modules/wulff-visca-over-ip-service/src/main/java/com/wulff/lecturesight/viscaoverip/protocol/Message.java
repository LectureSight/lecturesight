package com.wulff.lecturesight.viscaoverip.protocol;

import com.wulff.lecturesight.viscaoverip.protocol.VISCA.MessageType;

public class Message implements Cloneable {

  MessageType type;
  byte[] data;   // message template

  public Message(VISCA.MessageType type) {
    this.type = type;
  }

  public MessageType getMessageType() {
    return type;
  }
  
  public Message(VISCA.MessageType type, int[] data) {
    this.type = type;
    this.data = ByteUtils.i2b(data);
  }
  
  public Message setbyteValue(int index, byte value) {
      Message out = clone();
      out.data[ VISCA.CMD_LENGTH + index ] = value;
      return out;
  }
  
  public Message setshortValue(int index, short value) {
      byte [] data = ByteUtils.s2b(value);
      Message out = clone();
      for (int i = 0; i< data.length; i++){
          out.data [ VISCA.CMD_LENGTH + index + i ] = data[i];
      }
      return out;
  }

  public byte[] getBytes() {
    return data;
  }

  public void setAddress(int adr) {
    data[0] += adr;
  }

  @Override
  public Message clone() {
    Message clone = new Message(type);
    clone.data = new byte[this.data.length];
    System.arraycopy(data, 0, clone.data, 0, data.length);
    return clone;
  }
}
