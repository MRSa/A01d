package net.osdn.gokigen.a01d.camera.sony.operation;

import android.util.Log;

import net.osdn.gokigen.a01d.camera.IZoomLensControl;

public class SonyCameraZoomLensControl implements IZoomLensControl
{
    private final String TAG = toString();

    public SonyCameraZoomLensControl()
    {
        Log.v(TAG, "SonyCameraZoomLensControl()");
    }

    @Override
    public boolean canZoom() {
        Log.v(TAG, "canZoom()");
        return false;
    }

    @Override
    public void updateStatus()
    {
        Log.v(TAG, "updateStatus()");
    }

    @Override
    public float getMaximumFocalLength()
    {
        Log.v(TAG, "getMaximumFocalLength()");
        return (0);
    }

    @Override
    public float getMinimumFocalLength()
    {
        Log.v(TAG, "getMinimumFocalLength()");
        return (0);
    }

    @Override
    public float getCurrentFocalLength()
    {
        Log.v(TAG, "getCurrentFocalLength()");
        return 0;
    }

    @Override
    public void driveZoomLens(float targetLength)
    {
        Log.v(TAG, "driveZoomLens() : " + targetLength);
    }

    @Override
    public void moveInitialZoomPosition()
    {
        Log.v(TAG, "moveInitialZoomPosition()");
    }

    @Override
    public boolean isDrivingZoomLens()
    {
        Log.v(TAG, "isDrivingZoomLens()");
        return (false);
    }
}
