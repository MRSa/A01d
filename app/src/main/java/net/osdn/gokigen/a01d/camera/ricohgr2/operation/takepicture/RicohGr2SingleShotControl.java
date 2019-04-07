package net.osdn.gokigen.a01d.camera.ricohgr2.operation.takepicture;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;

/**
 *
 *
 *
 */
public class RicohGr2SingleShotControl
{
    private static final String TAG = RicohGr2SingleShotControl.class.getSimpleName();
    private final String shootUrl = "http://192.168.0.1/v1/camera/shoot";
    private final IAutoFocusFrameDisplay frameDisplayer;
    private boolean usePentaxDslr = false;
    private int timeoutMs = 6000;

    /**
     *
     *
     */
    public RicohGr2SingleShotControl(@NonNull Context context, @NonNull IAutoFocusFrameDisplay frameDisplayer)
    {
        this.frameDisplayer = frameDisplayer;
        prepare(context);
    }

    /**
     *
     *
     */
    private void prepare(@NonNull Context context)
    {
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            usePentaxDslr = preferences.getBoolean(IPreferencePropertyAccessor.USE_PENTAX_AUTOFOCUS, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "USE PENTAX DSLR : " + usePentaxDslr);
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
                        String postData = (usePentaxDslr) ? "" : "af=camera";
                        String result = SimpleHttpClient.httpPost(shootUrl, postData, timeoutMs);
                        if ((result == null)||(result.length() < 1))
                        {
                            Log.v(TAG, "singleShot() reply is null.");
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
