package net.osdn.gokigen.a01d.camera.ricohgr2.operation;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.operation.takepicture.RicohGr2AutoFocusControl;
import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.IUsePentaxCommand;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import androidx.annotation.NonNull;

public class RicohGr2CameraFocusControl implements IFocusingControl
{
    private final String TAG = toString();
    private final RicohGr2AutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    public RicohGr2CameraFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, @NonNull final IIndicatorControl indicator, @NonNull IUsePentaxCommand usePentaxCommand)
    {
        this.frameDisplay = frameDisplayer;
        this.afControl = new RicohGr2AutoFocusControl(frameDisplayer, indicator, usePentaxCommand);
    }

    @Override
    public boolean driveAutoFocus(MotionEvent motionEvent)
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
        afControl.unlockAutoFocus();
    }

    @Override
    public void halfPressShutter(boolean isPressed)
    {
        afControl.halfPressShutter(isPressed);
    }
}
