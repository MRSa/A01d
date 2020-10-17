package net.osdn.gokigen.a01d.camera.nikon.wrapper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import net.osdn.gokigen.a01d.camera.nikon.operation.NikonCaptureControl;
import net.osdn.gokigen.a01d.camera.nikon.operation.NikonFocusingControl;
import net.osdn.gokigen.a01d.camera.nikon.operation.NikonZoomLensControl;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.connection.NikonConnection;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.hardware.NikonCameraInformation;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview.NikonLiveViewControl;
import net.osdn.gokigen.a01d.camera.nikon.wrapper.status.NikonStatusChecker;
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpAsyncResponseReceiver;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.status.IPtpIpRunModeHolder;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.status.PtpIpRunMode;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public class NikonInterfaceProvider implements IPtpIpInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int ASYNC_RESPONSE_PORT = 15741;
    private static final int CONTROL_PORT = 15740;
    private static final int EVENT_PORT = 15740;
    private static final String CAMERA_IP = "192.168.1.1";

    private final Activity activity;
    private final PtpIpRunMode runmode;
    private final NikonCameraInformation cameraInformation;
    private NikonCaptureControl captureControl;
    private NikonFocusingControl focusingControl;
    private NikonConnection canonConnection;
    private PtpIpCommandPublisher commandPublisher;
    private NikonLiveViewControl liveViewControl;
    private PtpIpAsyncResponseReceiver asyncReceiver;
    private NikonZoomLensControl zoomControl;
    private NikonStatusChecker statusChecker;
    private ICameraStatusUpdateNotify statusListener;
    private IInformationReceiver informationReceiver;

    public NikonInterfaceProvider(@NonNull AppCompatActivity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        this.activity = context;
        commandPublisher = new PtpIpCommandPublisher(CAMERA_IP, CONTROL_PORT, true, false);
        liveViewControl = new NikonLiveViewControl(context, this, 20);
        asyncReceiver = new PtpIpAsyncResponseReceiver(CAMERA_IP, ASYNC_RESPONSE_PORT);
        statusChecker = new NikonStatusChecker(activity, commandPublisher, CAMERA_IP, EVENT_PORT);
        canonConnection = new NikonConnection(context, provider, this, statusChecker);
        cameraInformation = new NikonCameraInformation();
        zoomControl = new NikonZoomLensControl();
        this.statusListener = statusListener;
        this.runmode = new PtpIpRunMode();
        this.informationReceiver = informationReceiver;
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        captureControl = new NikonCaptureControl(commandPublisher, frameDisplayer);
        focusingControl = new NikonFocusingControl(activity, commandPublisher, frameDisplayer, indicator);
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
