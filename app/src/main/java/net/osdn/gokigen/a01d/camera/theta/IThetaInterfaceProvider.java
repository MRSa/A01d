package net.osdn.gokigen.a01d.camera.theta;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public interface IThetaInterfaceProvider
{
    ICameraConnection getCanonCameraConnection();
    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    /*
    IFujiXRunModeHolder getRunModeHolder();
    IFujiXCommandCallback getStatusHolder();
    IFujiXCommandPublisher getCommandPublisher();
    IFujiXCommunication getLiveviewCommunication();
    IFujiXCommunication getAsyncEventCommunication();
    IFujiXCommunication getCommandCommunication();
     */


    ICameraStatusWatcher getStatusWatcher();
    ICameraStatusUpdateNotify getStatusListener();

    ICameraStatusWatcher getCameraStatusWatcher();
    ICameraStatus getCameraStatusListHolder();

}
