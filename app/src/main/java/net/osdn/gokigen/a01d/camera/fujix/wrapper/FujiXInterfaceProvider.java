package net.osdn.gokigen.a01d.camera.fujix.wrapper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatus;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICameraStatusWatcher;
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
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.FujiXCommandPublisher;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandCallback;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommandPublisher;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.connection.FujiXConnection;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.liveview.FujiXLiveViewControl;
import net.osdn.gokigen.a01d.camera.fujix.wrapper.status.FujiXStatusChecker;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
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
    private FujiXCommandPublisher commandPublisher;
    private FujiXLiveViewControl liveViewControl;
    private FujiXAsyncResponseReceiver asyncReceiver;
    private FujiXZoomControl zoomControl;
    private FujiXCaptureControl captureControl;
    private FujiXFocusingControl focusingControl;
    private FujiXStatusChecker statusChecker;
    private ICameraStatusUpdateNotify statusListener;

    public FujiXInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener)
    {
        this.activity = context;
        commandPublisher = new FujiXCommandPublisher(CAMERA_IP, CONTROL_PORT);
        liveViewControl = new FujiXLiveViewControl(context, CAMERA_IP, STREAM_PORT);
        asyncReceiver = new FujiXAsyncResponseReceiver(CAMERA_IP, ASYNC_RESPONSE_PORT);
        fujiXConnection = new FujiXConnection(context, provider, this);
        zoomControl = new FujiXZoomControl();
        statusChecker = new FujiXStatusChecker(activity, commandPublisher);
        this.statusListener = statusListener;
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        captureControl = new FujiXCaptureControl(commandPublisher, frameDisplayer);
        focusingControl = new FujiXFocusingControl(activity, commandPublisher, frameDisplayer, indicator);
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
    public IFujiXCommandPublisher getCommandPublisher()
    {
        return (commandPublisher);
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
        return (commandPublisher);
    }

    @Override
    public ICameraStatusWatcher getStatusWatcher()
    {
        return (statusChecker);
    }

    @Override
    public ICameraStatusUpdateNotify getStatusListener()
    {
        return (statusListener);
    }

    @Override
    public ICameraStatus getCameraStatus()
    {
        return (statusChecker);
    }

    @Override
    public void setAsyncEventReceiver(@NonNull IFujiXCommandCallback receiver)
    {
        asyncReceiver.setEventSubscriber(receiver);
    }

}
