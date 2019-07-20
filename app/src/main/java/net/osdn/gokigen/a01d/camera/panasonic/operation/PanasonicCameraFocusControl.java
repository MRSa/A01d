package net.osdn.gokigen.a01d.camera.panasonic.operation;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.PanasonicAutoFocusControl;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCameraApi;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import androidx.annotation.NonNull;

public class PanasonicCameraFocusControl  implements IFocusingControl
{
    private final String TAG = toString();
    private final PanasonicAutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    public PanasonicCameraFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, @NonNull final IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        afControl = new PanasonicAutoFocusControl(frameDisplayer, indicator);
    }

    public void setCameraApi(@NonNull IPanasonicCameraApi panasonicCameraApi)
    {
        afControl.setCameraApi(panasonicCameraApi);
    }

    @Override
    public boolean driveAutoFocus(final MotionEvent motionEvent)
    {
        Log.v(TAG, "driveAutoFocus()");
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
        {
            return (false);
        }
        try
        {
            PointF point = frameDisplay.getPointWithEvent(motionEvent);
            if (frameDisplay.isContainsPoint(point))
            {
                afControl.lockAutoFocus(point);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    @Override
    public void unlockAutoFocus()
    {
        Log.v(TAG, "unlockAutoFocus()");
        try
        {
            afControl.unlockAutoFocus();
            frameDisplay.hideFocusFrame();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void halfPressShutter(boolean isPressed)
    {
        Log.v(TAG, "halfPressShutter() " + isPressed);
        try
        {
            afControl.halfPressShutter(isPressed);
            if (!isPressed)
            {
                // フォーカスを外す
                frameDisplay.hideFocusFrame();
                afControl.unlockAutoFocus();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
