package net.osdn.gokigen.a01d.camera.kodak.operation;


import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific.KodakExecuteFocus;

import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;


public class KodakFocusingControl implements IFocusingControl, IKodakCommandCallback
{
    private final String TAG = this.toString();

    //private final Activity context;
    private final IKodakCommandPublisher commandPublisher;
    private final IAutoFocusFrameDisplay frameDisplayer;
    //private final IIndicatorControl indicator;

    //private RectF preFocusFrameRect = null;
    //private boolean not_support_focus_lock = false;
    //private boolean isDumpLog = false;

    //public KodakFocusingControl(@NonNull Activity context, @NonNull IKodakCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    public KodakFocusingControl(@NonNull IKodakCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer)
    {
        //this.context = context;
        this.commandPublisher = commandPublisher;
        this.frameDisplayer = frameDisplayer;
        //this.indicator = indicator;
    }

    @Override
    public boolean driveAutoFocus(final MotionEvent motionEvent)
    {
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
        {
            return (false);
        }
        Log.v(TAG, "driveAutoFocus()");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    PointF point = frameDisplayer.getPointWithEvent(motionEvent);
                    if (point != null)
                    {
                        // preFocusFrameRect = getPreFocusFrameRect(point);
                        // showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Running, 1.0);
                        if (frameDisplayer.isContainsPoint(point))
                        {
                            lockAutoFocus(point);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
        return (false);
    }

    @Override
    public void unlockAutoFocus()
    {
        try
        {
            Log.v(TAG, " Unlock AF ");
            //commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, FOCUS_UNLOCK, isDumpLog, 0, 0x9206));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void halfPressShutter(boolean isPressed)
    {
        //unlockAutoFocus();
        //commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, FOCUS_MOVE, isDumpLog, 0, 0x90c1));
        lockAutoFocus(new PointF(0.5f, 0.5f));
    }

    private void lockAutoFocus(PointF point)
    {
        float maxPointLimitWidth = 1000000.0f;
        float maxPointLimitHeight = 1000000.0f;
        try
        {
            int x = (0x00ffffff & (Math.round(point.x * maxPointLimitWidth) + 1));
            int y = (0x00ffffff & (Math.round(point.y * maxPointLimitHeight) + 1));
            Log.v(TAG, "Lock AF: [" + x + ","+ y + "]");
            commandPublisher.enqueueCommand(new KodakExecuteFocus(this, x, y));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

/*
    private RectF getPreFocusFrameRect(@NonNull PointF point)
    {
        float imageWidth =  frameDisplayer.getContentSizeWidth();
        float imageHeight =  frameDisplayer.getContentSizeHeight();

        // Display a provisional focus frame at the touched point.
        float focusWidth = 0.125f;  // 0.125 is rough estimate.
        float focusHeight = 0.125f;
        if (imageWidth > imageHeight)
        {
            focusHeight *= (imageWidth / imageHeight);
        }
        else
        {
            focusHeight *= (imageHeight / imageWidth);
        }
        return (new RectF(point.x - focusWidth / 2.0f, point.y - focusHeight / 2.0f,
                point.x + focusWidth / 2.0f, point.y + focusHeight / 2.0f));
    }

    private void showFocusFrame(RectF rect, IAutoFocusFrameDisplay.FocusFrameStatus status, double duration)
    {
        frameDisplayer.showFocusFrame(rect, status, duration);
        indicator.onAfLockUpdate(IAutoFocusFrameDisplay.FocusFrameStatus.Focused == status);
    }

    private void hideFocusFrame()
    {
        frameDisplayer.hideFocusFrame();
        indicator.onAfLockUpdate(false);
    }
*/

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, " KodakFocusingControl::receivedMessage() : ");
    }
}
