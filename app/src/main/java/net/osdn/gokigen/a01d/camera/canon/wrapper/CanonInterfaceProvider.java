package net.osdn.gokigen.a01d.camera.canon.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

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
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

public class CanonInterfaceProvider implements IPtpIpInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private static final int ASYNC_RESPONSE_PORT = 15741;  // ??
    private static final int CONTROL_PORT = 15740;
    private static final int EVENT_PORT = 15740;
    //private static final String CAMERA_IP = "192.168.0.1";

    private final Activity activity;
    private final PtpIpRunMode runMode;
    private final CanonCameraInformation cameraInformation;
    private CanonCaptureControl captureControl;
    private CanonFocusingControl focusingControl;
    private final CanonConnection canonConnection;
    private final PtpIpCommandPublisher commandPublisher;
    private final CanonLiveViewControl liveViewControl;
    private final PtpIpAsyncResponseReceiver asyncReceiver;
    private final CanonZoomLensControl zoomControl;
    private final CanonStatusChecker statusChecker;
    private final ICameraStatusUpdateNotify statusListener;
    private final IInformationReceiver informationReceiver;

    public CanonInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        this.activity = context;

        String ipAddress;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            ipAddress = preferences.getString(IPreferencePropertyAccessor.CANON_HOST_IP, IPreferencePropertyAccessor.CANON_HOST_IP_DEFAULT_VALUE);
            if (ipAddress == null)
            {
                ipAddress = "192.168.0.1";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ipAddress = "192.168.0.1";
        }
        Log.v(TAG, " Canon IP : " + ipAddress);
        commandPublisher = new PtpIpCommandPublisher(ipAddress, CONTROL_PORT, false, false);
        liveViewControl = new CanonLiveViewControl(context, this, 10);  //
        asyncReceiver = new PtpIpAsyncResponseReceiver(ipAddress, ASYNC_RESPONSE_PORT);
        statusChecker = new CanonStatusChecker(context, commandPublisher, ipAddress, EVENT_PORT);
        canonConnection = new CanonConnection(context, provider, this, statusChecker);
        cameraInformation = new CanonCameraInformation();
        zoomControl = new CanonZoomLensControl(context, commandPublisher);
        this.statusListener = statusListener;
        this.runMode = new PtpIpRunMode();
        this.informationReceiver = informationReceiver;
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, " injectDisplay()");
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
        return (runMode);
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
