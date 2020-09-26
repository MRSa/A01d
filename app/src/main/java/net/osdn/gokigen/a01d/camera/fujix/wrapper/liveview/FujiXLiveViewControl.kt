package net.osdn.gokigen.a01d.camera.fujix.wrapper.liveview

import android.app.Activity
import android.util.Log
import androidx.preference.PreferenceManager
import net.osdn.gokigen.a01d.camera.ILiveViewControl
import net.osdn.gokigen.a01d.camera.fujix.wrapper.command.IFujiXCommunication
import net.osdn.gokigen.a01d.camera.utils.SimpleLogDumper
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.util.*

class FujiXLiveViewControl(activity: Activity, private val ipAddress: String, private val portNumber: Int) : ILiveViewControl, IFujiXCommunication
{
    private val TAG = toString()
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
        Log.v(TAG, " LOOP WAIT : $waitMs ms")
    }

    override fun startLiveView()
    {
        if (isStart)
        {
            // すでに受信スレッド動作中なので抜ける
            Log.v(TAG, " LiveView IS ALREADY STARTED")
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
                    Log.v(TAG, " IP : $ipAddress port : $portNumber")
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
            Log.v(TAG, message)
        }
    }

    private fun dump_bytes(header : String, byteArray: ByteArray, size : Int = 24)
    {
        if (logcat)
        {
            SimpleLogDumper.dump_bytes(header, byteArray.copyOf(size))
        }
    }

    private fun startReceiveAlter(socket: Socket)
    {
        var errorCount = 0
        val isr = socket.getInputStream()
        val byteArray = ByteArray(BUFFER_SIZE + 32)

        while (isStart)
        {
            try
            {
                var findJpeg = false
                var length_bytes: Int
                var read_bytes = isr.read(byteArray, 0, BUFFER_SIZE)
                if (read_bytes > DATA_HEADER_OFFSET)
                {
                    // メッセージボディの先頭にあるメッセージ長分は読み込む
                    length_bytes = (byteArray[3].toInt() and 0xff shl 24) + (byteArray[2].toInt() and 0xff shl 16) + (byteArray[1].toInt() and 0xff shl 8) + (byteArray[0].toInt() and 0xff)
                    if (byteArray[18] == 0xff.toByte() && byteArray[19] == 0xd8.toByte())
                    {
                        findJpeg = true
                        while (read_bytes < length_bytes && read_bytes < BUFFER_SIZE && length_bytes <= BUFFER_SIZE)
                        {
                            val append_bytes = isr.read(byteArray, read_bytes, length_bytes - read_bytes)
                            logcat("READ AGAIN : $append_bytes [$read_bytes]")
                            if (append_bytes < 0)
                            {
                                break
                            }
                            read_bytes = read_bytes + append_bytes
                        }
                        logcat("READ BYTES : " + read_bytes + "  (" + length_bytes + " bytes, " + waitMs + "ms)")
                    }
                    else
                    {
                        // ウェイトを短めに入れてマーカーを拾うまで待つ
                        Thread.sleep(waitMs / 4.toLong())
                        logcat(" --- wait LiveView ---")
                        continue
                    }
                }

                // 先頭データをダンプする
                dump_bytes("[LV]", byteArray)
                if (findJpeg)
                {
                    liveViewListener.onUpdateLiveView(Arrays.copyOfRange(byteArray, DATA_HEADER_OFFSET, read_bytes - DATA_HEADER_OFFSET), null)
                    errorCount = 0
                }
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

    private fun findJpegStartTag(byteArray : ByteArray, arraySize : Int) : Int
    {
        var index = 0
        while (index < arraySize)
        {
            val value1 = byteArray.get(index)
            if (value1 == 0xff.toByte())
            {
                if (byteArray.get(index + 1) == 0xd8.toByte())
                {
                    return (index)
                }
            }
            index++
        }
        return (-1)
    }

    private fun findJpegFinishTag(byteArray : ByteArray, arraySize : Int) : Int
    {
        var index = 0

        while (index < arraySize)
        {
            val value1 = byteArray.get(index)
            if (value1 == 0xff.toByte())
            {
                if (byteArray.get(index + 1) == 0xd9.toByte())
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
                    val startPosition = findJpegStartTag(byteArray, readBytes)
                    if (startPosition < 0)
                    {
                        Thread.sleep(waitMs / 4.toLong())
                        logcat(" --- Wait LiveView ---")
                        continue
                    }

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

                // JPEGのスタートタグが見つかっていた場合...
                val endPosition = findJpegFinishTag(byteArray, readBytes)
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
        private const val DATA_HEADER_OFFSET = 18
        private const val BUFFER_SIZE = 2048 * 1280
        private const val ERROR_LIMIT = 30
    }
}
