package net.osdn.gokigen.a01d.camera.panasonic.wrapper.connection;

public class PanasonicCameraDisconnectSequence implements Runnable
{
    //private final String TAG = this.toString();
    //private final boolean powerOff;

    PanasonicCameraDisconnectSequence(boolean isOff)
    {
        //this.powerOff = isOff;
    }

    @Override
    public void run()
    {
        // カメラをPowerOffして接続を切る
    }
}
