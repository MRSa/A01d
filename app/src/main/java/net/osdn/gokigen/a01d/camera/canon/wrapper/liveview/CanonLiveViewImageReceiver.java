package net.osdn.gokigen.a01d.camera.canon.wrapper.liveview;

import android.util.Log;

import androidx.annotation.NonNull;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;

import java.util.Arrays;

/**
 *   Canonサムネイル画像の受信
 *
 *
 */
public class CanonLiveViewImageReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final ICanonLiveViewImageCallback callback;

    CanonLiveViewImageReceiver(@NonNull ICanonLiveViewImageCallback callback)
    {
        this.callback = callback;
    }



    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        try
        {
            if (rx_body == null)
            {
                Log.v(TAG, " BITMAP IS NONE...");
                callback.onCompleted(null, null);
                return;
            }
            Log.v(TAG, " CanonLiveViewImageReceiver::receivedMessage() : " + rx_body.length);

            /////// 受信データから、サムネイルの先頭(0xff 0xd8)を検索する  /////
            int offset = rx_body.length - 22;
            //byte[] thumbnail0 = Arrays.copyOfRange(rx_body, 0, rx_body.length);
            while (offset > 32)
            {
                if ((rx_body[offset] == (byte) 0xff)&&((rx_body[offset + 1] == (byte) 0xd8)))
                {
                    break;
                }
                offset--;
            }
            byte[] thumbnail = Arrays.copyOfRange(rx_body, offset, rx_body.length);
            callback.onCompleted(thumbnail, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            {
                callback.onErrorOccurred(e);
            }
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }
}
