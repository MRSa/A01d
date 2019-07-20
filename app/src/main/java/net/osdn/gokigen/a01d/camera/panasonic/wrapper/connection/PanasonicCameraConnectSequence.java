package net.osdn.gokigen.a01d.camera.panasonic.wrapper.connection;

import android.app.Activity;
import android.util.Log;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraChangeListener;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCamera;
import net.osdn.gokigen.a01d.camera.panasonic.wrapper.IPanasonicCameraHolder;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

import androidx.annotation.NonNull;

/**
 *   Panasonicカメラとの接続処理
 *
 */
public class PanasonicCameraConnectSequence implements Runnable, PanasonicSsdpClient.ISearchResultCallback
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final IPanasonicCameraHolder cameraHolder;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final ICameraChangeListener listener;
    private final PanasonicSsdpClient client;

    PanasonicCameraConnectSequence(Activity context, ICameraStatusReceiver statusReceiver, final ICameraConnection cameraConnection, final @NonNull IPanasonicCameraHolder cameraHolder, final @NonNull ICameraChangeListener listener)
    {
        Log.v(TAG, "PanasonicCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.cameraHolder = cameraHolder;
        this.listener = listener;
        client = new PanasonicSsdpClient(context, this, statusReceiver, 1);
    }

    @Override
    public void run()
    {
        Log.v(TAG, "search()");
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_start));
            client.search();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceFound(IPanasonicCamera cameraDevice)
    {
        try
        {
            cameraStatusReceiver.onStatusNotify(context.getString(R.string.camera_detected) + " " + cameraDevice.getFriendlyName());
            cameraHolder.detectedCamera(cameraDevice);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinished()
    {
        Log.v(TAG, "PanasonicCameraConnectSequence.onFinished()");
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cameraHolder.prepare();
                        cameraHolder.startRecMode();
                        cameraHolder.startEventWatch(listener);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.v(TAG, "CameraConnectSequence:: connected.");
                    onConnectNotify();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void onConnectNotify()
    {
        try
        {
            final Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    // カメラとの接続確立を通知する
                    cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
                    cameraStatusReceiver.onCameraConnected();
                    Log.v(TAG, "onConnectNotify()");
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void waitForAMoment(long mills)
    {
        if (mills > 0)
        {
            try {
                Log.v(TAG, " WAIT " + mills + "ms");
                Thread.sleep(mills);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onErrorFinished(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }

}
