package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview

import android.util.Log
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper

class NikonLiveViewStatusReceiver(private val isDumpLog: Boolean = false) : IPtpIpCommandCallback
{
    override fun receivedMessage(id: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " receivedMessage $id  is NULL.")
            return
        }

        if (isDumpLog)
        {
            Log.v(TAG, " receivedMessage() [$id] : ${rx_body.size} bytes. ");
            val logDumpSize = if (rx_body.size > 64) 64 else rx_body.size
            SimpleLogDumper.dump_bytes(" [rcv]", rx_body.copyOfRange(0, logDumpSize))
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " onReceiveProgress() : $currentBytes/$totalBytes is NULL")
            return
        }
        if (isDumpLog)
        {
            Log.v(TAG, " receivedMessage() [$currentBytes/$totalBytes] : ${rx_body.size} bytes. ");
            val logDumpSize = if (rx_body.size > 64) 64 else rx_body.size
            SimpleLogDumper.dump_bytes(" [rcv-m]", rx_body.copyOfRange(0, logDumpSize))
        }
    }

    override fun isReceiveMulti(): Boolean
    {
        Log.v(TAG, " isReceiveMulti() : false")
        return (false)
    }

    companion object
    {
        private val TAG = "NikonLiveViewImageReceiver"
    }
}
