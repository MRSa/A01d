package net.osdn.gokigen.a01d.camera.fujix;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommand;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandIssuer;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication;
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

    IFujiXCommandIssuer getCommandIssuer();
    IFujiXCommunication getLiveviewCommunication();
    IFujiXCommunication getAsyncEventCommunication();
    IFujiXCommunication getCommandCommunication();
    void setAsyncEventReceiver(@NonNull IFujiXCommandCallback receiver);
}
