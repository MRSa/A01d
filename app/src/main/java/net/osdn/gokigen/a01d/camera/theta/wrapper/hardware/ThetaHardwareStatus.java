package net.osdn.gokigen.a01d.camera.theta.wrapper.hardware;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;

import java.util.Map;

public class ThetaHardwareStatus implements ICameraHardwareStatus
{
    public void updateStatus()
    {
        //
    }

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
