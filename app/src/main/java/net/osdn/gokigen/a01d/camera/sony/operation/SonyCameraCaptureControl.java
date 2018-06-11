package net.osdn.gokigen.a01d.camera.sony.operation;

import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.sony.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCameraApi;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class SonyCameraCaptureControl implements ICaptureControl
{
    private static final String TAG = SonyCameraCaptureControl.class.getSimpleName();
    private final SingleShotControl singleShotControl;

    public SonyCameraCaptureControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        singleShotControl = new SingleShotControl(frameDisplayer, indicator);
    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        singleShotControl.setCameraApi(sonyCameraApi);
    }

    /**
     *   撮影する
     *
     */
    @Override
    public void doCapture(int kind)
    {
        Log.v(TAG, "doCapture() : " + kind);
        try
        {
            singleShotControl.singleShot();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
