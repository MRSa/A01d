package net.osdn.gokigen.a01d.camera.olympuspen.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

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
import net.osdn.gokigen.a01d.camera.olympuspen.IOlympusPenInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympuspen.wrapper.connection.OlympusPenConnection;
import net.osdn.gokigen.a01d.camera.olympuspen.wrapper.hardware.OlympusPenHardwareStatus;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

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

    /**
     *
     *
     */
    public OlympusPenInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
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
        runMode = new OlympusPenRunMode();
    }

    public void prepare()
    {
        Log.v(TAG, "prepare()");
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
    }

    @Override
    public ICameraConnection getOlyCameraConnection()
    {
        return (olympusPenConnection);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (null);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        return (null);
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return (null);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return (null);
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (null);
    }

    @Override
    public ICaptureControl getCaptureControl()
    {
        return (null);
    }

    @Override
    public IDisplayInjector getDisplayInjector() {
        return (this);
    }

    @Override
    public ICameraStatus getCameraStatusListHolder()
    {
        return (null);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher() {
        return (null);
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
