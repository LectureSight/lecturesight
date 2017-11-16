package com.wulff.lecturesight.viscaoverip.service;

public class Limits {

  int min;
  int max;

  public Limits(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public int min() {return min;}

  public int max() {return max;}

  public boolean in(int i) {
    return (i >= min) && (i <= max);
  }

  public int clamp(int i) {
    if (i < min) return min;
    else if (i > max) return max;
    else return i;
  }
}
