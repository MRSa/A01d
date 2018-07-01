package net.osdn.gokigen.a01d.camera.sony.operation;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.sony.operation.takepicture.SonyAutoFocusControl;
import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCameraApi;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class SonyCameraFocusControl  implements IFocusingControl
{
    private final String TAG = toString();
    private final SonyAutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    public SonyCameraFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, @NonNull final IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        afControl = new SonyAutoFocusControl(frameDisplayer, indicator);
    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        afControl.setCameraApi(sonyCameraApi);
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
}
