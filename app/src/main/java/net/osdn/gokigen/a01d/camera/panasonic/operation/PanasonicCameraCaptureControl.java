package net.osdn.gokigen.a01d.camera.panasonic.operation;

import android.util.Log;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCameraApi;
import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import androidx.annotation.NonNull;

public class PanasonicCameraCaptureControl implements ICaptureControl
{
    private static final String TAG = PanasonicCameraCaptureControl.class.getSimpleName();
    private final SingleShotControl singleShotControl;

    public PanasonicCameraCaptureControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        singleShotControl = new SingleShotControl(frameDisplayer, indicator);
    }

    public void setCameraApi(@NonNull IPanasonicCameraApi panasonicCameraApi)
    {
        singleShotControl.setCameraApi(panasonicCameraApi);
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
