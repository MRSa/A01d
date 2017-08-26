package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import java.util.Map;

/**
 *
 *
 */
public interface ICameraHardwareStatus
{
    String getLensMountStatus();
    String getMediaMountStatus();

    float getMinimumFocalLength();
    float getMaximumFocalLength();
    float getActualFocalLength();

    Map<String, Object> inquireHardwareInformation();
}
