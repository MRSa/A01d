package net.osdn.gokigen.a01d.camera.ricohgr2.wrapper;

import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.camera.ILiveViewControl;
import net.osdn.gokigen.a01d.camera.utils.SimpleLiveviewSlicer;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.CameraLiveViewListenerImpl;
import net.osdn.gokigen.a01d.liveview.liveviewlistener.ILiveViewListener;

/**
 *
 *
 */
public class RicohGr2LiveViewControl implements ILiveViewControl
{
    private final String TAG = toString();
    private final CameraLiveViewListenerImpl liveViewListener;
    private String liveViewUrl = "http://192.168.0.1/v1/display";
    private boolean whileFetching = false;
    private static final int FETCH_ERROR_MAX = 30;

    /**
     *
     *
     */
    RicohGr2LiveViewControl()
    {
        liveViewListener = new CameraLiveViewListenerImpl();
    }

/*
    public void setLiveViewAddress(@NonNull String address, @NonNull String page)
    {
        liveViewUrl = "http://" + address + "/" + page;
    }
*/

    @Override
    public void changeLiveViewSize(String size)
    {
        //

    }

    @Override
    public void startLiveView()
    {
        Log.v(TAG, "startLiveView()");
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

    }

    @Override
    public void updateMagnifyingLiveViewScale(boolean isChangeScale)
    {

    }

    @Override
    public float getMagnifyingLiveViewScale()
    {
        return (1.0f);
    }

    @Override
    public float getDigitalZoomScale()
    {
        return (1.0f);
    }

    public ILiveViewListener getLiveViewListener()
    {
        return (liveViewListener);
    }
}
