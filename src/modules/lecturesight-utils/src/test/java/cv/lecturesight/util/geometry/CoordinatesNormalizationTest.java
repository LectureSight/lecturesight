package cv.lecturesight.util.geometry;

import org.junit.Assert;
import org.junit.Test;

public class CoordinatesNormalizationTest {

  @Test
  public void toNormalized() {

    CoordinatesNormalization normalizer = new CoordinatesNormalization(9, 7);

    // There is only an integer centre if the width and height is an odd number of pixels
    // 0 1 2 3 4 5 6 7 8 centre = 4, width = 9

    // Centre
    NormalizedPosition centre = normalizer.toNormalized(new Position(4, 3));
    Assert.assertEquals(new NormalizedPosition(0,0), centre);

    // Top left
    NormalizedPosition topLeft = normalizer.toNormalized(new Position(0, 0));
    Assert.assertEquals(new NormalizedPosition(-1,1), topLeft);

    // Top right
    NormalizedPosition topRight = normalizer.toNormalized(new Position(8, 0));
    Assert.assertEquals(new NormalizedPosition(1,1), topRight);

    // Bottom left
    NormalizedPosition bottomLeft = normalizer.toNormalized(new Position(0, 6));
    Assert.assertEquals(new NormalizedPosition(-1,-1), bottomLeft);

    // Bottom right
    NormalizedPosition bottomRight = normalizer.toNormalized(new Position(8, 6));
    Assert.assertEquals(new NormalizedPosition(1,-1), bottomRight);

    normalizer = new CoordinatesNormalization(640, 360);

    Assert.assertEquals(
      new NormalizedPosition(-1,1),
      normalizer.toNormalized(new Position(0, 0)));

    Assert.assertEquals(
      new NormalizedPosition(1,1),
      normalizer.toNormalized(new Position(639, 0)));

    Assert.assertEquals(
      new NormalizedPosition(-1,-1),
      normalizer.toNormalized(new Position(0, 359)));

    Assert.assertEquals(
      new NormalizedPosition(1,-1),
      normalizer.toNormalized(new Position(639, 359)));

  }

  @Test
  public void fromNormalized() {

    CoordinatesNormalization normalizer = new CoordinatesNormalization(9, 7);

    Assert.assertEquals(
      new Position(4,3),
      normalizer.fromNormalized(new NormalizedPosition(0f, 0f)));

    Assert.assertEquals(
      new Position(0,0),
      normalizer.fromNormalized(new NormalizedPosition(-1, 1)));

    Assert.assertEquals(
      new Position(8,0),
      normalizer.fromNormalized(new NormalizedPosition(1, 1)));

    Assert.assertEquals(
      new Position(0,6),
      normalizer.fromNormalized(new NormalizedPosition(-1, -1)));

    Assert.assertEquals(
      new Position(8,6),
      normalizer.fromNormalized(new NormalizedPosition(1, -1)));

  }
}
