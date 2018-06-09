package net.osdn.gokigen.a01d.camera.sony.wrapper.connection;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCamera;
import net.osdn.gokigen.a01d.camera.sony.wrapper.ISonyCameraHolder;


/**
 *   SONYカメラとの接続処理
 *
 */
public class SonyCameraConnectSequence implements Runnable, SonySsdpClient.ISearchResultCallback
{
    private final String TAG = this.toString();
    private final Context context;
    private final ICameraConnection cameraConnection;
    private final ISonyCameraHolder cameraHolder;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final SonySsdpClient client;

    SonyCameraConnectSequence(Context context, ICameraStatusReceiver statusReceiver, final ICameraConnection cameraConnection, final @NonNull ISonyCameraHolder cameraHolder)
    {
        Log.v(TAG, "SonyCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.cameraHolder = cameraHolder;
        client = new SonySsdpClient(context, this, statusReceiver, -1);
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
    public void onDeviceFound(ISonyCamera cameraDevice)
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
        Log.v(TAG, "SonyCameraConnectSequence.onFinished()");
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
    public void onErrorFinished(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }
}
