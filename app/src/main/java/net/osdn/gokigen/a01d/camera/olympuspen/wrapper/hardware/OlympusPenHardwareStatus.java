package net.osdn.gokigen.a01d.camera.olympuspen.wrapper.hardware;


import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;

import java.util.Map;

public class OlympusPenHardwareStatus implements ICameraHardwareStatus
{
    @Override
    public boolean isAvailableHardwareStatus()
    {
        return (false);
    }

    @Override
    public String getLensMountStatus()
    {
        return (null);
    }

    @Override
    public String getMediaMountStatus()
    {
        return (null);
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (0);
    }

    @Override
    public float getMaximumFocalLength()
    {
        return (0);
    }

    @Override
    public float getActualFocalLength()
    {
        return (0);
    }

    @Override
    public Map<String, Object> inquireHardwareInformation()
    {
        return (null);
    }

}
