package net.osdn.gokigen.a01d.camera.olympuspen.wrapper.connection;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.R;
import net.osdn.gokigen.a01d.camera.ICameraConnection;
import net.osdn.gokigen.a01d.camera.ICameraStatusReceiver;
import net.osdn.gokigen.a01d.camera.utils.SimpleHttpClient;

import java.util.HashMap;
import java.util.Map;

class OlympusPenCameraConnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final Activity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;

    OlympusPenCameraConnectSequence(@NonNull Activity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection)
    {
        Log.v(TAG, "OlympusPenCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
    }

    @Override
    public void run()
    {
        final String camInfoUrl = "http://192.168.0.10/get_caminfo.cgi";
        final String getCommandListUrl = "http://192.168.0.10/get_commandlist.cgi";
        final String getConnectModeUrl = "http://192.168.0.10/get_connectmode.cgi";
        final String switchCameraModeUrl = "http://192.168.0.10/switch_cammode.cgi";
        //final String getCameraStatusUrl = "http://192.168.0.10/get_activate.cgi";

        final int TIMEOUT_MS = 5000;
        try
        {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("User-Agent", "OlympusCameraKit"); // "OI.Share"
            headerMap.put("X-Protocol", "OlympusCameraKit"); // "OI.Share"

            String response = SimpleHttpClient.httpGetWithHeader(getConnectModeUrl, headerMap, null, TIMEOUT_MS);
            Log.v(TAG, " " + getConnectModeUrl + " " + response);
            if (response.length() > 0)
            {
                String response2 = SimpleHttpClient.httpGetWithHeader(getCommandListUrl, headerMap, null, TIMEOUT_MS);
                Log.v(TAG, " " + getCommandListUrl + " " + response2);

                String response3 = SimpleHttpClient.httpGetWithHeader(camInfoUrl, headerMap, null, TIMEOUT_MS);
                Log.v(TAG, " " + camInfoUrl + " " + response3);

                // 撮影モードに切り替え。
                String lvUrl = switchCameraModeUrl + "?mode=rec" + "&lvqty=" + getLiveViewQuality();
                String response4 = SimpleHttpClient.httpGetWithHeader(lvUrl, headerMap, null, TIMEOUT_MS);
                Log.v(TAG, " " + lvUrl + " " + response4);

                //// カメラのステータス取得
                //String response5 = SimpleHttpClient.httpGetWithHeader(getCameraStatusUrl, headerMap, null, TIMEOUT_MS);
                //Log.v(TAG, " " + getCameraStatusUrl + " " + response5);

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

    private String getLiveViewQuality()
    {
        //  ※ LV解像度をここで取得する (現状は 640x480 固定とする)
        return ("0640x0480");
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

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }
}
