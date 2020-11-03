package net.osdn.gokigen.a01d.camera.nikon.wrapper.liveview

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*

class NikonLiveViewControl(private val context: AppCompatActivity, interfaceProvider: IPtpIpInterfaceProvider, private val delayMs: Int, private val delayScale: Int) : ILiveViewControl, ILiveViewListener, IPtpIpCommunication, IPtpIpLiveViewImageCallback, IPtpIpCommandCallback
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
