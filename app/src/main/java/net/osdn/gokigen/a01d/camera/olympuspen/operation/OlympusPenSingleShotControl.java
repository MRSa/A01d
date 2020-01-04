package net.osdn.gokigen.a01d.camera.olympuspen.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.panasonic.operation.takepicture.SingleShotControl;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;

import java.util.HashMap;
import java.util.Map;

public class OlympusPenSingleShotControl
{
    private static final String TAG = SingleShotControl.class.getSimpleName();
    private static final int TIMEOUT_MS = 3000;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private final String COMMUNICATION_URL = "http://192.168.0.10/";
    private final String CAPTURE_COMMAND = "exec_takemotion.cgi?com=starttake";
    private final IIndicatorControl indicator;
    private Map<String, String> headerMap;

    /**
     *
     *
     */
    public OlympusPenSingleShotControl(@NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull IIndicatorControl indicator)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;

        headerMap = new HashMap<>();
        headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
        headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

    }

    /**
     *
     *
     */
    public void singleShot()
    {
        Log.v(TAG, "singleShot()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String reply = SimpleHttpClient.httpGetWithHeader((COMMUNICATION_URL + CAPTURE_COMMAND), headerMap, null, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "Capture Failure... : " + reply);
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
