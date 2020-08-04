package net.osdn.gokigen.a01d.camera.kodak.operation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.a01d.camera.IFocusingControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;

import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

public class KodakFocusingControl implements IFocusingControl, IPtpIpCommandCallback
{
    private final String TAG = this.toString();
    private static final int FOCUS_LOCK_PRE = 15;
    private static final int FOCUS_LOCK = 16;
    private static final int FOCUS_MOVE = 17;
    private static final int FOCUS_UNLOCK = 18;

    //private final Activity context;
    private final IKodakCommandPublisher commandPublisher;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final IIndicatorControl indicator;

    private float maxPointLimitWidth;
    private float maxPointLimitHeight;
    private RectF preFocusFrameRect = null;
    private boolean not_support_focus_lock = false;
    private boolean isDumpLog = false;

    public KodakFocusingControl(@NonNull Activity context, @NonNull IKodakCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        //this.context = context;
        this.commandPublisher = commandPublisher;
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String focusPoint = preferences.getString(IPreferencePropertyAccessor.NIKON_FOCUS_XY, IPreferencePropertyAccessor.NIKON_FOCUS_XY_DEFAULT_VALUE);
            String[] focus = focusPoint.split(",");
            if (focus.length == 2)
            {
                maxPointLimitWidth = Integer.parseInt(focus[0]);
                maxPointLimitHeight = Integer.parseInt(focus[1]);
            }
            else
            {
                maxPointLimitWidth = 6000.0f;
                maxPointLimitHeight = 4000.0f;
            }
            not_support_focus_lock = preferences.getBoolean(IPreferencePropertyAccessor.NIKON_NOT_SUPPORT_FOCUS_LOCK, false);
            Log.v(TAG, "FOCUS RESOLUTION : " + maxPointLimitWidth + "," + maxPointLimitHeight + " FOCUS LOCK : " + not_support_focus_lock);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            maxPointLimitWidth = 6000.0f;
            maxPointLimitHeight = 4000.0f;
        }
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
                        preFocusFrameRect = getPreFocusFrameRect(point);
                        showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Running, 0.0);
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
        //lockAutoFocus(new PointF(0.5f, 0.5f));
    }

    private void lockAutoFocus(PointF point)
    {
        try
        {
            int x = (0x0000ffff & (Math.round(point.x * maxPointLimitWidth) + 1));
            int y = (0x0000ffff & (Math.round(point.y * maxPointLimitHeight) + 1));
            Log.v(TAG, "Lock AF: [" + x + ","+ y + "]");
            if (!not_support_focus_lock) {
                //commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, FOCUS_LOCK, isDumpLog, 0, 0x9205, 8, x, y));
            } else {
                //commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, FOCUS_MOVE, isDumpLog, 0, 0x90c1));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
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

    /**
     *
     *
     */
    private void showFocusFrame(RectF rect, IAutoFocusFrameDisplay.FocusFrameStatus status, double duration)
    {
        frameDisplayer.showFocusFrame(rect, status, duration);
        indicator.onAfLockUpdate(IAutoFocusFrameDisplay.FocusFrameStatus.Focused == status);
    }

    /**
     *
     *
     */
    private void hideFocusFrame()
    {
        frameDisplayer.hideFocusFrame();
        indicator.onAfLockUpdate(false);
    }


    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (rx_body.length < 10)
            {
                Log.v(TAG, " --- BODY LENGTH IS SHORT : FOCUS OPERATION ---");
                hideFocusFrame();
                preFocusFrameRect = null;
                return;
            }
            int responseCode = (rx_body[8] & 0xff) + ((rx_body[9] & 0xff) * 256);
            if (responseCode != 0x2001)
            {
                Log.v(TAG, String.format(" --- RECEIVED NG REPLY. : FOCUS OPERATION (0x%x) ---", responseCode));
                hideFocusFrame();
                preFocusFrameRect = null;
                return;
            }

            if ((id == FOCUS_LOCK)||(id == FOCUS_LOCK_PRE))
            {
                Log.v(TAG, "FOCUS LOCKED");
                //commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, FOCUS_MOVE, isDumpLog, 0, 0x90c1));  // OKのときは駆動
                if (preFocusFrameRect != null)
                {
                    // showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Focused, 1.0);  // 1秒だけ表示
                    showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Focused, -1);
                }
            }
            else if (id == FOCUS_MOVE)
            {
                Log.v(TAG, "FOCUS MOVED");
                //if (preFocusFrameRect != null)
                //{
                //    hideFocusFrame();
                //}
            }
            else // if (id == FOCUS_UNLOCK)
            {
                Log.v(TAG, "FOCUS UNLOCKED");
                hideFocusFrame();
            }
            preFocusFrameRect = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }
}
