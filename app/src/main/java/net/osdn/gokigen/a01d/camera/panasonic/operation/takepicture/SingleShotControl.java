package net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture;

import android.util.Log;

import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCameraApi;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import org.json.JSONObject;

import androidx.annotation.NonNull;

public class SingleShotControl
{
    private static final String TAG = SingleShotControl.class.getSimpleName();
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final IIndicatorControl indicator;
    private IPanasonicCameraApi cameraApi = null;

    /**
     *
     *
     */
    public SingleShotControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
    }

    /**
     *
     *
     */
    public void setCameraApi(@NonNull IPanasonicCameraApi panasonicCameraApi)
    {
        this.cameraApi = panasonicCameraApi;
    }

    /**
     *
     *
     */
    public void singleShot()
    {
        Log.v(TAG, "singleShot()");
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
                        //JSONObject resultsObj = cameraApi.awaitTakePicture();
                        JSONObject resultsObj = cameraApi.actTakePicture();
                        if (resultsObj == null)
                        {
                            Log.v(TAG, "setTouchAFPosition() reply is null.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    frameDisplayer.hideFocusFrame();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
