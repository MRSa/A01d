package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;


/**
 *
 *
 */
public class OlympusInterfaceProvider implements IOlympusInterfaceProvider
{
    public OlympusInterfaceProvider()
    {

    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        return null;
    }

    @Override
    public IOlyCameraPropertyProvider getCameraPropertyProvider()
    {
        return null;
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return null;
    }
}
