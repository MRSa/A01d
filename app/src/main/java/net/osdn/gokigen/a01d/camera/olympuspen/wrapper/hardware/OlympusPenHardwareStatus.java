package net.osdn.gokigen.a01d.camera.olympuspen.wrapper.hardware;


import android.util.Log;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;

import java.util.Map;

public class OlympusPenHardwareStatus implements ICameraHardwareStatus
{
    private final String TAG = toString();

    public void updateStatus()
    {
        Log.v(TAG, "updateStatus()");
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
        Log.v(TAG, "getMinimumFocalLength()");
        return (0);
    }

    @Override
    public float getMaximumFocalLength()
    {
        Log.v(TAG, "getMaximumFocalLength()");
        return (0);
    }

    @Override
    public float getActualFocalLength()
    {
        Log.v(TAG, "getActualFocalLength()");
        return (0);
    }

    @Override
    public Map<String, Object> inquireHardwareInformation()
    {
        return (null);
    }

}
