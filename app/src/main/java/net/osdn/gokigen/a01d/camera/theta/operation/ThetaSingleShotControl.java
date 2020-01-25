package net.osdn.gokigen.a01d.camera.theta.operation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.theta.wrapper.IThetaSessionIdProvider;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.a01d.liveview.IIndicatorControl;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

public class ThetaSingleShotControl
{
    private final String TAG = toString();
    private final IThetaSessionIdProvider sessionIdProvider;
    private final ILiveViewControl liveViewControl;
    private final IIndicatorControl indicator;
    private boolean useThetaV21 = false;
    private int timeoutMs = 6000;


    public ThetaSingleShotControl(@NonNull Context context, @NonNull final IThetaSessionIdProvider sessionIdProvider, @NonNull IIndicatorControl indicator, @NonNull ILiveViewControl liveViewControl)
    {
        this.sessionIdProvider = sessionIdProvider;
        this.liveViewControl = liveViewControl;
        this.indicator = indicator;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            useThetaV21 = preferences.getBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "USE THETA WEB API V2.1 (OSC V2) : " + useThetaV21);
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
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String shootUrl = "http://192.168.1.1/osc/commands/execute";
                        String postData = (useThetaV21) ? "{\"name\":\"camera.takePicture\",\"parameters\":{\"timeout\":0}}" : "{\"name\":\"camera.takePicture\",\"parameters\":{\"sessionId\": \"" + sessionIdProvider.getSessionId() + "\"}}";
                        String result = SimpleHttpClient.httpPost(shootUrl, postData, timeoutMs);
                        if ((result == null)||(result.length() < 1))
                        {
                            Log.v(TAG, "singleShot() reply is null.");
                        }
                        else
                        {
                            Log.v(TAG, " singleShot() : " + result);
                            indicator.onShootingStatusUpdate(IIndicatorControl.shootingStatus.Starting);

                            // TODO: 画像処理が終わるまで待つ ... 本当は状態を見て次に進めないといけない
                            waitMs(3500);

                            // ライブビューのの再実行を指示する
                            indicator.onShootingStatusUpdate(IIndicatorControl.shootingStatus.Stopping);
                            liveViewControl.stopLiveView();
                            waitMs(1500);
                            liveViewControl.startLiveView();
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

    private void waitMs(int waitMs)
    {
        try
        {
            Thread.sleep(waitMs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
