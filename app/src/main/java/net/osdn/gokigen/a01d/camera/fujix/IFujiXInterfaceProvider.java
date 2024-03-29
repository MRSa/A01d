package net.osdn.gokigen.a01d.camera.fujix;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.status.IFujiXRunModeHolder;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public interface IFujiXInterfaceProvider
{
    ICameraConnection getFujiXCameraConnection();
    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    IFujiXRunModeHolder getRunModeHolder();
    IFujiXCommandCallback getStatusHolder();
    IFujiXCommandPublisher getCommandPublisher();
    IFujiXCommunication getLiveviewCommunication();
    IFujiXCommunication getAsyncEventCommunication();
    IFujiXCommunication getCommandCommunication();
    ICameraStatusWatcher getStatusWatcher();
    ICameraStatusUpdateNotify getStatusListener();

    ICameraStatusWatcher getCameraStatusWatcher();
    ICameraStatus getCameraStatusListHolder();

    void setAsyncEventReceiver(@NonNull IFujiXCommandCallback receiver);
}
