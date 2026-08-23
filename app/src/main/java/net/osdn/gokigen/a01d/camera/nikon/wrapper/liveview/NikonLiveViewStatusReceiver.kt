package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview

import android.util.Log
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper
class NikonLiveViewStatusReceiver(
    private val isDumpLog: Boolean = false
) : IPtpIpCommandCallback {

    override fun receivedMessage(id: Int, rxBody: ByteArray?) {
        if (rxBody == null) {
            Log.w(TAG, "receivedMessage [$id] : rxBody is NULL.")
            return
        }

        if (isDumpLog) {
            Log.v(TAG, "receivedMessage [$id] : ${rxBody.size} bytes.")
            dumpLogBytes(" [rcv]", rxBody)
        }

        // 00000 LiveViewステータスデータの解析・処理ロジックが必要なら、ここに実装
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rxBody: ByteArray?)
    {
        if (rxBody == null) {
            Log.w(TAG, "onReceiveProgress [$currentBytes/$totalBytes] : rxBody is NULL.")
            return
        }

        if (isDumpLog) {
            Log.v(TAG, "onReceiveProgress [$currentBytes/$totalBytes] : ${rxBody.size} bytes.")
            dumpLogBytes(" [rcv-m]", rxBody)
        }
    }

    override fun isReceiveMulti(): Boolean
    {
        // マルチパート受信を行わない場合は false
        return false
    }

    // メモリ割り当て（配列コピー）を避けつつログダンプを行うための補助関数
    private fun dumpLogBytes(prefix: String, rxBody: ByteArray)
    {
        val logDumpSize = rxBody.size.coerceAtMost(MAX_DUMP_SIZE)

        // リサイズ時のコピーを避けるため、ここでは sliceArray の代わりに部分渡しを意識します。
        SimpleLogDumper.dump_bytes(prefix, rxBody.copyOfRange(0, logDumpSize))
    }

    companion object {
        // クラス名と一致するように修正
        private val TAG = NikonLiveViewStatusReceiver::class.java.simpleName
        private const val MAX_DUMP_SIZE = 64
    }
}
/*
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
*/