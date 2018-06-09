package net.osdn.gokigen.a01d.liveview;

import net.osdn.gokigen.a01d.camera.ICameraConnection;

public interface IStatusViewDrawer
{
    void updateGridIcon();
    void updateConnectionStatus(ICameraConnection.CameraConnectionStatus connectionStatus);
    void updateStatusView(String message);
    void updateLiveViewScale(boolean isChangeScale);
    void startLiveView();
}
