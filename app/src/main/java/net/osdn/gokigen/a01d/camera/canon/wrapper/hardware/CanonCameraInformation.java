package net.osdn.gokigen.a01d.camera.canon.wrapper.hardware;

import net.osdn.gokigen.a01d.camera.ICameraInformation;

public class CanonCameraInformation implements ICameraInformation
{

    @Override
    public boolean isManualFocus()
    {
        return false;
    }

    @Override
    public boolean isElectricZoomLens()
    {
        return false;
    }

    @Override
    public boolean isExposureLocked()
    {
        return false;
    }
}
