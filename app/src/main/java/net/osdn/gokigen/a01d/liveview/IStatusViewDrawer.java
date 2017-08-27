package net.osdn.gokigen.a01d.liveview;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.IOlyCameraConnection;

public interface IStatusViewDrawer
{
    void updateGridIcon();
    void updateConnectionStatus(IOlyCameraConnection.CameraConnectionStatus connectionStatus);
    void updateStatusView(String message);
    void startLiveView();

    /*
    void updateStatusView(String message);
    void updateFocusAssistStatus();
    void updateGridFrameStatus();
    void showFavoriteSettingDialog();

    void toggleTimerStatus();

    void toggleGpsTracking();
    void updateGpsTrackingStatus();

    IMessageDrawer getMessageDrawer();
*/
}
