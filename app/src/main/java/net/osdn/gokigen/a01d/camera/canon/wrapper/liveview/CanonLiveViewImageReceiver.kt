package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview

import android.util.Log
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper
import java.io.ByteArrayOutputStream
import java.util.*

class CanonLiveViewImageReceiver(val callback: IPtpIpLiveViewImageCallback) : IPtpIpCommandCallback
{
    private val isDumpLog = false
    private val byteStream = ByteArrayOutputStream()

    private var receivedTotalBytes = 0
    private var receivedRemainBytes = 0
    private var receivedFirstData = false

    override fun receivedMessage(id: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " CanonLiveViewReceiver: MSG BODY IS NULL. (ID:$id)")
            callback.onCompleted(rx_body, null)
            return
        }
        receivedMultiMessage(id, rx_body)
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " CanonLiveViewReceiver: MSG BODY is NULL. ($currentBytes/$totalBytes)")
            callback.onCompleted(rx_body, null)
            return
        }
        if (isDumpLog)
        {
            Log.v(TAG, " CanonLiveViewReceiver::onReceiveProgress() $currentBytes/$totalBytes [size: ${rx_body.size}]")
        }
        parseReceivedBody(rx_body)
    }


    private fun parseReceivedBody(rx_body: ByteArray)
    {
        try
        {
            val length = rx_body.size
            var dataPosition = 0
            if (!receivedFirstData)
            {
                // データを最初に読んだとき。ヘッダ部分を読み飛ばす
                receivedFirstData = true
                dataPosition = rx_body[0].toUByte().toInt()
                if (isDumpLog)
                {
                    Log.v(TAG, " FIRST DATA POS. : $dataPosition len: $length ")
                    SimpleLogDumper.dump_bytes(" [1stData]", rx_body.copyOfRange(0, (32)))
                }
            }
            else
            {
                // 2回目以降の受信データ
                if (receivedRemainBytes > 0)
                {
                    // データの読み込みが途中だった場合...
                    if (length < receivedRemainBytes)
                    {
                        // 全部コピーする、足りないバイト数は残す
                        receivedRemainBytes -= length
                        receivedTotalBytes += rx_body.size
                        byteStream.write(rx_body, 0, rx_body.size)
                        return
                    }
                    else
                    {
                        byteStream.write(rx_body, dataPosition, receivedRemainBytes)
                        dataPosition = receivedRemainBytes
                        receivedRemainBytes = 0
                    }
                }
            }
            while (dataPosition <= length - 12)
            {
                val body_size = (rx_body[dataPosition].toUByte()).toInt() + ((rx_body[dataPosition + 1].toUByte()).toInt() * 256) + ((rx_body[dataPosition + 2].toUByte()).toInt() * 256 * 256) + ((rx_body[dataPosition + 3].toUByte()).toInt() * 256 * 256 * 256)
                //Log.v(TAG, " <> body_size : ${body_size} [$dataPosition] ($length)  aa: ${rx_body[dataPosition].toUByte().toInt()}  ${rx_body[dataPosition + 1].toUByte().toInt()} + ${rx_body[dataPosition + 2].toUByte().toInt()}")
                if (body_size <= 12)
                {
                    Log.v(TAG, " ----- BODY SIZE IS SMALL : " + dataPosition + " (" + body_size + ") [" + receivedRemainBytes + "] " + rx_body.size + " ")
                    break
                }

                //Log.v(TAG, " RX DATA : " + dataPosition + " (" + body_size + ") [" + receivedRemainBytes + "] (" + receivedTotalBytes + ")");
                if (dataPosition + body_size > length)
                {
                    // データがすべてバッファ内になかったときは、バッファすべてコピーして残ったサイズを記憶しておく。
                    val copysize = length - (dataPosition + 12)
                    byteStream.write(rx_body, dataPosition + 12, copysize)
                    receivedRemainBytes = body_size - copysize - 12 // マイナス12は、ヘッダ分
                    receivedTotalBytes += copysize
                    //Log.v(TAG, " ----- copy : " + (data_position + (12)) + " " + copysize + " remain : " + received_remain_bytes + "  body size : " + body_size);
                    break
                }
                try
                {
                    byteStream.write(rx_body, dataPosition + 12, body_size - 12)
                    dataPosition += body_size
                    receivedTotalBytes += 12
                    //Log.v(TAG, " --- COPY : " + (data_position + 12) + " " + (body_size - (12)) + " remain : " + received_remain_bytes);
                }
                catch (e: Exception)
                {
                    Log.v(TAG, "  pos : $dataPosition  size : $body_size length : $length")
                    e.printStackTrace()
                }
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }


    private fun receivedMultiMessage(id: Int, rx_body: ByteArray)
    {
        try
        {
            byteStream.write(rx_body, 0, rx_body.size)
            if (isDumpLog)
            {
                val thumbNail = byteStream.toByteArray()
                var dumpLength = thumbNail.size
                if (dumpLength > 256)
                {
                    dumpLength = 256
                }
                SimpleLogDumper.dump_bytes(" [--ID:$id(>)--]", Arrays.copyOfRange(thumbNail, 0, dumpLength))
                SimpleLogDumper.dump_bytes(" [-ID:$id(<)-]", Arrays.copyOfRange(thumbNail, thumbNail.size - dumpLength, thumbNail.size))
            }
            callback.onCompleted(byteStream.toByteArray(), null)
            receivedFirstData = false
            receivedRemainBytes = 0
            receivedTotalBytes = 0
            byteStream.reset()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callback.onErrorOccurred(e)
        }
    }



    override fun isReceiveMulti(): Boolean
    {
        return (true)
    }

    companion object
    {
        private val TAG = "CanonLiveViewReceiver"
    }

}