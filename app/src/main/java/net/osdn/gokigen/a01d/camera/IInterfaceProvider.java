package net.osdn.gokigen.a01d.camera;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlympusLiveViewListener;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ricohgr2.IRicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;

/**
 *
 */
public interface IInterfaceProvider
{
    IOlympusInterfaceProvider getOlympusInterface();
    IOlympusLiveViewListener getOlympusLiveViewListener();

    ISonyInterfaceProvider getSonyInterface();
    IRicohGr2InterfaceProvider getRicohGr2Infterface();

    ICameraConnection.CameraConnectionMethod getCammeraConnectionMethod();
}
