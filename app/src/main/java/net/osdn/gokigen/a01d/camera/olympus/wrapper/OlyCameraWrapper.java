package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.CameraPropertyUtilities;
import net.osdn.gokigen.a01d.liveview.CameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import java.util.Map;

import jp.co.olympus.camerakit.OLYCamera;

/**
 *
 *
 */
class OlyCameraWrapper implements ICameraRunMode, ILiveViewControl
{
    private final String TAG = toString();
    private final Activity context;
    private final OLYCamera camera;

    OlyCameraWrapper(Activity context)
    {
        this.context = context;
        camera = new OLYCamera();
        camera.setContext(context.getApplicationContext());
    }

    OLYCamera getOLYCamera()
    {
        return (camera);
    }



    /**
     *   ICameraRunMode の実装
     *
     */
    @Override
    public void changeRunMode(boolean isRecording)
    {
        OLYCamera.RunMode runMode = (isRecording) ? OLYCamera.RunMode.Recording : OLYCamera.RunMode.Playback;
        Log.v(TAG, "changeRunMode() : " + runMode);
        try
        {
            camera.changeRunMode(runMode);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRecordingMode()
    {
        boolean isRecordingMode = false;
        try
        {
            OLYCamera.RunMode runMode = camera.getRunMode();
            isRecordingMode =  (runMode == OLYCamera.RunMode.Recording);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (isRecordingMode);
    }


    /**
     *   ILiveViewControl の実装
     *
     */

    @Override
    public void changeLiveViewSize(String size)
    {
        try
        {
            camera.changeLiveViewSize(CameraPropertyUtilities.toLiveViewSizeType(size));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setLiveViewListener(CameraLiveViewListenerImpl listener)
    {
        try
        {
            camera.setLiveViewListener(listener);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void startLiveView()
    {
        try
        {
            camera.startLiveView();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   デジタルズームする
     *
     */
    @Override
    public void setDigitalZoom(float scale)
    {

    }

    /**
     *   ライブビューを拡大する
     *
     */
    @Override
    public void setLiveViewScale(float scale)
    {

    }

    /**
     *   ライブビューを拡大する
     *
     */
    @Override
    public void updateDigitalZoom()
    {
        try
        {
            float scale = getDigitalZoomScalePreference();
            camera.changeDigitalZoomScale(scale);
            Log.v(TAG, "DIGITAL ZOOM SCALE : " + scale);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   ライブビューを拡大する
     *
     */
    @Override
    public void updateMagnifyingLiveViewScale()
    {
       try
       {
           float scale = getMagnifyingLiveViewScale();
           if (scale < 5.0f)
           {
               if (camera.isMagnifyingLiveView())
               {
                   camera.stopMagnifyingLiveView();
                   Log.v(TAG, "RESET LIVE VIEW SCALE : " + 1.0f);
               }
               return;
           }
           OLYCamera.MagnifyingLiveViewScale setScale;
           if (scale >= 14.0f)
           {
               setScale = OLYCamera.MagnifyingLiveViewScale.X14;
           }
           else if (scale >= 10.0f)
           {
               setScale = OLYCamera.MagnifyingLiveViewScale.X10;
           }
           else if (scale >= 7.0f)
           {
               setScale = OLYCamera.MagnifyingLiveViewScale.X7;
           }
           else // if (scale >= 5.0f)
           {
               setScale = OLYCamera.MagnifyingLiveViewScale.X5;
           }
           changeMagnifyingLiveView(setScale);
           Log.v(TAG, "SET LIVE VIEW SCALE : " + scale);
       }
       catch (Exception e)
       {
           e.printStackTrace();
       }
    }

    /**
     *   ライブビューの中心を拡大する
     *
     */
    private void changeMagnifyingLiveView(OLYCamera.MagnifyingLiveViewScale setScale)
    {
        try
        {
            PointF centerPoint = new PointF(0.5f, 0.5f);  // 中心座標
            if (camera.isMagnifyingLiveView())
            {
                camera.changeMagnifyingLiveViewScale(setScale);
            }
            else
            {
                camera.startMagnifyingLiveViewAtPoint(centerPoint, setScale);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   ライブビュー拡大倍率の設定値を応答する
     *
     */
    private float getMagnifyingLiveViewScale()
    {
        float scale = 1.0f;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(IPreferencePropertyAccessor.MAGNIFYING_LIVE_VIEW_SCALE, IPreferencePropertyAccessor.MAGNIFYING_LIVE_VIEW_SCALE_DEFAULT_VALUE);
        try
        {
            scale = Float.parseFloat(value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            scale = 1.0f;
        }
        return (scale);
    }

    /**
     *   デジタルズーム倍率の設定値を応答する
     *
     */
    private float getDigitalZoomScalePreference()
    {
        float scale = 1.0f;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(IPreferencePropertyAccessor.DIGITAL_ZOOM_LEVEL, IPreferencePropertyAccessor.DIGITAL_ZOOM_LEVEL_DEFAULT_VALUE);
        try
        {
            Map<String, Float> range = camera.getDigitalZoomScaleRange();
            float max = range.get(OLYCamera.DIGITAL_ZOOM_SCALE_RANGE_MAXIMUM_KEY);
            float min = range.get(OLYCamera.DIGITAL_ZOOM_SCALE_RANGE_MINIMUM_KEY);

            if (value.equals("MAX"))
            {
                scale = max;
            }
            else
            {
                scale = Float.parseFloat(value);
            }
            if (scale < min)
            {
                scale = min;
            }
            else if (scale > max)
            {
                scale = max;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            scale = 1.0f;
        }
        return (scale);
    }
}
