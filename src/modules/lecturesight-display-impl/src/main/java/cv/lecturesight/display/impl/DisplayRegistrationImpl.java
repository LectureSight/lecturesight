package cv.lecturesight.display.impl;

import cv.lecturesight.display.DisplayRegistration;

public abstract class DisplayRegistrationImpl implements DisplayRegistration {

  private static int nextId = 0;
  private final int id;
  private String sid;
  protected boolean active; 

  public DisplayRegistrationImpl(String sid) {
    this.sid = sid;
    this.id = nextId++;
  }

  @Override
  public int getID() {
    return id;
  }

  @Override
  public String getSID() {
    return sid;
  }

  @Override
  public void setActive(boolean active) {
    this.active = active;
  }
  
  @Override
  public boolean isActive() {
    return active;
  }
}
