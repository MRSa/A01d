package net.osdn.gokigen.a01d.camera.olympuspen.wrapper;

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
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympuspen.IOlympusPenInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympuspen.wrapper.connection.OlympusPenConnection;
import net.osdn.gokigen.a01d.camera.olympuspen.wrapper.hardware.OlympusPenHardwareStatus;
import net.osdn.gokigen.a01d.camera.olympuspen.wrapper.status.OlympusPenCameraStatusWatcher;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

/**
 *
 *
 */
public class OlympusPenInterfaceProvider implements IOlympusPenInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final OlympusPenConnection olympusPenConnection;
    private final OlympusPenHardwareStatus hardwareStatus;
    private final OlympusPenRunMode runMode;
    private final OlympusPenLiveViewControl liveViewControl;
    private final OlympusPenZoomLensControl zoomLensControl;
    private final OlympusPenCameraInformation cameraInformation;
    private final OlympusPenCameraStatusWatcher statusWatcher;
    private  OlympusPenFocusControl focusControl = null;
    private  OlympusPenCaptureControl captureControl = null;

    /**
     *
     *
     */
    public OlympusPenInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider, @NonNull CameraStatusListener statusListener)
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
        //this.activity = context;
        //this.provider = provider;
        olympusPenConnection = new OlympusPenConnection(context, provider);
        hardwareStatus = new OlympusPenHardwareStatus();
        statusWatcher = new OlympusPenCameraStatusWatcher();
        runMode = new OlympusPenRunMode();
        liveViewControl = new OlympusPenLiveViewControl(statusWatcher, statusListener);
        zoomLensControl = new OlympusPenZoomLensControl(hardwareStatus);
        cameraInformation = new OlympusPenCameraInformation();
    }

    public void prepare()
    {
        Log.v(TAG, "prepare()");
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        focusControl = new OlympusPenFocusControl(frameDisplayer, indicator);
        captureControl = new OlympusPenCaptureControl(frameDisplayer, indicator);
    }

    @Override
    public ICameraConnection getOlyCameraConnection()
    {
        return (olympusPenConnection);
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
    public IOlyCameraPropertyProvider getCameraPropertyProvider()
    {
        return (cameraInformation);
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
    public ICameraHardwareStatus getHardwareStatus()
    {
        return (hardwareStatus);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return (runMode);
    }
}
