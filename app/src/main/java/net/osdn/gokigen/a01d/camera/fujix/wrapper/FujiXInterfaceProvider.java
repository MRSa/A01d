package net.osdn.gokigen.a01d.camera.fujix.wrapper;

import android.app.Activity;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.IFocusingModeNotify;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.fujix.IFujiXInterfaceProvider;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.FujiXAsyncResponseReceiver;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.FujiXCommandIssuer;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandIssuer;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXConnection;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.liveview.FujiXLiveViewControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public class FujiXInterfaceProvider implements IFujiXInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int STREAM_PORT = 55742;
    private static final int ASYNC_RESPONSE_PORT = 55741;
    private static final int CONTROL_PORT = 55740;
    private static final String CAMERA_IP = "192.168.0.1";

    private final Activity activity;
    private FujiXConnection fujiXConnection;
    private FujiXCommandIssuer commandIssuer;
    private FujiXLiveViewControl liveViewControl;
    private FujiXAsyncResponseReceiver asyncReceiver;


    public FujiXInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        this.activity = context;
        commandIssuer = new FujiXCommandIssuer(CAMERA_IP, CONTROL_PORT);
        liveViewControl = new FujiXLiveViewControl(context, CAMERA_IP, STREAM_PORT);
        asyncReceiver = new FujiXAsyncResponseReceiver(CAMERA_IP, ASYNC_RESPONSE_PORT);
        fujiXConnection = new FujiXConnection(context, provider, this);
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {

    }

    @Override
    public ICameraConnection getFujiXCameraConnection()
    {
        return (fujiXConnection);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (liveViewControl);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewControl.getLiveViewListener());
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return null;
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return null;
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return null;
    }

    @Override
    public ICaptureControl getCaptureControl()
    {
        return null;
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        return null;
    }

    @Override
    public IFujiXCommandIssuer getCommandIssuer()
    {
        return (commandIssuer);
    }

    @Override
    public IFujiXCommunication getLiveviewCommunication()
    {
        return (liveViewControl);
    }

    @Override
    public IFujiXCommunication getAsyncEventCommunication()
    {
        return (asyncReceiver);
    }

    @Override
    public IFujiXCommunication getCommandCommunication()
    {
        return (commandIssuer);
    }

    @Override
    public void setAsyncEventReceiver(@NonNull IFujiXCommandCallback receiver)
    {
        asyncReceiver.setEventSubscriber(receiver);
    }

}
