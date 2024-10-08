package net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener

import android.content.Context
import android.util.Log
import net.osdn.gokigen.a01d.ICardSlotSelector
import net.osdn.gokigen.a01d.camera.ICameraChangeListener
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient

/**
 *
 *
 */
class CameraEventObserver(context: Context, private val remote: IPanasonicCamera, cardSlotSelector: ICardSlotSelector
) : ICameraEventObserver
{
    private val statusHolder = CameraStatusHolder(context, remote, cardSlotSelector)
    private var isEventMonitoring: Boolean = false
    private var isActive: Boolean = false

    override fun start(): Boolean
    {
        if (!isActive)
        {
            Log.w(TAG, "start() observer is not active.")
            return (false)
        }
        if (isEventMonitoring)
        {
            Log.w(TAG, "start() already starting.")
            return (false)
        }
        isEventMonitoring = true

        try
        {
            val thread: Thread = object : Thread() {
                override fun run()
                {
                    Log.d(TAG, "start() exec.")
                    while (isEventMonitoring)
                    {
                        try
                        {
                            val sessionId = remote.getCommunicationSessionId()
                            val urlToSend = "${remote.getCmdUrl()}cam.cgi?mode=getstate"
                            val reply = if (!sessionId.isNullOrEmpty())
                            {
                                val headerMap: MutableMap<String, String> = HashMap()
                                headerMap["X-SESSION_ID"] = sessionId
                                SimpleHttpClient.httpGetWithHeader(urlToSend, headerMap, null, TIMEOUT_MS)
                            }
                            else
                            {
                                SimpleHttpClient.httpGet(urlToSend, TIMEOUT_MS)
                            }
                            // parse reply message
                            statusHolder.parse(reply)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                        try
                        {
                            sleep(1000)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                    isEventMonitoring = false
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    override fun stop()
    {
        isEventMonitoring = false
    }

    override fun release()
    {
        isEventMonitoring = false
        isActive = false
    }

    override fun setEventListener(listener: ICameraChangeListener)
    {
        try
        {
            statusHolder.setEventChangeListener(listener)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun clearEventListener()
    {
        try
        {
            statusHolder.clearEventChangeListener()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun getCameraStatusHolder(): ICameraStatusHolder
    {
        return (statusHolder)
    }

    override fun activate()
    {
        isActive = true
    }

    companion object
    {
        private val TAG: String = CameraEventObserver::class.java.simpleName
        private const val TIMEOUT_MS = 3000
        fun newInstance(context: Context, apiClient: IPanasonicCamera, cardSlotSelector: ICardSlotSelector): ICameraEventObserver
        {
            return (CameraEventObserver(context, apiClient, cardSlotSelector))
        }
    }
}
