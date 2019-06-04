package net.osdn.gokigen.a01d.camera.fujix.wrapper;

import android.app.Activity;
import android.util.Log;

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
import net.osdn.gokigen.a01d.camera.fujix.operation.FujiXCaptureControl;
import net.osdn.gokigen.a01d.camera.fujix.operation.FujiXFocusingControl;
import net.osdn.gokigen.a01d.camera.fujix.operation.FujiXZoomControl;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.FujiXAsyncResponseReceiver;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.FujiXCommandIssuer;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandIssuer;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXConnection;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.liveview.FujiXLiveViewControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2CameraCaptureControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2CameraFocusControl;
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
    private FujiXZoomControl zoomControl;
    private FujiXCaptureControl captureControl;
    private FujiXFocusingControl focusingControl;


    public FujiXInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        this.activity = context;
        commandIssuer = new FujiXCommandIssuer(CAMERA_IP, CONTROL_PORT);
        liveViewControl = new FujiXLiveViewControl(context, CAMERA_IP, STREAM_PORT);
        asyncReceiver = new FujiXAsyncResponseReceiver(CAMERA_IP, ASYNC_RESPONSE_PORT);
        fujiXConnection = new FujiXConnection(context, provider, this);
        zoomControl = new FujiXZoomControl();
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        captureControl = new FujiXCaptureControl(commandIssuer, frameDisplayer);
        focusingControl = new FujiXFocusingControl(activity, commandIssuer, frameDisplayer, indicator);
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
        return (focusingControl);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return null;
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (zoomControl);
    }

    @Override
    public ICaptureControl getCaptureControl()
    {
        return (captureControl);
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        return (this);
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
