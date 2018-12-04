class Box {

  int x;
  int y;
  int max_x;
  int max_y;

  Box(int x, int y, int max_x, int max_y) {
    this.x = x;
    this.y = y;
    this.max_x = max_x;
    this.max_y = max_y;
  }

  public int width() {
    return max_x - x;
  }

  public int height() {
    return max_y - y;
  }

  public boolean includes(int x, int y) {
    return x >= this.x && y >= this.y && x <= max_x && y <= max_y;
  }
}
