package net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.connection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.ricohgr2.wrapper.IUsePentaxCommand;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.a01d.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;

class RicohGr2CameraConnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;
    private final IUsePentaxCommand usePentaxCommand;

    RicohGr2CameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection, @NonNull IUsePentaxCommand usePentaxCommand)
    {
        Log.v(TAG, "RicohGr2CameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
        this.usePentaxCommand = usePentaxCommand;
    }

    @Override
    public void run()
    {
        final String areYouThereUrl = "http://192.168.0.1/v1/ping";
        final String grCommandUrl = "http://192.168.0.1/_gr";
        final int TIMEOUT_MS = 5000;
        try
        {
            String response = SimpleHttpClient.httpGet(areYouThereUrl, TIMEOUT_MS);
            Log.v(TAG, areYouThereUrl + " " + response);
            if (response.length() > 0)
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                // 接続時、レンズロックOFF + GR2 コマンド有効/無効の確認
                {
                    final String postData = "cmd=acclock off";
                    String response0 = SimpleHttpClient.httpPost(grCommandUrl, postData, TIMEOUT_MS);
                    Log.v(TAG, grCommandUrl + " " + response0);

                    // GR2 専用コマンドを受け付けられるかどうかで、Preference を書き換える
                    boolean pentaxCommand = !(response0.length() > 0);
                    usePentaxCommand.setUsePentaxCommand(pentaxCommand);

                    // 接続時、カメラの画面を消す
                    if ((pentaxCommand)&&(preferences.getBoolean(IPreferencePropertyAccessor.GR2_LCD_SLEEP, false)))
                    {
                        final String postData0 = "cmd=lcd sleep on";
                        String response1 = SimpleHttpClient.httpPost(grCommandUrl, postData0, TIMEOUT_MS);
                        Log.v(TAG, grCommandUrl + " " + response1);
                    }
                }
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
