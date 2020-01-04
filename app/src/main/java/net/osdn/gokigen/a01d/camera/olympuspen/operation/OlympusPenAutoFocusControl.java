package net.osdn.gokigen.a01d.camera.olympuspen.operation;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.PanasonicAutoFocusControl;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OlympusPenAutoFocusControl
{
    private static final String TAG = OlympusPenAutoFocusControl.class.getSimpleName();
    private static final int TIMEOUT_MS = 3000;
    private final IIndicatorControl indicator;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private Map<String, String> headerMap;

    private final String COMMUNICATION_URL = "http://192.168.0.10/";
    private final String AF_FRAME_COMMAND = "exec_takemotion.cgi?com=assignafframe";
    private final String AF_RELEASE_COMMAND = "exec_takemotion.cgi?com=releaseafframe";

    private float scaleX = 640.0f;
    private float scaleY = 480.0f;

    /**
     *
     *
     */
    public OlympusPenAutoFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, final IIndicatorControl indicator)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;

        headerMap = new HashMap<>();
        headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
        headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"
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

                        int posX = (int) (Math.floor(point.x * 1000.0 * scaleX));
                        int posY = (int) (Math.floor(point.y * 1000.0) * scaleY);
                        Log.v(TAG, "AF (" + posX + ", " + posY + ")");
                        String sendUrl = String.format(Locale.US, "%s%s&point=%04dx%04d", COMMUNICATION_URL, AF_FRAME_COMMAND, posX, posY);
                        String reply =  SimpleHttpClient.httpGetWithHeader(sendUrl, headerMap, null, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "setTouchAFPosition() reply is null.");
                        }

                        if (findTouchAFPositionResult(reply))
                        {
                            // AF FOCUSED
                            Log.v(TAG, "lockAutoFocus() : FOCUSED");
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Focused, 0.0);
                        }
                        else
                        {
                            // AF ERROR
                            Log.v(TAG, "lockAutoFocus() : ERROR");
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Failed, 1.0);
                        }
                        showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Errored, 1.0);
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
     *   シャッター半押し処理
     *
     */
    public void halfPressShutter(final boolean isPressed)
    {
        Log.v(TAG, "halfPressShutter() : " + isPressed);
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String status = (isPressed) ? "on" : "off";
/*
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + "cam.cgi?mode=camctrl&type=touch&value=500/500&value2=" + status, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "CENTER FOCUS (" + status + ") FAIL...");
                        }
                        else
                        {
                            indicator.onAfLockUpdate(isPressed);
                        }
*/
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

                        String reply =  SimpleHttpClient.httpGetWithHeader(COMMUNICATION_URL + AF_RELEASE_COMMAND, headerMap, null, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "unlockAutoFocus() reply is null.");
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
    private static boolean findTouchAFPositionResult(String replyXml)
    {
        try
        {
            if (replyXml.contains("ok"))
            {
                return (true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
}
