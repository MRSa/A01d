package net.osdn.gokigen.a01d.camera.nikon.operation;

import net.osdn.gokigen.a01d.camera.IZoomLensControl;

public class NikonZoomLensControl implements IZoomLensControl
{
    public NikonZoomLensControl()
    {

    }

    @Override
    public boolean canZoom()
    {
        return (false);
    }

    @Override
    public void updateStatus()
    {

    }

    @Override
    public float getMaximumFocalLength()
    {
        return (0);
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (0);
    }

    @Override
    public float getCurrentFocalLength()
    {
        return (0);
    }

    @Override
    public void driveZoomLens(float targetLength)
    {

    }

    @Override
    public void driveZoomLens(boolean isZoomIn)
    {

    }

    @Override
    public void moveInitialZoomPosition()
    {

    }

    @Override
    public boolean isDrivingZoomLens()
    {
        return (false);
    }
}
