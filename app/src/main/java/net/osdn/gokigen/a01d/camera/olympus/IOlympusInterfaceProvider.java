package net.osdn.gokigen.a01d.camera.olympus;

import net.osdn.gokigen.a01d.camera.olympus.operation.ICaptureControl;
import net.osdn.gokigen.a01d.camera.olympus.operation.IFocusingControl;
import net.osdn.gokigen.a01d.camera.olympus.operation.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraInformation;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.ICameraPowerOn;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;

/**
 *
 */
public interface IOlympusInterfaceProvider
{
    ICameraPowerOn getCameraPowerOn();
    IOlyCameraConnection getOlyCameraConnection();
    ICameraHardwareStatus getHardwareStatus();
    IOlyCameraPropertyProvider getCameraPropertyProvider();
    ICameraRunMode getCameraRunMode();
    IZoomLensControl getZoomLensControl();
    ILiveViewControl getLiveViewControl();
    IFocusingControl getFocusingControl();
    ICaptureControl getCaptureControl();
    ICameraInformation getCameraInformation();
}
