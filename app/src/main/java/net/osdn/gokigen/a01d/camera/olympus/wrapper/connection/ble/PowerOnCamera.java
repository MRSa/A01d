package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;


import android.util.Log;

/**
 *   BLE経由でカメラの電源を入れるクラス
 *
 */
public class PowerOnCamera
{
    private final String TAG = toString();
    private final PowerOnCameraCallback callback;

    public PowerOnCamera(PowerOnCameraCallback callback)
    {
        Log.v(TAG, "PowerOnCamera()");

        this.callback = callback;
    }

    public void wakeup()
    {
        Log.v(TAG, "PowerOnCamera::wakeup()");


        // 今は暫定で返しちゃう
        callback.wakeupExecuted(true);
    }

    /**
     *   実行終了時のコールバックのインタフェース
     *
     */
    public interface PowerOnCameraCallback
    {
        void wakeupExecuted(boolean isExecute);
    }

}
