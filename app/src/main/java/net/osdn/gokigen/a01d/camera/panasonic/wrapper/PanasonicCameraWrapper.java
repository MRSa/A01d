package net.osdn.gokigen.a01d.camera.panasonic.wrapper;

import android.app.Activity;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICameraChangeListener;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.IFocusingModeNotify;
import net.osdn.gokigen.a01d.camera.panasonic.IPanasonicInterfaceProvider;
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraCaptureControl;
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraFocusControl;
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraZoomLensControl;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.connection.PanasonicCameraConnection;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener.CameraEventObserver;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener.ICameraEventObserver;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener.ICameraStatusHolder;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PanasonicCameraWrapper implements IPanasonicCameraHolder, IPanasonicInterfaceProvider, IDisplayInjector
{
    private final String TAG = toString();
    private final Activity context;
    private final ICameraStatusReceiver provider;
    private final ICameraChangeListener listener;
    private IPanasonicCamera panasonicCamera = null;
    private IPanasonicCameraApi panasonicCameraApi = null;
    private ICameraEventObserver eventObserver = null;
    private PanasonicLiveViewControl liveViewControl = null;
    private PanasonicCameraFocusControl focusControl = null;
    private PanasonicCameraCaptureControl captureControl = null;
    private PanasonicCameraZoomLensControl zoomControl = null;

    public PanasonicCameraWrapper(final Activity context, final ICameraStatusReceiver statusReceiver , final @NonNull ICameraChangeListener listener)
    {
        this.context = context;
        this.provider = statusReceiver;
        this.listener = listener;
    }

    @Override
    public void prepare()
    {
        Log.v(TAG, " prepare : " + panasonicCamera.getFriendlyName() + " " + panasonicCamera.getModelName());
        try
        {
            this.panasonicCameraApi = PanasonicCameraApi.newInstance(panasonicCamera);
            eventObserver = CameraEventObserver.newInstance(context, panasonicCameraApi);
            liveViewControl = new PanasonicLiveViewControl(panasonicCameraApi);

            focusControl.setCameraApi(panasonicCameraApi);
            captureControl.setCameraApi(panasonicCameraApi);
            zoomControl.setCameraApi(panasonicCameraApi);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startRecMode()
    {
        try {
            List<String> apiCommands = getApiCommands();
            int index = apiCommands.indexOf("startRecMode");
            if (index > 0)
            {
                // startRecMode発行
                Log.v(TAG, "----- THIS CAMERA NEEDS COMMAND 'startRecMode'.");
                panasonicCameraApi.startRecMode();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


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
    public void detectedCamera(@NonNull IPanasonicCamera camera)
    {
        Log.v(TAG, "detectedCamera()");
        panasonicCamera = camera;
    }

    @Override
    public ICameraConnection getPanasonicCameraConnection()
    {
        return (new PanasonicCameraConnection(context, provider, this, listener));
    }

    @Override
    public ILiveViewControl getPanasonicLiveViewControl()
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
    public List<String> getApiCommands()
    {
        List<String> availableApis = new ArrayList<>();
        try
        {
            String apiList = panasonicCameraApi.getAvailableApiList().getString("result");
            apiList = apiList.replace("[","").replace("]", "").replace("\"","");
            String[] apiListSplit = apiList.split(",");
            availableApis = Arrays.asList(apiListSplit);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (availableApis);
    }

    @Override
    public IPanasonicCameraApi getCameraApi()
    {
        return (panasonicCameraApi);
    }

    @Override
    public void injectDisplay(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator, @NonNull IFocusingModeNotify focusingModeNotify)
    {
        Log.v(TAG, "injectDisplay()");

        focusControl = new PanasonicCameraFocusControl(frameDisplayer, indicator);
        captureControl = new PanasonicCameraCaptureControl(frameDisplayer, indicator);
        zoomControl = new PanasonicCameraZoomLensControl();
    }
}
