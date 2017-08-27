package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.property.CameraPropertyUtilities;
import net.osdn.gokigen.a01d.liveview.CameraLiveViewListenerImpl;

import jp.co.olympus.camerakit.OLYCamera;

/**
 *
 *
 */
class OlyCameraWrapper implements ICameraRunMode, ILiveViewControl
{
    private final String TAG = toString();
    //private final Activity context;
    private final OLYCamera camera;

    OlyCameraWrapper(Activity context)
    {
        //this.context = context;
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
}
