package net.osdn.gokigen.a01d.camera.kodak.wrapper.liveview;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.utils.SimpleLiveviewSlicer;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

public class KodakLiveViewControl  implements ILiveViewControl
{
    private final String TAG = this.toString();
    private final Context context;
    private final CameraLiveViewListenerImpl liveViewListener;
    private final String liveViewUrl; // "http://172.16.0.254:9176";
    private boolean whileFetching = false;
    private float cropScale = 1.0f;
    private static final int FETCH_ERROR_MAX = 30;

    /**
     *
     *
     */
    public KodakLiveViewControl(final Context context, @NonNull String camera_ip, int liveview_port)
    {
        this.context = context;
        this.liveViewUrl = "http://" + camera_ip + ":" + liveview_port + "/";
        prepare();
        liveViewListener = new CameraLiveViewListenerImpl();
    }

    /**
     *
     *
     */
    private void prepare()
    {
        try
        {
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            //mirrorMode = preferences.getBoolean(IPreferencePropertyAccessor.GR2_LIVE_VIEW, true);
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
        Log.v(TAG, "startLiveView()");
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
                        start(liveViewUrl);
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

    }

    private void start(@NonNull final String streamUrl)
    {
        if (whileFetching)
        {
            Log.v(TAG, "start() already starting.");
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
                        // Create Slicer to open the stream and parse it.
                        slicer = new SimpleLiveviewSlicer();
                        slicer.open(streamUrl);

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
                        if ((!whileFetching)&&(continuousNullDataReceived > FETCH_ERROR_MAX))
                        {
                            // 再度ライブビューのスタートをやってみる。
                            whileFetching = false;
                            //continuousNullDataReceived = 0;
                            start(streamUrl);
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
        //
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
        //
    }

    /**
     *   ライブビュー拡大倍率の設定値を応答する
     *
     */
    @Override
    public float getMagnifyingLiveViewScale()
    {
        return (cropScale);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }

}
