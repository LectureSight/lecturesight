package cv.lecturesight.cameraoperator.scripted;

import cv.lecturesight.scripting.api.ScriptBridge;

public class BridgeRegistration {

  String identifier;
  ScriptBridge bridgeObject;
  String[] imports;

  public BridgeRegistration(String identifier, ScriptBridge bridgeObject, String[] imports) {
    this.identifier = identifier;
    this.bridgeObject = bridgeObject;
    this.imports = imports;
  }
}
