package net.osdn.gokigen.a01d.camera.sony.operation.takepicture;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCameraApi;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 *
 */
public class SonyAutoFocusControl
{
    private static final String TAG = SonyAutoFocusControl.class.getSimpleName();
    private final IIndicatorControl indicator;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private ISonyCameraApi cameraApi = null;

    /**
     *
     *
     */
    public SonyAutoFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, final IIndicatorControl indicator)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
    }

    /**
     *
     *
     */
    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        this.cameraApi = sonyCameraApi;
    }

    /**
     *
     *
     */
    public void lockAutoFocus(@NonNull final PointF point)
    {
        Log.v(TAG, "lockAutoFocus() : [" + point.x + ", " + point.y + "]");
        if (cameraApi == null)
        {
            Log.v(TAG, "ISonyCameraApi is null...");
            return;
        }
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

                        Log.v(TAG, "AF (" + point.x + ", " + point.y + ")");
                        JSONObject resultsObj = cameraApi.setTouchAFPosition(point.x, point.y);
                        if (resultsObj == null)
                        {
                            Log.v(TAG, "setTouchAFPosition() reply is null.");
                        }
                        if (findTouchAFPositionResult(resultsObj))
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
        if (cameraApi == null)
        {
            Log.v(TAG, "ISonyCameraApi is null...");
            return;
        }
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        JSONObject resultsObj = cameraApi.cancelTouchAFPosition();
                        if (resultsObj == null)
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
        float imageWidth = 1.0f;
        float imageHeight = 1.0f;
        imageWidth = frameDisplayer.getContentSizeWidth();
        imageHeight = frameDisplayer.getContentSizeHeight();

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
    private static boolean findTouchAFPositionResult(JSONObject replyJson)
    {
        boolean afResult = false;
        try
        {
            int indexOfTouchAFPositionResult = 1;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfTouchAFPositionResult))
            {
                JSONObject touchAFPositionResultObj = resultsObj.getJSONObject(indexOfTouchAFPositionResult);
                afResult = touchAFPositionResultObj.getBoolean("AFResult");
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
