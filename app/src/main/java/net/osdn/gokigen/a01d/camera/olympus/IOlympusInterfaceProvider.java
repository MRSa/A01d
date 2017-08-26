package net.osdn.gokigen.a01d.camera.olympus;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlyCameraPropertyProvider;

/**
 *
 */
public interface IOlympusInterfaceProvider
{
    ICameraHardwareStatus getHardwareStatus();
    IOlyCameraPropertyProvider getCameraPropertyProvider();
    ICameraRunMode getCameraRunMode();

}
