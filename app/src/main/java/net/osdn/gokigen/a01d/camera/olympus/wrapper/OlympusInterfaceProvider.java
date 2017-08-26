package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;

import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.OlyCameraConnection;


/**
 *
 *
 */
public class OlympusInterfaceProvider implements IOlympusInterfaceProvider
{
    private final OlyCameraWrapper wrapper;
    private final OlyCameraConnection connection;

    public OlympusInterfaceProvider(Activity context, ICameraStatusReceiver provider)
    {
        this.wrapper = new OlyCameraWrapper(context);
        this.connection = new OlyCameraConnection(context, this.wrapper.getOLYCamera(), provider);

    }

    @Override
    public IOlyCameraConnection getOlyCameraConnection()
    {
        return (connection);
    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        return null;
    }

    @Override
    public IOlyCameraPropertyProvider getCameraPropertyProvider()
    {
        return null;
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        return null;
    }
}
