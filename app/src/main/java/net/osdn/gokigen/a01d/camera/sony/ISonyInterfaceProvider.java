package net.osdn.gokigen.a01d.camera.sony;

import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraInformation;
import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.IZoomLensControl;
import net.osdn.gokigen.a01d.camera.sony.wrapper.IDisplayInjector;
import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyApiService;
import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCameraApi;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

import java.util.List;

public interface ISonyInterfaceProvider
{
    ICameraConnection getSonyCameraConnection();
    ILiveViewControl getSonyLiveViewControl();
    ILiveViewListener getSonyLiveViewListener();
    IFocusingControl getFocusingControl();
    ICameraInformation getCameraInformation();
    IZoomLensControl getZoomLensControl();
    ICaptureControl getCaptureControl();
    IDisplayInjector getDisplayInjector();
    List<String> getApiCommands();
    ISonyCameraApi getCameraApi();
}
