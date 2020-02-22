package net.osdn.gokigen.a01d.camera.ptpip.wrapper.command;

import android.util.Log;

import androidx.annotation.Nullable;

public class PtpIpResponseReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();
    private final IPtpIpResponseReceiver callback;

    public PtpIpResponseReceiver(@Nullable IPtpIpResponseReceiver callback)
    {
        this.callback = callback;
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        int responseCode = 0;
        if (rx_body != null)
        {
            try
            {
                if (rx_body.length > 10)
                {
                    responseCode = (rx_body[8] & 0xff) + ((rx_body[9] & 0xff) * 256);
                    Log.v(TAG, String.format(" ID : %d, RESPONSE CODE : 0x%04x ", id, responseCode));
                }
                else
                {
                    Log.v(TAG, " receivedMessage() " + id + " " + rx_body.length + " bytes.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Log.v(TAG, " receivedMessage() " + id);
        }
        if (callback != null)
        {
            callback.response(id, responseCode);
        }
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        Log.v(TAG, " onReceiveProgress() " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }
}
