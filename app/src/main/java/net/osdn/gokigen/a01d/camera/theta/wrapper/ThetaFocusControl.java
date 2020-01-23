package net.osdn.gokigen.a01d.camera.theta.wrapper;

import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

public class ThetaFocusControl implements IFocusingControl
{
    private final String TAG = toString();
    private final IAutoFocusFrameDisplay frameDisplay;

    ThetaFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer)
    {
        this.frameDisplay = frameDisplayer;
    }

    @Override
    public boolean driveAutoFocus(final MotionEvent motionEvent)
    {
        Log.v(TAG, "driveAutoFocus()");
        try
        {
            frameDisplay.hideFocusFrame();
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
            frameDisplay.hideFocusFrame();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
