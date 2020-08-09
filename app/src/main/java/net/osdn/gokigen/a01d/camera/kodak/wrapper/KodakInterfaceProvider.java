package net.osdn.gokigen.a01d.camera.kodak.wrapper;

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
import net.osdn.gokigen.a01d.camera.kodak.IKodakInterfaceProvider;
import net.osdn.gokigen.a01d.camera.kodak.operation.KodakCaptureControl;
import net.osdn.gokigen.a01d.camera.kodak.operation.KodakFocusingControl;
import net.osdn.gokigen.a01d.camera.kodak.operation.KodakZoomLensControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommunication;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.KodakCommandCommunicator;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.connection.KodakConnection;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.hardware.KodakCameraInformation;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.liveview.KodakLiveViewControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.status.IKodakRunModeHolder;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.status.KodakRunMode;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.status.KodakStatusChecker;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_COMMAND_PORT;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_COMMAND_PORT_DEFAULT_VALUE;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_HOST_IP;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_HOST_IP_DEFAULT_VALUE;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_LIVEVIEW_PORT;
import static net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor.KODAK_LIVEVIEW_PORT_DEFAULT_VALUE;

public class KodakInterfaceProvider implements IKodakInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();

    private final Activity activity;
    private final KodakRunMode runmode;
    private final KodakCameraInformation cameraInformation;
    private KodakCaptureControl captureControl;
    private KodakFocusingControl focusingControl;
    private KodakConnection canonConnection;
    private KodakCommandCommunicator commandPublisher;
    private KodakLiveViewControl liveViewControl;
    private KodakZoomLensControl zoomControl;
    private KodakStatusChecker statusChecker;
    private ICameraStatusUpdateNotify statusListener;
    private IInformationReceiver informationReceiver;

    public KodakInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull ICameraStatusUpdateNotify statusListener, @NonNull IInformationReceiver informationReceiver)
    {
        String ipAddress;
        String controlPortStr;
        String liveviewPortStr;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            ipAddress = preferences.getString(KODAK_HOST_IP, KODAK_HOST_IP_DEFAULT_VALUE);
            controlPortStr = preferences.getString(KODAK_COMMAND_PORT, KODAK_COMMAND_PORT_DEFAULT_VALUE);
            liveviewPortStr = preferences.getString(KODAK_LIVEVIEW_PORT, KODAK_LIVEVIEW_PORT_DEFAULT_VALUE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ipAddress = "172.16.0.254";
            controlPortStr = "9175";
            liveviewPortStr = "9176";
        }
        int controlPort = parseInt(controlPortStr, 9175);
        int liveviewPort = parseInt(liveviewPortStr, 9176);

        this.activity = context;
        commandPublisher = new KodakCommandCommunicator(this, ipAddress, controlPort, true, false);
        liveViewControl = new KodakLiveViewControl(context, ipAddress, liveviewPort);
        statusChecker = new KodakStatusChecker();
        canonConnection = new KodakConnection(context, provider, this, statusChecker);
        cameraInformation = new KodakCameraInformation();
        zoomControl = new KodakZoomLensControl(commandPublisher);
        this.statusListener = statusListener;
        this.runmode = new KodakRunMode();
        this.informationReceiver = informationReceiver;
    }

    private int parseInt(@NonNull String key, int defaultValue)
    {
        int value = defaultValue;
        try
        {
            value = Integer.parseInt(key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (value);
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        captureControl = new KodakCaptureControl(commandPublisher, frameDisplayer);
        focusingControl = new KodakFocusingControl(commandPublisher, frameDisplayer);
        //focusingControl = new KodakFocusingControl(activity, commandPublisher, frameDisplayer, indicator);
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
    public IKodakRunModeHolder getRunModeHolder()
    {
        return (runmode);
    }

    @Override
    public IKodakCommandCallback getStatusHolder() {
        return (statusChecker);
    }

    @Override
    public IKodakCommandPublisher getCommandPublisher()
    {
        return (commandPublisher);
    }

    @Override
    public IKodakCommunication getCommandCommunication() {
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
    public String getStringFromResource(int resId)
    {
        return (activity.getString(resId));
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



}
