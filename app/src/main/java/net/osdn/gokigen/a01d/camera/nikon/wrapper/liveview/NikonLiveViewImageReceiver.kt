package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview

import android.util.Log
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

//  PTP/IPにおけるNikon LiveViewイメージレシーバー
class NikonLiveViewImageReceiver(
    private var callback: IPtpIpLiveViewImageCallback
) : IPtpIpCommandCallback
{
    private val isDumpLog = false

    @Volatile
    private var receivedTotalBytes = 0
    @Volatile
    private var receivedRemainBytes = 0
    @Volatile
    private var receivedFirstData = false

    // スレッドセーフ性を意識したバイナリバッファ
    private val byteStream = ByteArrayOutputStream()

    @Synchronized
    override fun receivedMessage(id: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, "NikonLiveViewImageReceiver: MSG BODY IS NULL. (ID:$id)")
            callback.onCompleted(null, null)
            return
        }

        if (isReceiveMulti) {
            receivedMessageMulti(id, rx_body)
        } else {
            receivedMessageSingle(id, rx_body)
        }
    }

    @Synchronized
    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rxBody: ByteArray?)
    {
        if (rxBody == null)
        {
            Log.v(TAG, "NikonLiveViewImageReceiver: MSG BODY IS NULL.")
            return
        }
        Log.v(TAG, "onReceiveProgress() $currentBytes/$totalBytes LENGTH: ${rxBody.size} bytes.")

        if (currentBytes > totalBytes && (currentBytes - totalBytes) < (32 + 14))
        {
            Log.v(TAG, "===== DO NOT RECEIVE RESPONSE MESSAGE YET. =====")
        }
        // 受信データからヘッダ解析および本体抽出
        cutHeader(rxBody)
    }

    private fun receivedMessageSingle(id: Int, rxBody: ByteArray)
    {
        try
        {
            Log.v(TAG, "receivedMessageSingle() : ${rxBody.size} bytes. id:$id")
            if (isDumpLog && rxBody.size > 64)
            {
                SimpleLogDumper.dump_bytes(" LV (FIRST) : ", rxBody.copyOfRange(0, 64))
                SimpleLogDumper.dump_bytes(" LV (-END-) : ", rxBody.copyOfRange(rxBody.size - 64, rxBody.size))
            }
            callback.onCompleted(rxBody, null)
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error in receivedMessageSingle", e)
            callback.onErrorOccurred(e)
        }
    }

    private fun receivedMessageMulti(id: Int, rxBody: ByteArray)
    {
        try {
            Log.v(TAG, "receivedMessageMulti() id[$id] size: ${rxBody.size} accumulated size: ${byteStream.size()}")

            // 累積された全データを引き渡して状態リセット
            val completeData = byteStream.toByteArray()
            callback.onCompleted(completeData, null)

            resetReceiverState()
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Error in receivedMessageMulti", e)
            callback.onErrorOccurred(e)
            resetReceiverState()
        }
    }

    private fun resetReceiverState()
    {
        receivedFirstData = false
        receivedRemainBytes = 0
        receivedTotalBytes = 0
        byteStream.reset()
    }

    private fun cutHeader(rxBody: ByteArray)
    {
        val length = rxBody.size
        var dataPosition = 0

        // --- 初回受信データの場合、ヘッダオフセットを取得
        if (!receivedFirstData)
        {
            receivedFirstData = true
            dataPosition = rxBody[0].toUByte().toInt()
            if (isDumpLog) {
                Log.v(TAG, "FIRST DATA POS: $dataPosition len: $length")
                if (length >= 32) {
                    SimpleLogDumper.dump_bytes(" [1stData]", rxBody.copyOfRange(0, 32))
                }
            }
        }
        else
        {
            // --- 過去のパケットから読み込み残りのデータ（ペロード）がある場合
            if (receivedRemainBytes > 0)
            {
                if (length < receivedRemainBytes)
                {
                    // 全体を書き込んでもまだ未完了の場合
                    byteStream.write(rxBody, 0, length)
                    receivedRemainBytes -= length
                    receivedTotalBytes += length
                    return
                }
                else
                {
                    // --- 残りのサイズ分だけ書き込み、次パケットへ進む
                    byteStream.write(rxBody, 0, receivedRemainBytes)
                    dataPosition = receivedRemainBytes
                    receivedTotalBytes += receivedRemainBytes
                    receivedRemainBytes = 0
                }
            }
        }

        // --- パケットヘッダ（4バイトのLittle-Endianサイズ）を解析しながら書き込み
        while (dataPosition <= length - 12)
        {
            // ByteBufferを使用して安全にリトルエンディアンのIntを取得
            val bodySize = ByteBuffer.wrap(rxBody, dataPosition, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .int

            if (bodySize <= 12)
            {
                Log.v(TAG, "BODY SIZE IS TOO SMALL: pos=$dataPosition, size=$bodySize")
                break
            }

            // バッファ長を超過している場合は残りを次回の受信へ繰り越す
            if (dataPosition + bodySize > length)
            {
                val copySize = length - (dataPosition + 12)
                if (copySize > 0) {
                    byteStream.write(rxBody, dataPosition + 12, copySize)
                    receivedTotalBytes += copySize
                }
                receivedRemainBytes = bodySize - 12 - copySize
                break
            }

            try
            {
                val writeSize = bodySize - 12
                byteStream.write(rxBody, dataPosition + 12, writeSize)
                dataPosition += bodySize
                receivedTotalBytes += writeSize
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Error writing byte stream at pos: $dataPosition, size: $bodySize", e)
                callback.onErrorOccurred(e)
                break
            }
        }
    }

    override fun isReceiveMulti(): Boolean {
        return true
    }

    companion object {
        private val TAG = NikonLiveViewImageReceiver::class.java.simpleName
    }
}
/*
class NikonLiveViewImageReceiver(private var callback: IPtpIpLiveViewImageCallback) : IPtpIpCommandCallback
{
    private val isDumpLog = false
    private var receivedTotalBytes = 0
    private var receivedRemainBytes = 0
    private var receivedFirstData = false
    private var byteStream = ByteArrayOutputStream()

    override fun receivedMessage(id: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " NikonLiveViewImageReceiver: MSG BODY IS NULL. (ID:$id)")
            callback.onCompleted(rx_body, null)
            return
        }
        if (isReceiveMulti)
        {
            receivedMessageMulti(id, rx_body)
        }
        else
        {
            receivedMessageSingle(id, rx_body)
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " NikonLiveViewImageReceiver: MSG BODY IS NULL.")
            return
        }
        Log.v(TAG, " onReceiveProgress() $currentBytes/$totalBytes LENGTH: ${rx_body.size} bytes.")
        if ((currentBytes > totalBytes)&&((currentBytes - totalBytes) < (32 + 14)))
        {
            // Operation Response Packet を受信していないとき...
            Log.v(TAG, " ===== DO NOT RECEIVE RESPONSE MESSAGE YET. =====")
        }

        // 受信したデータから、通信のヘッダ部分を削除する
        cutHeader(rx_body)
    }

    private fun receivedMessageSingle(id: Int, rx_body: ByteArray)
    {
        try
        {
            Log.v(TAG, "receivedMessage_single() : " + rx_body.size + " bytes. id:$id")
            if ((isDumpLog)&&(rx_body.size > 64))
            {
                SimpleLogDumper.dump_bytes(" LV (FIRST) : ", rx_body.copyOfRange(0, 64))
                SimpleLogDumper.dump_bytes(" LV (-END-) : ", rx_body.copyOfRange(rx_body.size - 64, rx_body.size))
            }
            callback.onCompleted(rx_body, null)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun receivedMessageMulti(id: Int, rx_body: ByteArray)
    {
        try
        {
            Log.v(TAG, " receivedMessage_multi()  id[$id] size : ${rx_body.size}  id:$id")

            callback.onCompleted(byteStream.toByteArray(), null)
            receivedFirstData = false
            receivedRemainBytes = 0
            receivedTotalBytes = 0
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
        var dataPosition = 0
        if (!receivedFirstData)
        {
            // データを最初に読んだとき。ヘッダ部分を読み飛ばす
            receivedFirstData = true
            dataPosition = rx_body[0].toUByte().toInt()
            if (isDumpLog)
            {
                Log.v(TAG, " FIRST DATA POS. : $dataPosition len: $length ");
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

    override fun isReceiveMulti(): Boolean
    {
        return (true)
    }

    companion object
    {
        private val TAG = "NikonLiveViewImageRcvr"
    }
}
*/
