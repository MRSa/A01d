package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection;

import android.content.Context;

/**
 *   カメラの接続/切断
 *
 */
public interface IOlyCameraConnection
{
    // WIFI 接続系
    void startWatchWifiStatus(Context context);
    void stopWatchWifiStatus(Context context);
    boolean isWatchWifiStatus();

    /** カメラ接続系 **/
    void disconnect(final boolean powerOff);
    void connect();

}
