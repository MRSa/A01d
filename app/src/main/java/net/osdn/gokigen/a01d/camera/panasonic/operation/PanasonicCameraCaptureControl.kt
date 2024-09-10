package net.osdn.gokigen.a01d.camera.panasonic.operation

import android.util.Log
import net.osdn.gokigen.a01d.camera.ICaptureControl
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.SingleShotControl
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.a01d.liveview.IIndicatorControl

class PanasonicCameraCaptureControl(frameDisplayer: IAutoFocusFrameDisplay, indicator: IIndicatorControl) : ICaptureControl
{
    private val singleShotControl = SingleShotControl(frameDisplayer, indicator)

    fun setCamera(panasonicCamera: IPanasonicCamera)
    {
        singleShotControl.setCamera(panasonicCamera)
    }

    /**
     * 撮影する
     *
     */
    override fun doCapture(kind: Int)
    {
        Log.v(TAG, "doCapture() : $kind")
        try
        {
            singleShotControl.singleShot()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = PanasonicCameraCaptureControl::class.java.simpleName
    }
}
