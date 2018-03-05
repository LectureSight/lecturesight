package cv.lecturesight.util.geometry;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class CameraPositionModelTest {

  private CameraPositionModel model = new CameraPositionModel(0, 0, 0, 0);

  @org.junit.Before
  public void setUp() throws Exception {
  }

  @org.junit.After
  public void tearDown() throws Exception {
  }

  @org.junit.Test
  public void testSceneLimits() {

    // Test scene limits calibration

    model.update(-17000, 17000, -9000, 2000);
    checkSceneLimits();

  }

  private void checkSceneLimits() {

    // To camera co-ordinates

    Position centre = model.toCameraCoordinates(new NormalizedPosition(0, 0));
    Assert.assertEquals(new Position(0, -3500), centre);

    Position topLeft = model.toCameraCoordinates(new NormalizedPosition(-1, 1));
    Assert.assertEquals(new Position(-17000, 2000), topLeft);

    Position topRight = model.toCameraCoordinates(new NormalizedPosition(1, 1));
    Assert.assertEquals(new Position(17000, 2000), topRight);

    Position bottomLeft = model.toCameraCoordinates(new NormalizedPosition(-1, -1));
    Assert.assertEquals(new Position(-17000, -9000), bottomLeft);

    Position bottomRight = model.toCameraCoordinates(new NormalizedPosition(1, -1));
    Assert.assertEquals(new Position(17000, -9000), bottomRight);

    // To normalized co-ordinates

    NormalizedPosition centreN = model.toNormalizedCoordinates(centre);
    Assert.assertEquals(new NormalizedPosition(0, 0), centreN);

    NormalizedPosition topLeftN = model.toNormalizedCoordinates(topLeft);
    Assert.assertEquals(new NormalizedPosition(-1, 1), topLeftN);

    NormalizedPosition topRightN = model.toNormalizedCoordinates(topRight);
    Assert.assertEquals(new NormalizedPosition(1, 1), topRightN);

    NormalizedPosition bottomLeftN = model.toNormalizedCoordinates(bottomLeft);
    Assert.assertEquals(new NormalizedPosition(-1, -1), bottomLeftN);

    NormalizedPosition bottomRightN = model.toNormalizedCoordinates(bottomRight);
    Assert.assertEquals(new NormalizedPosition(1, -1), bottomRightN);

    // Check interpolation

    // x = +|- 0.05, +|- 850
    NormalizedPosition halfUpperRightN = new NormalizedPosition(0.05f, 0.05f);
    NormalizedPosition halfLowerLeftN = new NormalizedPosition(-0.05f, -0.05f);

    Position halfUpperRight = model.toCameraCoordinates(halfUpperRightN);
    Position halfLowerLeft = model.toCameraCoordinates(halfLowerLeftN);

    Assert.assertEquals(new Position(850, -3225), halfUpperRight);
    Assert.assertEquals(new Position(-850, -3775), halfLowerLeft);

    // Inverse functions

    Assert.assertEquals(halfUpperRightN, model.toNormalizedCoordinates(halfUpperRight));
    Assert.assertEquals(halfLowerLeftN, model.toNormalizedCoordinates(halfLowerLeft));

    // Check positions close to the edge

    NormalizedPosition nearTopLeftN = new NormalizedPosition(0.90f, 0.9f);
    NormalizedPosition nearBottomRightN = new NormalizedPosition(-0.9f, -0.9f);

    Position nearTopLeft = model.toCameraCoordinates(nearTopLeftN);
    Position nearBottomRight = model.toCameraCoordinates(nearBottomRightN);

    Assert.assertEquals(new Position(15300, 1450), nearTopLeft);
    Assert.assertEquals(new Position(-15300, -8450), nearBottomRight);

    // Inverse functions

    Assert.assertEquals(nearTopLeftN, model.toNormalizedCoordinates(nearTopLeft));
    Assert.assertEquals(nearBottomRightN, model.toNormalizedCoordinates(nearBottomRight));

    // Outside the normalized co-ord range

    Position outsideTopLeft = new Position(18700, 2550);
    Position outsideBottomLeft = new Position(-18700, -9550);

    // These should calculate to 1.1, 1.1 but NormalisedPosition applies min/max values of -1 and 1

    Assert.assertEquals(new NormalizedPosition(1.0f, 1.0f), model.toNormalizedCoordinates(outsideTopLeft));
    Assert.assertEquals(new NormalizedPosition(-1.0f, -1.0f), model.toNormalizedCoordinates(outsideBottomLeft));

  }

  @org.junit.Test
  public void testExtrapolate() {

    // Extrapolate

    double y = model.extrapolate(3, 1, 1, 2, 2);
    Assert.assertEquals(3, y, 0.0001);

    y = model.extrapolate(3, 1, 0, 2, 0.5);
    Assert.assertEquals(1, y, 0.0001);

    y = model.extrapolate(0, 1, 1, 2, 2);
    Assert.assertEquals(0, y, 0.0001);

    y = model.extrapolate(-2, 0, 0.5, 1, 1);
    Assert.assertEquals(-0.5, y, 0.0001);

    // Linear Interpolate
    y = model.extrapolate(1.5, 1, 1, 2, 2);
    Assert.assertEquals(1.5, y, 0.0001);

    y = model.extrapolate(1.5, 1, 0.5, 2, 1);
    Assert.assertEquals(0.75, y, 0.0001);

  }

  @org.junit.Test
  public void testAutoCalibration() {

    model.update(-17000, 17000, -9000, 2000);

    NormalizedPosition upperRightN = new NormalizedPosition(0.1f, 0.1f);
    NormalizedPosition lowerLeftN = new NormalizedPosition(-0.1f, -0.1f);

    Position upperRight = model.toCameraCoordinates(upperRightN);
    Position lowerLeft = model.toCameraCoordinates(lowerLeftN);

    // Clear model
    model.update(0, 0, 0, 0);

    List<NormalizedPosition> sceneMarkers = new ArrayList<>();
    List<Position> cameraPresets = new ArrayList<>();

    // empty list
    Assert.assertFalse(model.update(sceneMarkers, cameraPresets));

    // Centre
    sceneMarkers.add(new NormalizedPosition(0,0));
    cameraPresets.add(new Position(0,-3500));

    // too few points
    Assert.assertFalse(model.update(sceneMarkers, cameraPresets));

    // Upper-right
    sceneMarkers.add(upperRightN);
    cameraPresets.add(upperRight);

    // Lower-left
    sceneMarkers.add(lowerLeftN);
    cameraPresets.add(lowerLeft);

    // enough points
    Assert.assertTrue(model.update(sceneMarkers, cameraPresets));

    // Scene limits still correct
    checkSceneLimits();

  }

  @org.junit.Test
  public void testAutoCalibrationMonotonic() {

    model.update(-17000, 17000, -9000, 2000);

    NormalizedPosition upperRightN = new NormalizedPosition(0.1f, 0.1f);
    NormalizedPosition lowerLeftN = new NormalizedPosition(-0.1f, -0.1f);

    List<NormalizedPosition> sceneMarkers = new ArrayList<>();
    List<Position> cameraPresets = new ArrayList<>();

    // Add markers
    sceneMarkers.add(new NormalizedPosition(-0.47f, -0.26f));
    cameraPresets.add(new Position(-2107, -855));

    sceneMarkers.add(new NormalizedPosition(-0.18f,-0.00f));
    cameraPresets.add(new Position(-967, -621));

    sceneMarkers.add(new NormalizedPosition(0.64f, -0.09f));
    cameraPresets.add(new Position(2238, -643));

    sceneMarkers.add(new NormalizedPosition(-0.91f, 0.03f));
    cameraPresets.add(new Position(-3422, -348));

    sceneMarkers.add(new NormalizedPosition(0.90f, 0.06f));
    cameraPresets.add(new Position(3268, -471));

    // Update model
    Assert.assertTrue(model.update(sceneMarkers, cameraPresets));

    // Check interpolation and inverse
    NormalizedPosition nPos = new NormalizedPosition(-0.7f, 0);
    Position cameraPos = model.toCameraCoordinates(nPos);
    NormalizedPosition nPosInv = model.toNormalizedCoordinates(cameraPos);
    Assert.assertEquals(-2842, cameraPos.getX());
    Assert.assertTrue(Math.abs(nPos.getX() -  nPosInv.getX()) < 0.001);

    nPos = new NormalizedPosition(0, -0.6f);
    cameraPos = model.toCameraCoordinates(nPos);
    nPosInv = model.toNormalizedCoordinates(cameraPos);
    Assert.assertEquals(-1162, cameraPos.getY());
    Assert.assertTrue(Math.abs(nPos.getY() -  nPosInv.getY()) < 0.001);
  }

}
