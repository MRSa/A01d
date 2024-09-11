package net.osdn.gokigen.a01d.camera.panasonic.wrapper

import android.app.Activity
import android.util.Log
import net.osdn.gokigen.a01d.ICardSlotSelector
import net.osdn.gokigen.a01d.camera.ICameraChangeListener
import net.osdn.gokigen.a01d.camera.ICameraConnection
import net.osdn.gokigen.a01d.camera.ICameraInformation
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver
import net.osdn.gokigen.a01d.camera.ICaptureControl
import net.osdn.gokigen.a01d.camera.IDisplayInjector
import net.osdn.gokigen.a01d.camera.IFocusingControl
import net.osdn.gokigen.a01d.camera.IFocusingModeNotify
import net.osdn.gokigen.a01d.camera.ILiveViewControl
import net.osdn.gokigen.a01d.camera.IZoomLensControl
import net.osdn.gokigen.a01d.camera.panasonic.IPanasonicInterfaceProvider
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraCaptureControl
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraFocusControl
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraZoomLensControl
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.connection.PanasonicCameraConnection
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener.CameraEventObserver.Companion.newInstance
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.eventlistener.ICameraEventObserver
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.a01d.liveview.IIndicatorControl
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener

class PanasonicCameraWrapper(
    private val context: Activity,
    private val provider: ICameraStatusReceiver,
    private val listener: ICameraChangeListener,
    private val cardSlotSelector: ICardSlotSelector
) :
    IPanasonicCameraHolder, IPanasonicInterfaceProvider, IDisplayInjector
{
    private var panasonicCamera: IPanasonicCamera? = null
    private var eventObserver: ICameraEventObserver? = null
    private var liveViewControl: PanasonicLiveViewControl? = null
    private var focusControl: PanasonicCameraFocusControl? = null
    private var captureControl: PanasonicCameraCaptureControl? = null
    private var zoomControl: PanasonicCameraZoomLensControl? = null
    private lateinit var cameraConnection: PanasonicCameraConnection

    override fun prepare()
    {
        Log.v(
            TAG,
            " prepare : " + panasonicCamera?.getFriendlyName() + " " + panasonicCamera?.getModelName()
        )
        try
        {
            if (eventObserver == null) {
                eventObserver = newInstance(context, panasonicCamera!!, cardSlotSelector)
            }
            if (liveViewControl == null) {
                liveViewControl = PanasonicLiveViewControl(panasonicCamera!!)
            }
            focusControl?.setCamera(panasonicCamera!!)
            captureControl?.setCamera(panasonicCamera!!)
            zoomControl?.setCamera(panasonicCamera!!)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startRecMode()
    {
        try
        {
            // 撮影モード(RecMode)に切り替え
            val sessionId = panasonicCamera?.getCommunicationSessionId()
            val urlToSend = "${panasonicCamera?.getCmdUrl()}cam.cgi?mode=camcmd&value=recmode"
            val reply = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetWithHeader(urlToSend, headerMap, null,
                    TIMEOUT_MS
                )
            }
            else
            {
                SimpleHttpClient.httpGet(urlToSend, TIMEOUT_MS)
            }


            if (!reply.contains("ok")) {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE RECMODE.")
            }

            val urlToSend2 = "${panasonicCamera?.getCmdUrl()}cam.cgi?mode=setsetting&type=afmode&value=1area"
            val reply2 = if (!sessionId.isNullOrEmpty())
            {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["X-SESSION_ID"] = sessionId
                SimpleHttpClient.httpGetWithHeader(urlToSend2, headerMap, null,
                    TIMEOUT_MS
                )
            } else {
                SimpleHttpClient.httpGet(urlToSend, TIMEOUT_MS)
            }
            if (!reply2.contains("ok")) {
                Log.v(TAG, "CAMERA REPLIED ERROR : CHANGE AF MODE 1area.")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startEventWatch(listener: ICameraChangeListener?)
    {
        try
        {
            if (eventObserver != null)
            {
                if (listener != null)
                {
                    eventObserver!!.setEventListener(listener)
                }
                eventObserver?.activate()
                eventObserver?.start()
                eventObserver?.getCameraStatusHolder()?.getLiveviewStatus()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun detectedCamera(camera: IPanasonicCamera)
    {
        Log.v(TAG, "detectedCamera()")
        panasonicCamera = camera
    }

    override fun getPanasonicCameraConnection(): ICameraConnection
    {
        if (!::cameraConnection.isInitialized)
        {
            cameraConnection = PanasonicCameraConnection(
                context, provider, this,
                listener
            )
        }
        return (cameraConnection)
    }

    override fun getPanasonicLiveViewControl(): ILiveViewControl?
    {
        return (liveViewControl)
    }

    override fun getLiveViewListener(): ILiveViewListener?
    {
        return (liveViewControl?.getLiveViewListener())
    }

    override fun getFocusingControl(): IFocusingControl?
    {
        return (focusControl)
    }

    override fun getCameraInformation(): ICameraInformation?
    {
        return null
    }

    override fun getZoomLensControl(): IZoomLensControl?
    {
        return (zoomControl)
    }

    override fun getCaptureControl(): ICaptureControl?
    {
        return (captureControl)
    }

    override fun getDisplayInjector(): IDisplayInjector
    {
        return (this)
    }

    override fun getPanasonicCamera(): IPanasonicCamera?
    {
        return (panasonicCamera)
    }

    override fun injectDisplay(
        frameDisplayer: IAutoFocusFrameDisplay,
        indicator: IIndicatorControl,
        focusingModeNotify: IFocusingModeNotify
    ) {
        Log.v(TAG, "injectDisplay()")

        focusControl = PanasonicCameraFocusControl(frameDisplayer, indicator)
        captureControl = PanasonicCameraCaptureControl(frameDisplayer, indicator)
        zoomControl = PanasonicCameraZoomLensControl()
    }

    companion object
    {
        private val TAG: String = PanasonicCameraWrapper::class.java.simpleName
        private const val TIMEOUT_MS = 3000
    }
}
