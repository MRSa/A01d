package net.osdn.gokigen.a01d.camera.panasonic.operation

import android.util.Log
import android.view.MotionEvent
import net.osdn.gokigen.a01d.camera.IFocusingControl
import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.PanasonicAutoFocusControl
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay
import net.osdn.gokigen.a01d.liveview.IIndicatorControl

class PanasonicCameraFocusControl(private val frameDisplay: IAutoFocusFrameDisplay, indicator: IIndicatorControl) : IFocusingControl
{
    private val afControl = PanasonicAutoFocusControl(frameDisplay, indicator)

    fun setCamera(panasonicCamera: IPanasonicCamera)
    {
        afControl.setCamera(panasonicCamera)
    }

    override fun driveAutoFocus(motionEvent: MotionEvent): Boolean
    {
        Log.v(TAG, "driveAutoFocus()")
        if (motionEvent.action != MotionEvent.ACTION_DOWN)
        {
            return (false)
        }
        try
        {
            val point = frameDisplay.getPointWithEvent(motionEvent)
            if (frameDisplay.isContainsPoint(point))
            {
                afControl.lockAutoFocus(point)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    override fun unlockAutoFocus()
    {
        Log.v(TAG, "unlockAutoFocus()")
        try
        {
            afControl.unlockAutoFocus()
            frameDisplay.hideFocusFrame()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun halfPressShutter(isPressed: Boolean)
    {
        Log.v(TAG, "halfPressShutter() $isPressed")
        try
        {
            afControl.halfPressShutter(isPressed)
            if (!isPressed)
            {
                // フォーカスを外す
                frameDisplay.hideFocusFrame()
                afControl.unlockAutoFocus()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG: String = PanasonicCameraFocusControl::class.java.simpleName
    }
}