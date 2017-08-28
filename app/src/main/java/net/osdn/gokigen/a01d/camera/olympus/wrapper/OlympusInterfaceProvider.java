package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;

import net.osdn.gokigen.a01d.camera.olympus.IOlympusDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.operation.IFocusingControl;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.OlyCameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.IOlyCameraPropertyProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.OlyCameraPropertyProxy;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;


/**
 *
 *
 */
public class OlympusInterfaceProvider implements IOlympusInterfaceProvider, IOlympusDisplayInjector
{
    private final OlyCameraWrapper wrapper;
    private final OlyCameraConnection connection;
    private final OlyCameraPropertyProxy propertyProxy;
    private final OlyCameraHardwareStatus hardwareStatus;
    private OlyCameraFocusControl focusControl = null;

    public OlympusInterfaceProvider(Activity context, ICameraStatusReceiver provider)
    {
        this.wrapper = new OlyCameraWrapper(context);
        this.connection = new OlyCameraConnection(context, this.wrapper.getOLYCamera(), provider);
        this.propertyProxy = new OlyCameraPropertyProxy(this.wrapper.getOLYCamera());
        this.hardwareStatus = new OlyCameraHardwareStatus(this.wrapper.getOLYCamera());

    }

    @Override
    public void injectOlympusDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        focusControl = new OlyCameraFocusControl(wrapper, frameDisplayer, indicator);
    }

    @Override
    public IOlyCameraConnection getOlyCameraConnection()
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
    public ILiveViewControl getLiveViewControl()
    {
        return (wrapper);
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        return (focusControl);
    }
}
