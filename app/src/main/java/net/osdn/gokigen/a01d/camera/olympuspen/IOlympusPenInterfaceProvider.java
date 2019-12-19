package net.osdn.gokigen.a01d.camera.olympuspen;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

/**
 *
 */
public interface IOlympusPenInterfaceProvider
{
    ICameraConnection getOlyCameraConnection();
    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    ICameraStatus getCameraStatusListHolder();
    ICameraStatusWatcher getCameraStatusWatcher();
    //IPlaybackControl getPlaybackControl();

    ICameraHardwareStatus getHardwareStatus();
    ICameraRunMode getCameraRunMode();
}
