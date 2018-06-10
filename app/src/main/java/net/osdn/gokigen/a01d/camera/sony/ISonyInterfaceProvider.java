package net.osdn.gokigen.a01d.camera.sony;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public interface ISonyInterfaceProvider
{
    ICameraConnection getSonyCameraConnection();
    ILiveViewControl getSonyLiveViewControl();
    ILiveViewListener getSonyLiveViewListener();

}
