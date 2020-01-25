package net.osdn.gokigen.a01d.camera.theta.wrapper;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.theta.operation.ThetaSingleShotControl;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

public class ThetaCaptureControl implements ICaptureControl
{
    private static final String TAG = ThetaCaptureControl.class.getSimpleName();
    private final ThetaSingleShotControl singleShotControl;

    ThetaCaptureControl(@NonNull Context context, @NonNull final IThetaSessionIdProvider sessionIdProvider, @NonNull IIndicatorControl indicator, @NonNull ILiveViewControl liveViewControl)
    {
        singleShotControl = new ThetaSingleShotControl(context, sessionIdProvider, indicator, liveViewControl);
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
