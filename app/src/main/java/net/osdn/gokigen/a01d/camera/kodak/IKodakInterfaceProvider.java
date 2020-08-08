package net.osdn.gokigen.a01d.camera.kodak;

import net.osdn.gokigen.a01d.IInformationReceiver;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommunication;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.status.IKodakRunModeHolder;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public interface IKodakInterfaceProvider
{
    ICameraConnection getCameraConnection();
    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    IKodakRunModeHolder getRunModeHolder();
    IKodakCommandCallback getStatusHolder();
    IKodakCommandPublisher getCommandPublisher();
    IKodakCommunication getCommandCommunication();
    IInformationReceiver getInformationReceiver();

    ICameraStatusWatcher getStatusWatcher();
    ICameraStatusUpdateNotify getStatusListener();

    ICameraStatusWatcher getCameraStatusWatcher();
    ICameraStatus getCameraStatusListHolder();

    String getStringFromResource(int resId);
}
