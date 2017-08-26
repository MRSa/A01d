package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection;

import android.content.Context;

/**
 *   カメラの接続/切断
 *
 */
public interface IOlyCameraConnection
{
    enum CameraConnectionStatus
    {
        UNKNOWN,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    // WIFI 接続系
    void startWatchWifiStatus(Context context);
    void stopWatchWifiStatus(Context context);

    /** カメラ接続系 **/
    void disconnect(final boolean powerOff);
    void connect();

    /** 接続状態 **/
    CameraConnectionStatus getConnectionStatus();
    void forceUpdateConnectionStatus(CameraConnectionStatus status);

}
