package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;
import android.os.Build;

import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.IFocusingModeNotify;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.OlyCameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.ICameraPowerOn;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.PowerOnCamera;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble.PowerOnCameraLP;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.OlyCameraPropertyProxy;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;


/**
 *
 *
 */
public class OlympusInterfaceProvider implements IOlympusInterfaceProvider, IDisplayInjector
{
    private final OlyCameraWrapper wrapper;
    private final OlyCameraConnection connection;
    private final OlyCameraPropertyProxy propertyProxy;
    private final OlyCameraHardwareStatus hardwareStatus;
    private final OLYCameraPropertyListenerImpl propertyListener;
    private final OlyCameraZoomLensControl zoomLensControl;
    private final ICameraPowerOn cameraPowerOn;
    private OlyCameraFocusControl focusControl = null;
    private OlyCameraCaptureControl captureControl = null;

    public OlympusInterfaceProvider(Activity context, ICameraStatusReceiver provider)
    {
        this.wrapper = new OlyCameraWrapper(context);
        this.connection = new OlyCameraConnection(context, this.wrapper.getOLYCamera(), provider);
        this.propertyProxy = new OlyCameraPropertyProxy(this.wrapper.getOLYCamera());
        this.hardwareStatus = new OlyCameraHardwareStatus(this.wrapper.getOLYCamera());
        this.propertyListener = new OLYCameraPropertyListenerImpl(this.wrapper.getOLYCamera());
        this.zoomLensControl = new OlyCameraZoomLensControl(context, this.wrapper.getOLYCamera());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            this.cameraPowerOn = new PowerOnCameraLP(context, this.wrapper.getOLYCamera());
        }
        else
        {
            this.cameraPowerOn = new PowerOnCamera(context, this.wrapper.getOLYCamera());
        }
    }

    @Override
    public void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify)
    {
        focusControl = new OlyCameraFocusControl(wrapper, frameDisplayer, indicator);
        captureControl = new OlyCameraCaptureControl (wrapper, frameDisplayer, indicator);
        propertyListener.setFocusingControl(focusingModeNotify);
    }

    @Override
    public ICameraPowerOn getCameraPowerOn()
    {
        return (cameraPowerOn);
    }

    @Override
    public ICameraConnection getOlyCameraConnection()
    {
        return (connection);
    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        return (hardwareStatus);
    }

    @Override
    public IOlyCameraPropertyProvider getCameraPropertyProvider()
    {
        return (propertyProxy);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return (wrapper);
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        return (zoomLensControl);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        return (wrapper);
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return (focusControl);
    }

    @Override
    public ICaptureControl getCaptureControl() {
        return (captureControl);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        return (propertyListener);
    }

    @Override
    public IOlympusLiveViewListener getLiveViewListener()
    {
       return (wrapper);
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        return (this);
    }

}
