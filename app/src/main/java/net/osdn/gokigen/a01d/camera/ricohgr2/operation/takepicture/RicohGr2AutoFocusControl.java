package net.osdn.gokigen.a01d.camera.ricohgr2.operation.takepicture;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.IUsePentaxCommand;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import org.json.JSONObject;

import androidx.annotation.NonNull;

/**
 *
 *
 */
public class RicohGr2AutoFocusControl
{
    private static final String TAG = RicohGr2AutoFocusControl.class.getSimpleName();
    private final IIndicatorControl indicator;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final IUsePentaxCommand usePentaxCommand;
    private String unlockAutoFocusUrl = "http://192.168.0.1/v1/lens/focus/unlock";
    private String halfPressShutterUrl = "http://192.168.0.1/_gr";
    private int timeoutMs = 6000;


    /**
     *
     *
     */
    public RicohGr2AutoFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, final IIndicatorControl indicator, @NonNull IUsePentaxCommand usePentaxCommand)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
        this.usePentaxCommand = usePentaxCommand;
    }

    /**
     *
     *
     */
    public void lockAutoFocus(@NonNull final PointF point)
    {
        Log.v(TAG, "lockAutoFocus() : [" + point.x + ", " + point.y + "]");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    RectF preFocusFrameRect = getPreFocusFrameRect(point);
                    try
                    {
                        showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Running, 0.0);

                        String lockAutoFocusUrl = (usePentaxCommand.getUsePentaxCommand()) ? "http://192.168.0.1/v1/lens/focus" : "http://192.168.0.1/v1/lens/focus/lock";
                        //int posX = (int) (Math.round(point.x * 100.0));
                        //int posY = (int) (Math.round(point.y * 100.0));
                        String postData = "pos=" + ( (int) (Math.round(point.x * 100.0))) + "," + ((int) (Math.round(point.y * 100.0)));
                        Log.v(TAG, "AF (" + postData + ")");
                        String result = SimpleHttpClient.httpPost(lockAutoFocusUrl, postData, timeoutMs);
                        if ((result == null)||(result.length() < 1))
                        {
                            Log.v(TAG, "setTouchAFPosition() reply is null.");
                        }
                        else if (findTouchAFPositionResult(result))
                        {
                            // AF FOCUSED
                            Log.v(TAG, "lockAutoFocus() : FOCUSED");
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Focused, 1.0);  // いったん1秒だけ表示
                        }
                        else
                        {
                            // AF ERROR
                            Log.v(TAG, "lockAutoFocus() : ERROR");
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Failed, 1.0);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        try
                        {
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Errored, 1.0);
                        }
                        catch (Exception ee)
                        {
                            ee.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
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
    public void unlockAutoFocus()
    {
        Log.v(TAG, "unlockAutoFocus()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String result = SimpleHttpClient.httpPost(unlockAutoFocusUrl, "", timeoutMs);
                        if ((result == null)||(result.length() < 1))
                        {
                            Log.v(TAG, "cancelTouchAFPosition() reply is null.");
                        }
                        hideFocusFrame();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
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
    public void halfPressShutter(final boolean isPressed)
    {
        Log.v(TAG, "halfPressShutter() " + isPressed);
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String postData = (isPressed) ? "cmd=baf 1" : "cmd=baf 0";
                        String result = SimpleHttpClient.httpPost(halfPressShutterUrl, postData, timeoutMs);
                        if ((result == null)||(result.length() < 1))
                        {
                            Log.v(TAG, "halfPressShutter() [" + isPressed + "] reply is null.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
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
    private static boolean findTouchAFPositionResult(String replyString)
    {
        boolean afResult = false;
        try
        {
            JSONObject resultObject = new JSONObject(replyString);
            String result = resultObject.getString("errMsg");
            boolean focused = resultObject.getBoolean("focused");
            if (result.contains("OK"))
            {
                afResult = focused;
                Log.v(TAG, "AF Result : " + afResult);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (afResult);
    }
}
