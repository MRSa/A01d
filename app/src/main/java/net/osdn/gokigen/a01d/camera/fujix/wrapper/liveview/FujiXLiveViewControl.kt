package net.osdn.gokigen.a01d.camera.fujix.wrapper.liveview

import android.app.Activity
import android.util.Log
import androidx.preference.PreferenceManager
import net.osdn.gokigen.a01d.camera.ILiveViewControl
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor
import java.io.ByteArrayOutputStream
import java.net.Socket

class FujiXLiveViewControl(activity: Activity, private val ipAddress: String, private val portNumber: Int) : ILiveViewControl, IFujiXCommunication
{
    private val classTag = toString()
    private val liveViewListener = CameraLiveViewListenerImpl()
    private var waitMs = 0
    private var isStart = false
    private val logcat = false
    private val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

    init
    {
        try
        {
            val waitMsStr = preferences.getString(IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT, IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE)
            logcat(" waitMS : $waitMsStr")
            if (waitMsStr != null)
            {
                val wait = waitMsStr.toInt()
                if (wait in 20 .. 800)
                {
                    waitMs = wait
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            waitMs = 100
        }
        Log.v(classTag, " LOOP WAIT : $waitMs ms")
    }

    override fun startLiveView()
    {
        if (isStart)
        {
            // すでに受信スレッド動作中なので抜ける
            Log.v(classTag, " LiveView IS ALREADY STARTED")
            return
        }
        isStart = true
        try
        {
            Thread {
                try
                {
                    //startReceiveAlter(Socket(ipAddress, portNumber))
                    startReceive(Socket(ipAddress, portNumber))
                }
                catch (e: Exception)
                {
                    Log.v(classTag, " IP : $ipAddress port : $portNumber")
                    e.printStackTrace()
                }
            }.start()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun stopLiveView()
    {
        isStart = false
    }

    override fun updateDigitalZoom()
    {

    }

    override fun updateMagnifyingLiveViewScale(isChangeScale: Boolean)
    {

    }

    override fun getMagnifyingLiveViewScale(): Float
    {
        return (1.0f)
    }

    override fun changeLiveViewSize(size: String)
    {

    }

    override fun getDigitalZoomScale(): Float
    {
        return (1.0f)
    }

    fun getLiveViewListener(): ILiveViewListener
    {
        return (liveViewListener)
    }

    override fun connect(): Boolean
    {
        return (true)
    }

    override fun disconnect()
    {
        isStart = false
    }

    private fun logcat(message: String)
    {
        if (logcat)
        {
            Log.v(classTag, message)
        }
    }

    private fun findJpegTag(byteArray : ByteArray, arraySize : Int, charTag : Byte) : Int
    {
        var index = 0
        while (index < arraySize)
        {
            val value1 = byteArray[index]
            if (value1 == 0xff.toByte())
            {
                if (byteArray[index + 1] == charTag)
                {
                    return (index)
                }
            }
            index++
        }
        return (-1)
    }

    private fun startReceive(socket: Socket)
    {
        var errorCount = 0
        val isr = socket.getInputStream()
        val byteArray = ByteArray(BUFFER_SIZE + 32)
        var findJpeg = false
        val byteBuffer = ByteArrayOutputStream()

        while (isStart)
        {
            try
            {
                val readBytes = isr.read(byteArray, 0, BUFFER_SIZE)
                if (readBytes < 0)
                {
                    ////////// 受信待ち...  //////////
                    Thread.sleep(waitMs / 4.toLong())
                    logcat(" --- Wait [LiveView] ---")
                    continue
                }

                if (!findJpeg)
                {
                    val startPosition = findJpegTag(byteArray, readBytes, 0xd8.toByte())
                    if (startPosition < 0)
                    {
                        Thread.sleep(waitMs / 4.toLong())
                        logcat(" --- Wait LiveView ---")
                        continue
                    }

                    // JPEGのスタートタグが見つかっていた場合
                    findJpeg = true
                    if (startPosition < readBytes)
                    {
                        byteBuffer.reset()
                        byteBuffer.write(byteArray, startPosition, (readBytes - startPosition))
                        Thread.sleep(waitMs / 4.toLong())
                        logcat(" --- RECEIVE JPEG ---")
                        continue
                    }
                }

                // JPEGのエンドタグを探す
                val endPosition = findJpegTag(byteArray, readBytes, 0xd9.toByte())
                if (endPosition < 0)
                {
                    // エンドマーカーがなかった...全部streamに流しておく
                    byteBuffer.write(byteArray, 0, readBytes)
                    Thread.sleep(waitMs / 4.toLong())
                    logcat(" --- RECEIVE JPEG (cont.) ---")
                    continue
                }

                byteBuffer.write(byteArray, 0, endPosition)
                liveViewListener.onUpdateLiveView(byteBuffer.toByteArray(), null)
                logcat(" --- SEND JPEG IMAGE ---")
                errorCount = 0
                findJpeg = false
                Thread.sleep(waitMs.toLong())
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                errorCount++
            }

            if (errorCount > ERROR_LIMIT)
            {
                // エラーが連続でたくさん出たらループをストップ(ライブビューを停止)させる
                isStart = false
            }
        }

        try
        {
            isr.close()
            socket.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val BUFFER_SIZE = 2048 * 1280
        private const val ERROR_LIMIT = 30
    }
}
