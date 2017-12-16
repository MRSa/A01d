package net.osdn.gokigen.a01d.camera.olympus.wrapper.connection.ble;


public interface ICameraPowerOn
{
    // カメラ起動指示
    void wakeup(PowerOnCameraCallback callback);

    // 実行終了時のコールバックのインタフェース
    interface PowerOnCameraCallback
    {
        void wakeupExecuted(boolean isExecute);
    }
}
