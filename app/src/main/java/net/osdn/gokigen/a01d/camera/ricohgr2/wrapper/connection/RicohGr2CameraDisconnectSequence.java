package net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.connection;

import android.util.Log;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

public class RicohGr2CameraDisconnectSequence  implements Runnable
{
    private final String TAG = this.toString();
    private final boolean powerOff;

    RicohGr2CameraDisconnectSequence(boolean isOff)
    {
        this.powerOff = isOff;
    }

    @Override
    public void run()
    {
        // カメラをPowerOffして接続を切る
        try
        {
            if (powerOff)
            {
                final String cameraPowerOffUrl = "http://192.168.0.1/v1/device/finish";
                final String postData = "";
                final int TIMEOUT_MS = 5000;
                String response = SimpleHttpClient.httpPost(cameraPowerOffUrl, postData, TIMEOUT_MS);
                Log.v(TAG, cameraPowerOffUrl + " " + response);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
