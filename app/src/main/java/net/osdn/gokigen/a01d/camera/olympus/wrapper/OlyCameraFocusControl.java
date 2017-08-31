package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import net.osdn.gokigen.a01d.camera.olympus.operation.IFocusingControl;
import net.osdn.gokigen.a01d.camera.olympus.operation.takepicture.AutoFocusControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

/**
 *
 *
 */
public class OlyCameraFocusControl implements IFocusingControl
{
    private final String TAG = toString();
    private final AutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    public OlyCameraFocusControl(OlyCameraWrapper wrapper, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        afControl = new AutoFocusControl(wrapper.getOLYCamera(), frameDisplayer, indicator);
    }

    @Override
    public boolean driveAutoFocus(final MotionEvent motionEvent)
    {
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
        {
            return (false);
        }

        if (frameDisplay != null)
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    PointF point = frameDisplay.getPointWithEvent(motionEvent);
                    if (frameDisplay.isContainsPoint(point))
                    {
                        afControl.lockAutoFocus(point);
                    }
                }
            });
            try
            {
                thread.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return (false);
    }

    @Override
    public void unlockAutoFocus()
    {
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
