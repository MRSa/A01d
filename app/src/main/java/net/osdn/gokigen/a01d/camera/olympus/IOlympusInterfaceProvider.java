package net.osdn.gokigen.a01d.camera.olympus;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;

/**
 *
 */
public interface IOlympusInterfaceProvider
{
    IOlyCameraConnection getOlyCameraConnection();
    ICameraHardwareStatus getHardwareStatus();
    IOlyCameraPropertyProvider getCameraPropertyProvider();
    ICameraRunMode getCameraRunMode();

}
