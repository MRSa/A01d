package net.osdn.gokigen.a01d.camera;

/**
 *  ズームレンズの状態
 *
 */

public interface IZoomLensControl
{
    boolean canZoom();
    void updateStatus();
    float getMaximumFocalLength();
    float getMinimumFocalLength();
    float getCurrentFocalLength();
    void driveZoomLens(float targetLength);
    void driveZoomLens(boolean isZoomIn);
    void moveInitialZoomPosition();
    boolean isDrivingZoomLens();

}
