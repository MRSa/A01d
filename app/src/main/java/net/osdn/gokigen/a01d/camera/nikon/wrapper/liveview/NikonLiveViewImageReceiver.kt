package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview

import android.util.Log
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper
import java.io.ByteArrayOutputStream
import java.util.*

class NikonLiveViewImageReceiver(private var callback: IPtpIpLiveViewImageCallback) : IPtpIpCommandCallback
{
    private val isDumpLog = true
    private var received_total_bytes = 0
    private var received_remain_bytes = 0
    private var receivedFirstData = false
    private var byteStream = ByteArrayOutputStream()

    override fun receivedMessage(id: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " MSG BODY IS NULL.")
            return
        }
        if (isReceiveMulti)
        {
            receivedMessage_multi(id, rx_body)
        }
        else
        {
            receivedMessage_single(id, rx_body)
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " MSG BODY IS NULL.")
            return
        }
        Log.v(TAG, " onReceiveProgress() $currentBytes/$totalBytes LENGTH: ${rx_body.size} bytes.")

        // 受信したデータから、通信のヘッダ部分を削除する
        cutHeader(rx_body)
    }

    private fun receivedMessage_single(id: Int, rx_body: ByteArray)
    {
        try
        {
            Log.v(TAG, "receivedMessage_single() : " + rx_body.size + " bytes.")
            if ((isDumpLog)&&(rx_body.size > 64))
            {
                SimpleLogDumper.dump_bytes(" LV (FIRST) : ", Arrays.copyOfRange(rx_body, 0, 64))
                SimpleLogDumper.dump_bytes(" LV (-END-) : ", Arrays.copyOfRange(rx_body, rx_body.size - 64, rx_body.size))
            }
            callback.onCompleted(rx_body, null)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun receivedMessage_multi(id: Int, rx_body: ByteArray)
    {
        try
        {
            Log.v(TAG, " receivedMessage_multi()  id[$id] size : ${rx_body.size} ")

            callback.onCompleted(byteStream.toByteArray(), null)
            receivedFirstData = false
            received_remain_bytes = 0
            received_total_bytes = 0
            byteStream.reset()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            run { callback.onErrorOccurred(e) }
        }
    }

    private fun cutHeader(rx_body: ByteArray)
    {
        val length = rx_body.size
        var data_position = 0
        if (!receivedFirstData)
        {
            // データを最初に読んだとき。ヘッダ部分を読み飛ばす
            receivedFirstData = true
            data_position = rx_body[0].toUByte().toInt()
            if (isDumpLog)
            {
                Log.v(TAG, " FIRST DATA POS. : $data_position len: $length ");
                SimpleLogDumper.dump_bytes(" [sXXs]", Arrays.copyOfRange(rx_body, 0, (32)));
            }
        }
        else
        {
            // 2回目以降の受信データ
            if (received_remain_bytes > 0)
            {
                // データの読み込みが途中だった場合...
                if (length < received_remain_bytes)
                {
                    // 全部コピーする、足りないバイト数は残す
                    received_remain_bytes = received_remain_bytes - length
                    received_total_bytes = received_total_bytes + rx_body.size
                    byteStream.write(rx_body, 0, rx_body.size)
                    return
                }
                else
                {
                    byteStream.write(rx_body, data_position, received_remain_bytes)
                    data_position = received_remain_bytes
                    received_remain_bytes = 0
                }
            }
        }
        while (data_position <= length - 12)
        {
            val body_size = (rx_body[data_position].toUByte()).toInt() + ((rx_body[data_position + 1].toUByte()).toInt() * 256) + ((rx_body[data_position + 2].toUByte()).toInt() * 256 * 256) + ((rx_body[data_position + 3].toUByte()).toInt() * 256 * 256 * 256)

            Log.v(TAG, " XX body_size : ${body_size} [$data_position] ($length)  aa: ${rx_body[data_position].toUByte().toInt()}  ${rx_body[data_position + 1].toUByte().toInt()} + ${rx_body[data_position + 2].toUByte().toInt()}")
            SimpleLogDumper.dump_bytes("XX", Arrays.copyOfRange(rx_body, 0, 32))
            if (body_size <= 12)
            {
                Log.v(TAG, " ----- BODY SIZE IS SMALL : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] " + rx_body.size + " ")
                break
            }

            // 受信データ(のヘッダ部分)をダンプする
            Log.v(TAG, " RX DATA : " + data_position + " (" + body_size + ") [" + received_remain_bytes + "] (" + received_total_bytes + ")");
            if (data_position + body_size > length)
            {
                // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                val copysize = length - (data_position + 12)
                byteStream.write(rx_body, data_position + 12, copysize)
                received_remain_bytes = body_size - copysize - 12 // マイナス12は、ヘッダ分
                received_total_bytes = received_total_bytes + copysize
                //Log.v(TAG, " ----- copy : " + (data_position + (12)) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                break
            }
            try
            {
                byteStream.write(rx_body, data_position + 12, body_size - 12)
                data_position = data_position + body_size
                received_total_bytes = received_total_bytes + 12
                //Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - (12)) + " remain : " + received_remain_bytes);
            }
            catch (e: Exception)
            {
                Log.v(TAG, "  pos : $data_position  size : $body_size length : $length")
                e.printStackTrace()
            }
        }
    }

    override fun isReceiveMulti(): Boolean
    {
        return (true)
    }

    companion object
    {
        private val TAG = "NikonLiveViewImageReceiver"
    }
}