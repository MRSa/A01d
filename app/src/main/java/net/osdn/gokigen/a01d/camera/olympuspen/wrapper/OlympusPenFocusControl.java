package net.osdn.gokigen.a01d.camera.olympuspen.wrapper;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.olympuspen.operation.OlympusPenAutoFocusControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class OlympusPenFocusControl implements IFocusingControl
{
    private final String TAG = toString();
    private final OlympusPenAutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    OlympusPenFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, @NonNull final IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        afControl = new OlympusPenAutoFocusControl(frameDisplayer, indicator);
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
