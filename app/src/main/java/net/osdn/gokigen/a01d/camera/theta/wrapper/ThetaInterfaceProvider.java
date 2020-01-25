package net.osdn.gokigen.a01d.camera.theta.wrapper;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.CameraStatusListener;
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
import net.osdn.gokigen.a01d.camera.theta.IThetaInterfaceProvider;
import net.osdn.gokigen.a01d.camera.theta.wrapper.connection.ThetaConnection;
import net.osdn.gokigen.a01d.camera.theta.wrapper.hardware.ThetaHardwareStatus;
import net.osdn.gokigen.a01d.camera.theta.wrapper.status.ThetaCameraStatusWatcher;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

/**
 *
 *
 */
public class ThetaInterfaceProvider implements IThetaInterfaceProvider, IDisplayInjector, IThetaSessionIdNotifier, IThetaSessionIdProvider
{
    private final String TAG = toString();
    private final Activity activity;
    private final ThetaConnection thetaConnection;
    private final ThetaHardwareStatus hardwareStatus;
    private final ThetaRunMode runMode;
    private final ThetaLiveViewControl liveViewControl;
    private final ThetaZoomLensControl zoomLensControl;
    private final ThetaCameraInformation cameraInformation;
    private final ThetaCameraStatusWatcher statusWatcher;
    private final CameraStatusListener statusListener;
    private ThetaFocusControl focusControl = null;
    private ThetaCaptureControl captureControl = null;
    private String sessionId = "";

    /**
     *
     *
     */
    public ThetaInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull CameraStatusListener statusListener)
    {
/*
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int communicationTimeoutMs = 10000;  // デフォルトは 10000ms とする
        try
        {
            communicationTimeoutMs = Integer.parseInt(preferences.getString(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT, IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE)) * 1000;
            if (communicationTimeoutMs < 3000)
            {
                communicationTimeoutMs = 3000;  // 最小値は 3000msとする。
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
        this.activity = context;
        //this.provider = provider;
        this.statusListener = statusListener;
        thetaConnection = new ThetaConnection(context, provider, this);
        hardwareStatus = new ThetaHardwareStatus();
        statusWatcher = new ThetaCameraStatusWatcher();
        runMode = new ThetaRunMode();
        liveViewControl = new ThetaLiveViewControl(context, this);
        zoomLensControl = new ThetaZoomLensControl(hardwareStatus);
        cameraInformation = new ThetaCameraInformation();
    }

    public void prepare()
    {
        Log.v(TAG, "prepare()");
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        focusControl = new ThetaFocusControl(frameDisplayer);
        captureControl = new ThetaCaptureControl(activity, this, indicator, liveViewControl);
    }

    @Override
    public ICameraConnection getCameraConnection()
    {
        return (thetaConnection);
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
        return (focusControl);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return (cameraInformation);
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (zoomLensControl);
    }

    @Override
    public ICaptureControl getCaptureControl()
    {
        return (captureControl);
    }

    @Override
    public IDisplayInjector getDisplayInjector() {
        return (this);
    }

    @Override
    public ICameraStatusWatcher getStatusWatcher()
    {
        return (statusWatcher);
    }

    @Override
    public ICameraStatusUpdateNotify getStatusListener()
    {
        return (statusListener);
    }

    @Override
    public ICameraStatus getCameraStatusListHolder()
    {
        return (statusWatcher);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher() {
        return (statusWatcher);
    }

    @Override
    public void receivedSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId()
    {
        return (this.sessionId);
    }
}
