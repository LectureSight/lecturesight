package cv.lecturesight.display.impl;

import cv.lecturesight.display.DisplayRegistration;

public class DisplayRegistrationImpl implements DisplayRegistration {

  private static int nextId = 0;
  private final int id;
  private String sid;

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
  
  public boolean equals(DisplayRegistrationImpl other) {
    return id == other.id && sid.equals(other.sid);
  }
}
