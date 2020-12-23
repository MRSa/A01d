package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview

import android.app.Activity
import android.util.Log
import net.osdn.gokigen.a01d.camera.ILiveViewControl
import net.osdn.gokigen.a01d.camera.ptpip.IPtpIpInterfaceProvider
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommunication
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGenericWithRetry
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.liveview.IPtpIpLiveViewImageCallback
import net.osdn.gokigen.a01d.liveview.liveviewlistener.IImageDataReceiver
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener
import java.util.*

class CanonLiveViewControl(context: Activity, interfaceProvider: IPtpIpInterfaceProvider, private val delayMs: Int, private val isSearchJpegHeader: Boolean) : ILiveViewControl, ILiveViewListener, IPtpIpCommunication, IPtpIpLiveViewImageCallback
{
    private val commandIssuer = interfaceProvider.commandPublisher
    private val isDumpLog = false
    private val retryCount = 1200
    private val imageReceiver = CanonLiveViewImageReceiver(context, this)
    private var dataReceiver: IImageDataReceiver? = null
    private var liveViewIsReceiving = false
    private var commandIssued = false

    fun getLiveViewListener(): ILiveViewListener
    {
        return (this)
    }

    private fun mainLoop()
    {
        if (!commandIssued)
        {
            if (isDumpLog)
            {
                Log.v(TAG, " enqueueCommand() [ queue size : ${commandIssuer.currentQueueSize} ] ")
            }
            if (commandIssuer.currentQueueSize < 3)
            {
                commandIssued = true
                commandIssuer.enqueueCommand(PtpIpCommandGenericWithRetry(imageReceiver, IPtpIpMessages.SEQ_GET_VIEWFRAME, delayMs, retryCount, isRetry = false, isDumpLog = false, holdId = 0, opcode = 0x9153, bodySize = 12, value = 0x00200000, value2 = 0x01, value3 = 0x00, value4 = 0x00))
            }
        }
        try
        {
            Thread.sleep(delayMs.toLong())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun changeLiveViewSize(size: String) {}

    override fun startLiveView()
    {
        Log.v(TAG, " startLiveView() : delay $delayMs ms.")
        liveViewIsReceiving = true
        try
        {
            val thread = Thread {
                try
                {
                    while (liveViewIsReceiving)
                    {
                        mainLoop()
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun stopLiveView()
    {
        Log.v(TAG, " stopLiveView() ")
        liveViewIsReceiving = false
    }

    override fun updateDigitalZoom()
    {
        Log.v(TAG, " updateDigitalZoom() ")
    }

    override fun updateMagnifyingLiveViewScale(isChangeScale: Boolean)
    {
        Log.v(TAG, " updateMagnifyingLiveViewScale() ")
    }

    override fun getMagnifyingLiveViewScale(): Float
    {
        return (0.0f)
    }

    override fun getDigitalZoomScale(): Float
    {
        return (0.0f)
    }

    override fun setCameraLiveImageView(target: IImageDataReceiver)
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

    override fun onCompleted(data: ByteArray?, metadata: Map<String, Any>?)
    {
        //Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- ");
        try
        {
            if ((dataReceiver != null)&&(data != null))
            {
                if (isDumpLog)
                {
                    Log.v(TAG, "  ---+++--- RECEIVED LV IMAGE ---+++--- : " + data.size + " bytes.")
                }
                val headerSize = searchJpegHeader(data)
                if (headerSize >= 0)
                {
                    dataReceiver?.setImageData(Arrays.copyOfRange(data, headerSize, data.size), metadata) // ヘッダ部分を切り取って送る
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        commandIssued = false
    }

    private fun searchJpegHeader(data: ByteArray): Int
    {
        if (data.size <= 8)
        {
            return -1
        }
        if (!isSearchJpegHeader)
        {
            // JPEG ヘッダを探さない場合は、8バイト固定とする
            return (8)
        }
        try
        {
            val size = (data.size - 1)
            var index = 0
            while (index < size)
            {
                if (data[index] == 0xff.toByte() && data[index + 1] == 0xd8.toByte())
                {
                    return (index)
                }
                index++
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        // 見つからなかったときは 8 を返す
        return (8)
    }

    override fun onErrorOccurred(e: Exception)
    {
        Log.v(TAG, " onErrorOccurred () : " + e.localizedMessage)
        commandIssued = false
    }

    companion object
    {
        private const val TAG = "CanonLiveViewControl"
    }
}
