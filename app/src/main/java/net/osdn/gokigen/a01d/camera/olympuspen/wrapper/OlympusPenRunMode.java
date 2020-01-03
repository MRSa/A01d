package net.osdn.gokigen.a01d.camera.olympuspen.wrapper;

import android.util.Log;

import net.osdn.gokigen.a01d.camera.olympus.wrapper.ICameraRunMode;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

public class OlympusPenRunMode implements ICameraRunMode
{
    private final String TAG = this.toString();
    private boolean runMode = false;

    @Override
    public void changeRunMode(final boolean isRecording)
    {
        final int TIMEOUT_MS = 5000;
        try
        {
            Log.v(TAG, " changeRunMode : " + isRecording);
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // カメラとの接続確立を通知する
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
                    headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

                    String playModeUrl = "http://192.168.0.10/switch_cammode.cgi";
                    if (isRecording)
                    {
                        playModeUrl = playModeUrl + "?mode=rec";
                    }
                    else
                    {
                        playModeUrl = playModeUrl + "?mode=play";
                    }
                    String response = SimpleHttpClient.httpGetWithHeader(playModeUrl, headerMap, null, TIMEOUT_MS);
                    Log.v(TAG, " " + playModeUrl + " " + response);
                    try
                    {
                        if (response.contains("ok"))
                        {
                            runMode = isRecording;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRecordingMode()
    {
        return (runMode);
    }
}
