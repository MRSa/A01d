package net.osdn.gokigen.a01d.camera.olympus;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlympusLiveViewListener;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.ICameraPowerOn;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;

/**
 *
 */
public interface IOlympusInterfaceProvider
{
    ICameraPowerOn getCameraPowerOn();
    ICameraConnection getOlyCameraConnection();
    ICameraHardwareStatus getHardwareStatus();
    IOlyCameraPropertyProvider getCameraPropertyProvider();
    ICameraRunMode getCameraRunMode();
    IZoomLensControl getZoomLensControl();
    ILiveViewControl getLiveViewControl();
    IFocusingControl getFocusingControl();
    ICaptureControl getCaptureControl();
    ICameraInformation getCameraInformation();
    IDisplayInjector getDisplayInjector();

    IOlympusLiveViewListener getLiveViewListener();
}
