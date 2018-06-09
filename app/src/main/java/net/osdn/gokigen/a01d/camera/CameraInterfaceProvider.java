package net.osdn.gokigen.a01d.camera;

import android.app.Activity;
import android.support.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.sony.wrapper.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.wrapper.SonyCameraWrapper;

public class CameraInterfaceProvider implements IInterfaceProvider
{
    private final OlympusInterfaceProvider olympus;
    private final SonyCameraWrapper sony;

    public CameraInterfaceProvider(@NonNull Activity context, @NonNull ICameraStatusReceiver provider)
    {
        olympus = new OlympusInterfaceProvider(context, provider);
        sony = new SonyCameraWrapper(context, provider);
    }

    @Override
    public IOlympusInterfaceProvider getOlympusInterface()
    {
        return (olympus);
    }

    @Override
    public IDisplayInjector getOlympusDisplayInjector()
    {
        return (olympus);
    }

    @Override
    public ISonyInterfaceProvider getSonyInterface()
    {
        return (sony);
    }
}
