package net.osdn.gokigen.a01d.camera.ricohgr2.wrapper;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IFocusingModeNotify;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.IRicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2CameraCaptureControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2CameraFocusControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.RicohGr2CameraZoomLensControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.connection.RicohGr2Connection;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

/**
 *
 *
 */
public class RicohGr2InterfaceProvider implements IRicohGr2InterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final Activity activity;
    private final ICameraStatusReceiver provider;
    private final RicohGr2Connection gr2Connection;
    private RicohGr2LiveViewControl liveViewControl;
    private RicohGr2CameraCaptureControl captureControl;
    private RicohGr2CameraZoomLensControl zoomControl;
    private RicohGr2CameraFocusControl focusControl;

    /**
     *
     *
     */
    public RicohGr2InterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        this.activity = context;
        this.provider = provider;
        gr2Connection = new RicohGr2Connection(context, provider);
        liveViewControl = new RicohGr2LiveViewControl(context);
        zoomControl = new RicohGr2CameraZoomLensControl();

    }

    /**
     *
     *
     */
    public void prepare()
    {
        // liveViewControl = new RicohGr2LiveViewControl();
    }

    /**
     *
     *
     */
    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");
        focusControl = new RicohGr2CameraFocusControl(activity, frameDisplayer, indicator);
        captureControl = new RicohGr2CameraCaptureControl(frameDisplayer);
    }

    /**
     *
     *
     */
    @Override
    public ICameraConnection getRicohGr2CameraConnection()
    {
        return (gr2Connection);
    }

    /**
     *
     *
     */
    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (liveViewControl);
    }

    /**
     *
     *
     */
    @Override
    public ILiveViewListener getLiveViewListener()
    {
        if (liveViewControl == null)
        {
            return (null);
        }
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
    public IDisplayInjector getDisplayInjector() {
        return (this);
    }
}
