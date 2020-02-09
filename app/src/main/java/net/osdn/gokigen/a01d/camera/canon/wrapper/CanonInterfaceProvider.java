package net.osdn.gokigen.a01d.camera.canon.wrapper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.IInformationReceiver;
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
import net.osdn.gokigen.a01d.camera.canon.operation.CanonCaptureControl;
import net.osdn.gokigen.a01d.camera.canon.operation.CanonFocusingControl;
import net.osdn.gokigen.a01d.camera.canon.wrapper.connection.CanonConnection;
import net.osdn.gokigen.a01d.camera.canon.operation.CanonZoomLensControl;
import net.osdn.gokigen.a01d.camera.canon.wrapper.hardware.CanonCameraInformation;
import net.osdn.gokigen.a01d.camera.canon.wrapper.liveview.CanonLiveViewControl;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpAsyncResponseReceiver;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.status.IPtpIpRunModeHolder;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.status.PtpIpRunMode;
import net.osdn.gokigen.a01d.camera.canon.wrapper.status.CanonStatusChecker;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public class CanonInterfaceProvider implements IPtpIpInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int STREAM_PORT = 15742;   // ??
    private static final int ASYNC_RESPONSE_PORT = 15741;  // ??
    private static final int CONTROL_PORT = 15740;
    private static final int EVENT_PORT = 15740;
    private static final String CAMERA_IP = "192.168.0.1";

    private final Activity activity;
    private final PtpIpRunMode runmode;
    private final CanonCameraInformation cameraInformation;
    private CanonCaptureControl captureControl;
    private CanonFocusingControl focusingControl;
    private CanonConnection canonConnection;
    private PtpIpCommandPublisher commandPublisher;
    private CanonLiveViewControl liveViewControl;
    private PtpIpAsyncResponseReceiver asyncReceiver;
    private CanonZoomLensControl zoomControl;
    private CanonStatusChecker statusChecker;
    private ICameraStatusUpdateNotify statusListener;
    private IInformationReceiver informationReceiver;

    public CanonInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        this.activity = context;
        commandPublisher = new PtpIpCommandPublisher(CAMERA_IP, CONTROL_PORT);
        liveViewControl = new CanonLiveViewControl(context, CAMERA_IP, STREAM_PORT);
        asyncReceiver = new PtpIpAsyncResponseReceiver(CAMERA_IP, ASYNC_RESPONSE_PORT);
        statusChecker = new CanonStatusChecker(activity, commandPublisher, CAMERA_IP, EVENT_PORT);
        canonConnection = new CanonConnection(context, provider, this, statusChecker);
        cameraInformation = new CanonCameraInformation();
        zoomControl = new CanonZoomLensControl();
        this.statusListener = statusListener;
        this.runmode = new PtpIpRunMode();
        this.informationReceiver = informationReceiver;
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        captureControl = new CanonCaptureControl(commandPublisher, frameDisplayer);
        focusingControl = new CanonFocusingControl(activity, commandPublisher, frameDisplayer, indicator);
    }

    @Override
    public ICameraConnection getCameraConnection()
    {
        return (canonConnection);
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
        return (cameraInformation);
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
    public IPtpIpRunModeHolder getRunModeHolder()
    {
        return (runmode);
    }

    @Override
    public IPtpIpCommandCallback getStatusHolder() {
        return (statusChecker);
    }

    @Override
    public IPtpIpCommandPublisher getCommandPublisher()
    {
        return (commandPublisher);
    }

    @Override
    public IPtpIpCommunication getLiveviewCommunication()
    {
        return (liveViewControl);
    }

    @Override
    public IPtpIpCommunication getAsyncEventCommunication()
    {
        return (asyncReceiver);
    }

    @Override
    public IPtpIpCommunication getCommandCommunication()
    {
        return (commandPublisher);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher()
    {
        return (statusChecker);
    }

    @Override
    public ICameraStatusUpdateNotify getStatusListener()
    {
        return (statusListener);
    }

    @Override
    public ICameraStatus getCameraStatusListHolder()
    {
        return (statusChecker);
    }

    @Override
    public IInformationReceiver getInformationReceiver()
    {
        // ちょっとこの引き回しは気持ちがよくない...
        return (informationReceiver);
    }

    @Override
    public ICameraStatusWatcher getStatusWatcher()
    {
        return (statusChecker);
    }

    @Override
    public void setAsyncEventReceiver(@NonNull IPtpIpCommandCallback receiver)
    {
        asyncReceiver.setEventSubscriber(receiver);
    }

}
