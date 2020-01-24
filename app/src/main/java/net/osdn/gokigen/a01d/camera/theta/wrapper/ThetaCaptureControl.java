package net.osdn.gokigen.a01d.camera.theta.wrapper;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.olympuspen.operation.OlympusPenSingleShotControl;
import net.osdn.gokigen.a01d.camera.panasonic.operation.PanasonicCameraCaptureControl;
import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera;
import net.osdn.gokigen.a01d.camera.theta.operation.ThetaSingleShotControl;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class ThetaCaptureControl implements ICaptureControl
{
    private static final String TAG = ThetaCaptureControl.class.getSimpleName();
    private final ThetaSingleShotControl singleShotControl;

    public ThetaCaptureControl(@NonNull Context context, @NonNull final IThetaSessionIdProvider sessionIdProvider, @NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        singleShotControl = new ThetaSingleShotControl(context, sessionIdProvider, frameDisplayer, indicator);
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
