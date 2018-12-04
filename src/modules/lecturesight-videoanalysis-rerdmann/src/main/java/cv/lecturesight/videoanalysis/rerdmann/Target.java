package cv.lecturesight.videoanalysis.rerdmann;

import cv.lecturesight.objecttracker.TrackerObject;

class Target {

  static targetSeq = 1

  int id = -1;
  int seq = -1;

  // Initial position
  int first_x;
  int first_y;

  // Current position
  int x;
  int y;

  // Offset from last position
  int vx;
  int vy;
  double vt;

  // Max distance moved from point of origin in the target's lifetime
  double vt_origin = 0;

  long first_seen = 0; // Time that the target was first seen
  long time = 0;

  long last_move = 0;  // Time that the target last moved
  long last_match = 0; // Time that the target last matched the template
  int matchscore = 0;  // Last match score

  Box searchbox;
  Box updatebox;

  TrackerObject to;    // TrackerObject repreenting this target

  Target(int x, int y) {
    this.seq = targetSeq++;
    this.x = x;
    this.y = y;
    this.first_x = x;
    this.first_y = y;
    int ht = 32 / 2;
    this.searchbox = new Box(x-ht-5, y-ht-5, x+ht+5, y+ht+5);
    this.updatebox = new Box(x-ht-5, y-ht-5, x+ht+5, y+ht+5);
    first_seen = System.currentTimeMillis();
    last_move = first_seen;
    to = new TrackerObject(first_seen);
  }
}
