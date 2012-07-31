package cv.lecturesight.opencl.impl.trigger;

import cv.lecturesight.opencl.api.OCLSignal;

public class OCLSignalImpl implements OCLSignal {

  private int id;
  private String name;

  public OCLSignalImpl(int id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof OCLSignalImpl) {
      OCLSignalImpl otherSignal = (OCLSignalImpl) other;
      return otherSignal.id == this.id && otherSignal.name.equals(this.name);
    } else {
      return false;
    }
  }
}
