package com.wulff.lecturesight.viscaoverip.protocol;

public class VISCA {

  public static enum MessageType {
    INQUIRY(0x01, 0x10), 
    MOVEMENT(0x01, 0x00), 
    ZOOM(0x01, 0x00), 
    FOCUS(0x01, 0x00), 
    CAM_COMMAND(0x01, 0x00), 
    CAM_ADMIN(0x01, 0x00), 
    NET(0x01, 0x00);            // TODO correct code?
    
    private int[] code;
    MessageType(int one, int two) {
        code = new int[2];
        code[0] = one;
        code[1] = two;
    }
    public int[] getCode() {
        return code;
    }
  }

  /**
   * Enum of error types in VISCA.
   *
   */
  public static enum ErrorType{
    SYNTAX_ERROR,CMD_BUFFER_FULL,CMD_CANCELLED,NO_SOCKET,CMD_NOT_EXECUTABLE,UNKNOWN
  }

  public static final int TERMINATOR = 0xff;
  public static final int ADR_BROADCAST = 0x88;
  public static final int ADR_CAMERA_N = 0x80;
  public static final int DATA = 0x00;

  public static final int DEFAULT_SPEED = 0x01;
  
  public static final int CMD_LENGTH = 4;

  // definition of byte sequences of VISCA messages
  public static final int[] CODE_AddressSet = {ADR_BROADCAST, 0x30, 0x01, TERMINATOR};
  public static final int[] CODE_IfClear = {ADR_CAMERA_N, 0x01, 0x00, 0x01, TERMINATOR};
  public static final int[] CODE_CommandCancel = {ADR_CAMERA_N, 0x20, TERMINATOR};
  public static final int[] CODE_CamVersion_Inq = {ADR_CAMERA_N, 0x09, 0x00, 0x02, TERMINATOR};
  public static final int[] CODE_PanTiltPos_Inq = {ADR_CAMERA_N, 0x09, 0x06, 0x12, TERMINATOR};
  public static final int[] CODE_ZoomPos_Inq = {ADR_CAMERA_N, 0x09, 0x04, 0x47, TERMINATOR};
  public static final int[] CODE_FocusPos_Inq = {ADR_CAMERA_N, 0x09, 0x04, 0x48, TERMINATOR};
  public static final int[] CODE_MoveHome_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x04, TERMINATOR};
  public static final int[] CODE_MoveAbsolute_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x02, DATA, DATA,  DATA, DATA,  DATA, DATA,  DATA, DATA,  DATA, DATA, TERMINATOR};
  public static final int[] CODE_MoveRelative_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x03, DATA, DATA,  DATA, DATA,  DATA, DATA,  DATA, DATA,  DATA, DATA, TERMINATOR};
  public static final int[] CODE_MoveUp_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x03, 0x01, TERMINATOR};
  public static final int[] CODE_MoveDown_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x03, 0x02, TERMINATOR};
  public static final int[] CODE_MoveLeft_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x01, 0x03, TERMINATOR};
  public static final int[] CODE_MoveRight_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x02, 0x03, TERMINATOR};
  public static final int[] CODE_MoveUpLeft_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x01, 0x01, TERMINATOR};
  public static final int[] CODE_MoveUpRight_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x02, 0x01, TERMINATOR};
  public static final int[] CODE_MoveDownLeft_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x01, 0x02, TERMINATOR};
  public static final int[] CODE_MoveDownRight_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x02, 0x02, TERMINATOR};
  public static final int[] CODE_StopMove_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x01, DATA, DATA, 0x03, 0x03, TERMINATOR};
  public static final int[] CODE_LimitSet_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x07, 0x00, DATA, DATA, DATA,  DATA, DATA, DATA, DATA,  DATA, DATA, TERMINATOR};
  public static final int[] CODE_LimitClear_Cmd = {ADR_CAMERA_N, 0x01, 0x06, 0x07, 0x01, DATA, 0x07, 0x0f,  0x0f, 0x0f, 0x07, 0x0f,  0x0f, 0x0f, TERMINATOR};
  public static final int[] CODE_Zoom_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x47, DATA, DATA, DATA, DATA, TERMINATOR};
  public static final int[] CODE_FocusStop_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x08, 0x00, TERMINATOR};
  public static final int[] CODE_FocusFar_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x08, 0x02, TERMINATOR};
  public static final int[] CODE_FocusNear_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x08, 0x03, TERMINATOR};
  public static final int[] CODE_FocusAuto_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x38, 0x02, TERMINATOR};
  public static final int[] CODE_FocusManual_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x38, 0x03, TERMINATOR};
  public static final int[] CODE_FocusDirect_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x48, DATA, DATA, DATA, DATA, TERMINATOR};

  // Non-standard VISCA commands (Vaddio cameras, maybe others)
  public static final int[] CODE_MemoryReset_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x3F, 0x00, DATA, TERMINATOR};
  public static final int[] CODE_MemorySet_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x3F, 0x01, DATA, TERMINATOR};
  public static final int[] CODE_MemoryRecall_Cmd = {ADR_CAMERA_N, 0x01, 0x04, 0x3F, 0x02, DATA, TERMINATOR};
  
  // Network messages __________________________________________________________
  public static final Message NET_ADDRESS_SET = new Message(MessageType.NET, CODE_AddressSet);
  public static final Message NET_IF_CLEAR = new Message(MessageType.NET, CODE_IfClear);
  public static final Message NET_COMMAND_CANCEL = new Message(MessageType.NET, CODE_CommandCancel);

  // Inquiry messages ___________________________________________________________
  public static final Message INQ_CAM_VERSION = new Message(MessageType.INQUIRY, CODE_CamVersion_Inq);
  public static final Message INQ_PAN_TILT_POS = new Message(MessageType.INQUIRY, CODE_PanTiltPos_Inq);
  public static final Message INQ_ZOOM_POS = new Message(MessageType.INQUIRY, CODE_ZoomPos_Inq);
  public static final Message INQ_FOCUS_POS = new Message(MessageType.INQUIRY, CODE_FocusPos_Inq);

  // Command messages __________________________________________________________
  public static final Message CMD_MOVE_HOME = new Message(MessageType.MOVEMENT, CODE_MoveHome_Cmd);
  public static final Message CMD_MOVE_UP = new Message(MessageType.MOVEMENT, CODE_MoveUp_Cmd);
  public static final Message CMD_MOVE_DOWN = new Message(MessageType.MOVEMENT, CODE_MoveDown_Cmd);
  public static final Message CMD_MOVE_LEFT = new Message(MessageType.MOVEMENT, CODE_MoveLeft_Cmd);
  public static final Message CMD_MOVE_RIGHT = new Message(MessageType.MOVEMENT, CODE_MoveRight_Cmd);
  public static final Message CMD_MOVE_UP_LEFT = new Message(MessageType.MOVEMENT, CODE_MoveUpLeft_Cmd);
  public static final Message CMD_MOVE_UP_RIGHT = new Message(MessageType.MOVEMENT, CODE_MoveUpRight_Cmd);
  public static final Message CMD_MOVE_DOWN_LEFT = new Message(MessageType.MOVEMENT, CODE_MoveDownLeft_Cmd);
  public static final Message CMD_MOVE_DOWN_RIGHT = new Message(MessageType.MOVEMENT, CODE_MoveDownRight_Cmd);
  public static final Message CMD_MOVE_ABSOLUTE = new Message(MessageType.MOVEMENT, CODE_MoveAbsolute_Cmd);
  public static final Message CMD_MOVE_RELATIVE = new Message(MessageType.MOVEMENT, CODE_MoveRelative_Cmd);
  public static final Message CMD_STOP_MOVE = new Message(MessageType.MOVEMENT, CODE_StopMove_Cmd);
  public static final Message CMD_LIMIT_SET = new Message(MessageType.CAM_COMMAND, CODE_LimitSet_Cmd);
  public static final Message CMD_LIMIT_CLEAR = new Message(MessageType.CAM_COMMAND, CODE_LimitClear_Cmd);
  public static final Message CMD_ZOOM = new Message(MessageType.ZOOM, CODE_Zoom_Cmd);
  public static final Message CMD_FOCUS_STOP = new Message(MessageType.FOCUS, CODE_FocusStop_Cmd);
  public static final Message CMD_FOCUS_FAR = new Message(MessageType.FOCUS, CODE_FocusFar_Cmd);
  public static final Message CMD_FOCUS_NEAR = new Message(MessageType.FOCUS, CODE_FocusNear_Cmd);
  public static final Message CMD_FOCUS_AUTO = new Message(MessageType.FOCUS, CODE_FocusAuto_Cmd);
  public static final Message CMD_FOCUS_MANUAL = new Message(MessageType.FOCUS, CODE_FocusManual_Cmd);
  public static final Message CMD_FOCUS_DIRECT = new Message(MessageType.FOCUS, CODE_FocusDirect_Cmd);

  // Non-standard VISCA commands (Vaddio cameras, maybe others)
  public static final Message CMD_MOVE_PRESET = new Message(MessageType.MOVEMENT, CODE_MemoryRecall_Cmd);

}
