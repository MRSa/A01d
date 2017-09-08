package net.osdn.gokigen.a01d.liveview;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;

public interface IStatusViewDrawer
{
    void updateGridIcon();
    void updateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus connectionStatus);
    void updateStatusView(String message);
    void updateLiveViewScale(boolean isChangeScale);
    void startLiveView();
}
