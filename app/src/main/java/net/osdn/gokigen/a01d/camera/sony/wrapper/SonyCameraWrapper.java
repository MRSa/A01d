package net.osdn.gokigen.a01d.camera.sony.wrapper;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IFocusingModeNotify;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.operation.SonyCameraCaptureControl;
import net.osdn.gokigen.a01d.camera.sony.operation.SonyCameraFocusControl;
import net.osdn.gokigen.a01d.camera.sony.wrapper.connection.SonyCameraConnection;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.CameraEventObserver;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.ICameraChangeListener;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.ICameraEventObserver;
import net.osdn.gokigen.a01d.camera.sony.wrapper.eventlistener.ICameraStatusHolder;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public class SonyCameraWrapper implements ISonyCameraHolder, ISonyInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final Activity context;
    private final ICameraStatusReceiver provider;
    private ISonyCamera sonyCamera = null;
    private ICameraEventObserver eventObserver = null;
    private SonyLiveViewControl liveViewControl = null;
    private SonyCameraFocusControl focusControl = null;
    private SonyCameraCaptureControl captureControl = null;

    public SonyCameraWrapper(final Activity context, final ICameraStatusReceiver statusReceiver)
    {
        this.context = context;
        this.provider = statusReceiver;
    }

    @Override
    public void prepare()
    {
        Log.v(TAG, " prepare : " + sonyCamera.getFriendlyName() + " " + sonyCamera.getModelName());
        ISonyCameraApi sonyCameraApi = SonyCameraApi.newInstance(sonyCamera);
        eventObserver = CameraEventObserver.newInstance(context, sonyCameraApi);
        liveViewControl = new SonyLiveViewControl(sonyCameraApi);

        focusControl.setCameraApi(sonyCameraApi);
        captureControl.setCameraApi(sonyCameraApi);
    }

    @Override
    public void startEventWatch(@Nullable ICameraChangeListener listener)
    {
        try
        {
            if (eventObserver != null)
            {
                if (listener != null)
                {
                    eventObserver.setEventListener(listener);
                }
                eventObserver.activate();
                eventObserver.start();
                ICameraStatusHolder holder = eventObserver.getCameraStatusHolder();
                holder.getLiveviewStatus();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void detectedCamera(@NonNull ISonyCamera camera)
    {
        Log.v(TAG, "detectedCamera()");
        sonyCamera = camera;
    }

    @Override
    public ICameraConnection getSonyCameraConnection()
    {
        return (new SonyCameraConnection(context, provider, this));
    }

    @Override
    public ILiveViewControl getSonyLiveViewControl()
    {
        return (liveViewControl);
    }

    @Override
    public ILiveViewListener getSonyLiveViewListener()
    {
        return (liveViewControl.getSonyLiveViewListener());
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
        return null;
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
    public void injectDisplay(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator, @NonNull IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");

        focusControl = new SonyCameraFocusControl(frameDisplayer, indicator);
        captureControl = new SonyCameraCaptureControl(frameDisplayer, indicator);
    }
}
