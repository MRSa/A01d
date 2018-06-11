package net.osdn.gokigen.a01d.camera.olympus.wrapper;

import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.olympus.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class OlyCameraCaptureControl implements ICaptureControl
{
    private final String TAG = toString();
    private final SingleShotControl singleShotControl;

    public OlyCameraCaptureControl(OlyCameraWrapper wrapper, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        singleShotControl = new SingleShotControl(wrapper.getOLYCamera(), frameDisplayer, indicator);
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
