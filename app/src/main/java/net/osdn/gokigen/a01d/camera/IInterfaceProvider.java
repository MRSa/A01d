package net.osdn.gokigen.a01d.camera;

import net.osdn.gokigen.a01d.camera.sony.wrapper.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;

/**
 *
 */
public interface IInterfaceProvider
{
    IOlympusInterfaceProvider getOlympusInterface();
    IDisplayInjector getOlympusDisplayInjector();

    ISonyInterfaceProvider getSonyInterface();
}
