package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.app.Activity;
import android.util.Log;

import jp.co.olympus.camerakit.OLYCamera;

/**
 *
 *
 */
class OlyCameraWrapper implements ICameraRunMode
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
}
