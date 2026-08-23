package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview

import android.util.Log
import android.os.Handler
import android.os.Looper
import net.osdn.gokigen.a01d.camera.ILiveViewControl
import net.osdn.gokigen.a01d.camera.nikon.wrapper.command.messages.specific.NikonLiveViewRequestMessage
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpResponseReceiver
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener


class NikonLiveViewControl(
    interfaceProvider: IPtpIpInterfaceProvider,
    private val delayMs: Int,
    private val delayScale: Int
) : ILiveViewControl,
    ILiveViewListener,
    IPtpIpCommunication,
    IPtpIpLiveViewImageCallback,
    IPtpIpCommandCallback {

    private val isDumpLog = false
    private val commandIssuer = interfaceProvider.commandPublisher
    private val imageReceiver = NikonLiveViewImageReceiver(this)
    //private val statusReceiver = NikonLiveViewStatusReceiver(isDumpLog = true)

    private var dataReceiver: IImageDataReceiver? = null
    private var liveViewIsReceiving = false

    // スレッドをブロックせずに遅延実行するためのHandler（必要に応じてCoroutine等に変更可）
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getLiveViewListener(): ILiveViewListener = this

    override fun changeLiveViewSize(size: String?) {
        Log.v(TAG, "changeLiveViewSize() : $size")
    }

    override fun startLiveView() {
        Log.v(TAG, "startLiveView()")
        try {
            liveViewIsReceiving = true
            commandIssuer.enqueueCommand(
                PtpIpCommandGeneric(
                    this,
                    IPtpIpMessages.SEQ_START_LIVEVIEW,
                    delayMs,
                    isDumpLog,
                    0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in startLiveView", e)
        }
    }

    override fun stopLiveView() {
        Log.v(TAG, "stopLiveView()")
        try {
            if (liveViewIsReceiving) {
                liveViewIsReceiving = false
                commandIssuer.enqueueCommand(
                    PtpIpCommandGeneric(
                        PtpIpResponseReceiver(null),
                        IPtpIpMessages.SEQ_STOP_LIVEVIEW,
                        delayMs,
                        isDumpLog,
                        0, 0x9202, 0, 0x00, 0x00, 0x00, 0x00
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in stopLiveView", e)
        }
    }

    override fun updateDigitalZoom() {
        Log.v(TAG, "updateDigitalZoom()")
    }

    override fun updateMagnifyingLiveViewScale(isChangeScale: Boolean) {
        Log.v(TAG, "updateMagnifyingLiveViewScale() : $isChangeScale")
    }

    override fun getMagnifyingLiveViewScale(): Float = 0.0f

    override fun getDigitalZoomScale(): Float = 0.0f

    override fun setCameraLiveImageView(target: IImageDataReceiver?) {
        Log.v(TAG, "setCameraLiveImageView()")
        dataReceiver = target
    }

    override fun connect(): Boolean {
        Log.v(TAG, "connect()")
        return true
    }

    override fun disconnect() {
        Log.v(TAG, "disconnect()")
        // Handlerに積まれた遅延タスクがあれば解除
        mainHandler.removeCallbacksAndMessages(null)
    }

    override fun onCompleted(data: ByteArray?, metadata: Map<String?, Any?>?) {
        try {
            if ((dataReceiver != null)&&(data != null)&&(data.isNotEmpty()))
            {
                val offset = searchJpegHeader(data)
                if (offset != -1 && offset < data.size) {
                    // IImageDataReceiver 側に setImageData(data, offset, length, metadata) のような
                    // オフセット指定メソッドを追加できれば、copyOfRange によるメモリ再割り当てを抑止できるが...
                    val jpegData = data.copyOfRange(offset, data.size)
                    dataReceiver?.setImageData(jpegData, metadata)
                } else {
                    Log.w(TAG, "JPEG header (0xFFD8) not found in frame data.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame onCompleted", e)
        }
        sendNextMessage()
    }

    private fun sendNextMessage() {
        val totalDelay = delayMs.toLong() * delayScale
        Log.v(TAG, "sendNextMessage(), delay : $totalDelay ms")

        mainHandler.postDelayed({
            try {
                if (commandIssuer.isExistCommandMessageQueue(IPtpIpMessages.SEQ_GET_VIEWFRAME) < 2) {
                    commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendNextMessage task", e)
            }
        }, totalDelay)
    }

    /**
     * JPEG先頭マーカー (0xFF, 0xD8) を検索する。
     * 見つからない場合は -1 を返す。
     */
    private fun searchJpegHeader(data: ByteArray): Int
    {
        if (data.size < 2) return -1

        // 先頭 4096 バイトまで安全に検索
        val limit = (data.size - 1).coerceAtMost(4096)
        for (pos in 0 until limit) {
            if (data[pos] == 0xFF.toByte() && data[pos + 1] == 0xD8.toByte()) {
                return pos
            }
        }
        return -1
    }

    override fun onErrorOccurred(e: Exception)
    {
        Log.e(TAG, "onErrorOccurred() : ${e.localizedMessage}", e)
    }

    override fun receivedMessage(id: Int, rxBody: ByteArray?) {
        if (rxBody == null) {
            Log.w(TAG, "receivedMessage() body is null.")
            return
        }
        Log.v(TAG, "receivedMessage() [id:$id]")

        try {
            if (rxBody.size < 10) {
                Log.w(TAG, "receivedMessage() : BODY LENGTH IS TOO SHORT. SEND RETRY MESSAGE")
                retrySendMessage(id)
                return
            }

            var responseCode = getShortLittleEndian(rxBody, 8)

            if (id == IPtpIpMessages.SEQ_CHECK_EVENT) {
                // 応答の末尾からレスポンスコードを取得
                responseCode = getShortLittleEndian(rxBody, rxBody.size - 6)
            }

            if (responseCode != 0x2001) {
                // NG応答を受信...同じコマンドを再送する
                Log.w(TAG, String.format("RECEIVED NG REPLY ID : %d, RESPONSE CODE : 0x%04x ", id, responseCode))
                retrySendMessage(id)
                return
            }

            Log.v(TAG, String.format("----- OK REPLY (ID : %d) ----- ", id))
            waitSleepAndExecute {
                when (id) {
                    IPtpIpMessages.SEQ_START_LIVEVIEW -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_READY, delayMs, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00))
                    IPtpIpMessages.SEQ_DEVICE_READY -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_AFDRIVE, delayMs, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00))
                    IPtpIpMessages.SEQ_CHECK_EVENT -> commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
                    IPtpIpMessages.SEQ_GET_DEVICE_PROP1 -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_DEVICE_PROP2, delayMs, isDumpLog, 0, 0x1015, 4, 0xd100, 0x00, 0x00, 0x00))
                    IPtpIpMessages.SEQ_GET_DEVICE_PROP2 -> commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
                    else -> commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in receivedMessage", e)
        }
    }

    private fun waitSleepAndExecute(block: () -> Unit) {
        mainHandler.postDelayed({
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command after delay", e)
            }
        }, delayMs.toLong())
    }

    private fun retrySendMessage(id: Int) {
        waitSleepAndExecute {
            when (id) {
                IPtpIpMessages.SEQ_START_LIVEVIEW -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_START_LIVEVIEW, delayMs, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_DEVICE_READY -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_READY, delayMs, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_CHECK_EVENT -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_CHECK_EVENT, delayMs, isDumpLog, 0, 0x90c7, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_GET_DEVICE_PROP1 -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_DEVICE_PROP1, delayMs, isDumpLog, 0, 0x5007, 4, 0x5007, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_GET_DEVICE_PROP2 -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_DEVICE_PROP2, delayMs, isDumpLog, 0, 0x1015, 4, 0xd100, 0x00, 0x00, 0x00))
                else -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_AFDRIVE, delayMs, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00))
            }
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rxBody: ByteArray?) {
        Log.v(TAG, "onReceiveProgress() : $currentBytes/$totalBytes")
    }

    override fun isReceiveMulti(): Boolean = false

    // バイト配列から指定されたオフセットの位置にある 16bit 整数 (Little Endian) を復元するヘルパー
    private fun getShortLittleEndian(bytes: ByteArray, offset: Int): Int {
        if (offset + 1 >= bytes.size) return 0
        return (bytes[offset].toUByte().toInt()) or (bytes[offset + 1].toUByte().toInt() shl 8)
    }

    companion object {
        private val TAG = NikonLiveViewControl::class.java.simpleName
    }
}

/*
class NikonLiveViewControl(val context: AppCompatActivity, interfaceProvider: IPtpIpInterfaceProvider, private val delayMs: Int, private val delayScale: Int) : ILiveViewControl, ILiveViewListener, IPtpIpCommunication, IPtpIpLiveViewImageCallback, IPtpIpCommandCallback
{
    private val isDumpLog = false
    private val commandIssuer = interfaceProvider.commandPublisher
    private val imageReceiver = NikonLiveViewImageReceiver(this)
    private val statusReceiver = NikonLiveViewStatusReceiver(true)
    private var dataReceiver: IImageDataReceiver? = null
    private var liveViewIsReceiving = false

    fun getLiveViewListener(): ILiveViewListener
    {
        return (this)
    }

    override fun changeLiveViewSize(size: String?)
    {
        Log.v(TAG, " changeLiveViewSize() : $size ")
    }

    override fun startLiveView()
    {
        Log.v(TAG, " startLiveView() ")
        try
        {
            commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_START_LIVEVIEW, delayMs, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun stopLiveView()
    {
        Log.v(TAG, " stopLiveView() ")
        try
        {
            if (liveViewIsReceiving)
            {
                liveViewIsReceiving = false
                commandIssuer.enqueueCommand(PtpIpCommandGeneric(PtpIpResponseReceiver(null), IPtpIpMessages.SEQ_STOP_LIVEVIEW, delayMs, isDumpLog, 0, 0x9202, 0, 0x00, 0x00, 0x00, 0x00))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun updateDigitalZoom()
    {
        Log.v(TAG, " updateDigitalZoom() ")
    }

    override fun updateMagnifyingLiveViewScale(isChangeScale: Boolean)
    {
        Log.v(TAG, " updateMagnifyingLiveViewScale() : $isChangeScale ")
    }

    override fun getMagnifyingLiveViewScale(): Float
    {
        return (0.0f)
    }

    override fun getDigitalZoomScale(): Float
    {
        return (0.0f)
    }

    override fun setCameraLiveImageView(target: IImageDataReceiver?)
    {
        Log.v(TAG, " setCameraLiveImageView() ")
        dataReceiver = target
    }

    override fun connect(): Boolean
    {
        Log.v(TAG, " connect() ")
        return (true)
    }

    override fun disconnect()
    {
        Log.v(TAG, " disconnect() ")
    }

    override fun onCompleted(data: ByteArray?, metadata: Map<String?, Any?>?)
    {
        try
        {
            if (dataReceiver != null && data != null && data.size > 0)
            {
                val offset = searchJpegHeader(data)
                if (data.size > 8 && offset < data.size)
                {
                    dataReceiver?.setImageData(Arrays.copyOfRange(data, offset, data.size), metadata) // ヘッダ部分を切り取って送る
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        sendNextMessage()
    }

    private fun sendNextMessage()
    {
        Log.v(TAG, "sendNextMessage(), sleep : ${delayMs.toLong() * delayScale} ms ")
        try
        {
            //Thread.sleep(delayMs.toLong())
            //commandIssuer.enqueueCommand(NikonStatusRequestMessage(statusReceiver, delayMs, isDumpLog))
            Thread.sleep(delayMs.toLong() * delayScale)
            if (commandIssuer.isExistCommandMessageQueue(IPtpIpMessages.SEQ_GET_VIEWFRAME) < 2)
            {
                commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun searchJpegHeader(data: ByteArray): Int
    {
        try
        {
            var pos = 0

            // 先頭の 1024 bytesまで
            val limit = if (data.size < 4096) data.size - 1 else 4096
            while (pos < limit)
            {
                if (((data[pos] == 0xff.toByte())&&(data[pos + 1] == 0xd8.toByte())))
                {
                    return (pos)
                }
                pos++
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (384)    // ヘッダサイズを決め打ち...
    }

    override fun onErrorOccurred(e: Exception)
    {
        Log.v(TAG, " onErrorOccurred () : " + e.localizedMessage)
    }

    override fun receivedMessage(id: Int, rx_body: ByteArray?)
    {
        if (rx_body == null)
        {
            Log.v(TAG, " NikonLiveViewControl::receivedMessage() body is null.")
            return
        }
        Log.v(TAG, " NikonLiveViewControl::receivedMessage() [id:$id]")
        try
        {
            if (rx_body.size < 10)
            {
                Log.v(TAG, " NikonLiveViewControl::receivedMessage() : BODY LENGTH IS TOO SHORT. SEND RETRY MESSAGE")
                retrySendMessage(id)
                return
            }

            var responseCode: Int = (rx_body[8].toUByte().toInt()) + (rx_body[9].toUByte().toInt()) * 256
            if (id == IPtpIpMessages.SEQ_CHECK_EVENT)
            {
                // 応答にはデータが含まれているので....受信データの末尾を拾う
                responseCode = (rx_body[rx_body.size - 6].toUByte().toInt()) + (rx_body[rx_body.size - 5].toUByte().toInt()) * 256
            }

            if (responseCode != 0x2001)
            {
                // NG応答を受信...同じコマンドを再送する
                Log.v(TAG, String.format(" NikonLiveViewControl: RECEIVED NG REPLY ID : %d, RESPONSE CODE : 0x%04x ", id, responseCode))
                retrySendMessage(id)
                return
            }

            Log.v(TAG, String.format(" NikonLiveViewControl: ----- OK REPLY (ID : %d) ----- ", id))
            waitSleep()
            when (id)
            {
                IPtpIpMessages.SEQ_START_LIVEVIEW -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_READY, delayMs, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_DEVICE_READY -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_AFDRIVE, delayMs, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_CHECK_EVENT -> commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
                IPtpIpMessages.SEQ_GET_DEVICE_PROP1 -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_DEVICE_PROP2, delayMs, isDumpLog, 0, 0x1015, 4, 0xd100, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_GET_DEVICE_PROP2 -> commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
                else -> commandIssuer.enqueueCommand(NikonLiveViewRequestMessage(imageReceiver, delayMs, isDumpLog))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun waitSleep()
    {
        try
        {
            Thread.sleep(delayMs.toLong())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun retrySendMessage(id: Int)
    {
        try
        {
            waitSleep()
            when (id)
            {
                IPtpIpMessages.SEQ_START_LIVEVIEW -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_START_LIVEVIEW, delayMs, isDumpLog, 0, 0x9201, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_DEVICE_READY -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_DEVICE_READY, delayMs, isDumpLog, 0, 0x90c8, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_CHECK_EVENT -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_CHECK_EVENT, delayMs, isDumpLog, 0, 0x90c7, 0, 0x00, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_GET_DEVICE_PROP1 -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_DEVICE_PROP1, delayMs, isDumpLog, 0, 0x5007, 4, 0x5007, 0x00, 0x00, 0x00))
                IPtpIpMessages.SEQ_GET_DEVICE_PROP2 -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_GET_DEVICE_PROP2, delayMs, isDumpLog, 0, 0x1015, 4, 0xd100, 0x00, 0x00, 0x00))
                else -> commandIssuer.enqueueCommand(PtpIpCommandGeneric(this, IPtpIpMessages.SEQ_AFDRIVE, delayMs, isDumpLog, 0, 0x90c1, 0, 0x00, 0x00, 0x00, 0x00))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onReceiveProgress(currentBytes: Int, totalBytes: Int, rx_body: ByteArray?)
    {
        Log.v(TAG, " onReceiveProgress() ")
    }

    override fun isReceiveMulti(): Boolean
    {
        return (false)
    }

    companion object
    {
        private const val TAG = "NikonLiveViewControl"
    }
}
*/