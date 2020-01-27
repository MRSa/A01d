package net.osdn.gokigen.a01d.camera.canon;


import androidx.annotation.NonNull;

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
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.status.IPtpIpRunModeHolder;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public interface ICanonInterfaceProvider
{
    ICameraConnection getCanonCameraConnection();
    ILiveViewControl getLiveViewControl();
    ILiveViewListener getLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();

    IPtpIpRunModeHolder getRunModeHolder();
    IPtpIpCommandCallback getStatusHolder();
    IPtpIpCommandPublisher getCommandPublisher();
    IPtpIpCommunication getLiveviewCommunication();
    IPtpIpCommunication getAsyncEventCommunication();
    IPtpIpCommunication getCommandCommunication();
    IInformationReceiver getInformationReceiver();

    ICameraStatusWatcher getStatusWatcher();
    ICameraStatusUpdateNotify getStatusListener();

    ICameraStatusWatcher getCameraStatusWatcher();
    ICameraStatus getCameraStatusListHolder();

    void setAsyncEventReceiver(@NonNull IPtpIpCommandCallback receiver);
}
