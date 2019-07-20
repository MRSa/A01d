package net.osdn.gokigen.a01d.camera;

import net.osdn.gokigen.a01d.camera.fujix.IFujiXInterfaceProvider;
import net.osdn.gokigen.a01d.camera.olympus.wrapper.IOlympusLiveViewListener;
import net.osdn.gokigen.a01d.camera.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.a01d.camera.panasonic.IPanasonicInterfaceProvider;
import net.osdn.gokigen.a01d.camera.ricohgr2.IRicohGr2InterfaceProvider;
import net.osdn.gokigen.a01d.camera.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.a01d.liveview.ICameraStatusUpdateNotify;

import androidx.annotation.NonNull;

/**
 *
 */
public interface IInterfaceProvider
{
    IOlympusInterfaceProvider getOlympusInterface();
    IOlympusLiveViewListener getOlympusLiveViewListener();

    ISonyInterfaceProvider getSonyInterface();
    IRicohGr2InterfaceProvider getRicohGr2Infterface();
    IFujiXInterfaceProvider getFujiXInterface();
    IPanasonicInterfaceProvider getPanasonicInterface();

    void setUpdateReceiver(@NonNull ICameraStatusUpdateNotify receiver);

    ICameraConnection.CameraConnectionMethod getCammeraConnectionMethod();
}
