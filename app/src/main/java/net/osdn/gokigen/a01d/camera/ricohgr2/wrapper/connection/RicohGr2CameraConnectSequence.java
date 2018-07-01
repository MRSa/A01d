package net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.connection;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

class RicohGr2CameraConnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;

    RicohGr2CameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection)
    {
        Log.v(TAG, "RicohGr2CameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
    }

    @Override
    public void run()
    {
        final String areYouThereUrl = "http://192.168.0.1/v1/ping";
        final int TIMEOUT_MS = 5000;
        try
        {
            String response = SimpleHttpClient.httpGet(areYouThereUrl, TIMEOUT_MS);
            Log.v(TAG, areYouThereUrl + " " + response);
            if (response.length() > 0)
            {
                onConnectNotify();
            }
            else
            {
                onConnectError(context.getString(R.string.camera_not_found));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            onConnectError(e.getLocalizedMessage());
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

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }
}
