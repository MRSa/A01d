package net.osdn.gokigen.a01d.camera.theta.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.utils.SimpleLiveviewSlicer;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;


public class ThetaLiveViewControl implements ILiveViewControl
{
    private final String TAG = toString();
    //private final Context context;
    private final CameraLiveViewListenerImpl liveViewListener;
    private final IThetaSessionIdProvider sessionIdProvider;
    private boolean useOscV2 = false;
    private boolean whileFetching = false;
    private static final int FETCH_ERROR_MAX = 30;

    ThetaLiveViewControl(@NonNull final Context context, @NonNull final IThetaSessionIdProvider sessionIdProvider)
    {
        this.sessionIdProvider = sessionIdProvider;
        liveViewListener = new CameraLiveViewListenerImpl();
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            useOscV2 = preferences.getBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void changeLiveViewSize(String size)
    {
        //

    }

    @Override
    public void startLiveView()
    {
        Log.v(TAG, " startLiveView()");
        //prepare();
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        start();
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
    public void stopLiveView()
    {
        whileFetching = false;
    }

    private void start()
    {
        if (whileFetching)
        {
            Log.v(TAG, "start() already starting.");
            return;
        }
        whileFetching = true;

        // A thread for retrieving liveview data from server.
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.d(TAG, "Starting retrieving streaming data from server.");
                    SimpleLiveviewSlicer slicer = null;
                    int continuousNullDataReceived = 0;
                    try
                    {
                        String streamUrl = "http://192.168.1.1/osc/commands/execute";
                        final String paramData = (useOscV2) ? "{\"name\":\"camera.getLivePreview\",\"parameters\":{\"timeout\":0}}" : "{\"name\":\"camera._getLivePreview\",\"parameters\":{\"sessionId\": \"" + sessionIdProvider.getSessionId() + "\"}}";
                        Log.v(TAG, " >>>>> START THETA PREVIEW : " + streamUrl + " " + paramData);

                        // Create Slicer to open the stream and parse it.
                        slicer = new SimpleLiveviewSlicer();
                        slicer.open(streamUrl, paramData);

                        while (whileFetching)
                        {
                            final SimpleLiveviewSlicer.Payload payload = slicer.nextPayloadForMotionJpeg();
                            if (payload == null)
                            {
                                //Log.v(TAG, "Liveview Payload is null.");
                                continuousNullDataReceived++;
                                if (continuousNullDataReceived > FETCH_ERROR_MAX)
                                {
                                    Log.d(TAG, " FETCH ERROR MAX OVER ");
                                    break;
                                }
                                continue;
                            }
                            //if (mJpegQueue.size() == 2)
                            //{
                            //    mJpegQueue.remove();
                            //}
                            //mJpegQueue.add(payload.getJpegData());
                            liveViewListener.onUpdateLiveView(payload.getJpegData(), null);
                            continuousNullDataReceived = 0;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            if (slicer != null)
                            {
                                slicer.close();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        //mJpegQueue.clear();
                        if ((whileFetching)&&(continuousNullDataReceived > FETCH_ERROR_MAX))
                        {
                            // 再度ライブビューのスタートをやってみる。
                            whileFetching = false;
                            //continuousNullDataReceived = 0;
                            start();
                        }
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
    public void updateDigitalZoom()
    {
        Log.v(TAG, "updateDigitalZoom() ");
    }

    /**
     *   デジタルズーム倍率の設定値を応答する
     *
     */
    @Override
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }

    /**
     *   クロップサイズを変更する
     *
     */
    @Override
    public void updateMagnifyingLiveViewScale(final boolean isChangeScale)
    {
        Log.v(TAG, "updateMagnifyingLiveViewScale() : " + isChangeScale);
    }

    /**
     *   ライブビュー拡大倍率の設定値を応答する
     *
     */
    @Override
    public float getMagnifyingLiveViewScale()
    {
        return (1.0f);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }
}
