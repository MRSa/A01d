package net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture

import android.util.Log
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.a01d.liveview.IIndicatorControl

class SingleShotControl(private val frameDisplayer: IAutoFocusFrameDisplay, private val indicator: IIndicatorControl)
{
    private lateinit var camera: IPanasonicCamera

    fun setCamera(panasonicCamera: IPanasonicCamera) {
        this.camera = panasonicCamera
    }

    fun singleShot()
    {
        Log.v(TAG, "singleShot()")
        if (!::camera.isInitialized)
        {
            Log.v(TAG, "IPanasonicCamera is not initialized...")
            return
        }
        try
        {
            val thread = Thread {
                try
                {
                    val sessionId = camera.getCommunicationSessionId()
                    val urlToSend = "${camera.getCmdUrl()}cam.cgi?mode=camcmd&value=capture"
                    val reply = if (!sessionId.isNullOrEmpty()) {
                        val headerMap: MutableMap<String, String> = HashMap()
                        headerMap["X-SESSION_ID"] = sessionId
                        SimpleHttpClient.httpGetWithHeader(urlToSend, headerMap, null, TIMEOUT_MS)
                    } else {
                        SimpleHttpClient.httpGet(urlToSend, TIMEOUT_MS)
                    }
                    if (!reply.contains("ok")) {
                        Log.v(TAG, "Capture Failure... : $reply")
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                frameDisplayer.hideFocusFrame()
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = SingleShotControl::class.java.simpleName
        private const val TIMEOUT_MS = 3000
    }
}
