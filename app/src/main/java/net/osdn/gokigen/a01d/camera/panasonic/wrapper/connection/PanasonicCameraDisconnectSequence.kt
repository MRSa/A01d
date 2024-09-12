package net.osdn.gokigen.a01d.camera.panasonic.wrapper.connection

import android.util.Log

class PanasonicCameraDisconnectSequence(private val isOff: Boolean = false) : Runnable
{
    override fun run()
    {
        // ----- カメラをPowerOffして接続を切る
        Log.v(TAG, "PanasonicCameraDisconnectSequence : $isOff")
    }
    companion object
    {
        private val TAG = PanasonicSsdpClient::class.java.simpleName
    }
}
