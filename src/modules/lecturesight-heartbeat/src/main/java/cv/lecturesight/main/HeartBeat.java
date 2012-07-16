package cv.lecturesight.main;

interface HeartBeat {

  boolean isReady();
  boolean isRunning();
  void init();
  void deinit();
  void step(int i);
  void go();
  void stop();
  
}
