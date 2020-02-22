package net.osdn.gokigen.a01d.camera.ptpip.wrapper.command;

import android.util.Log;

public class PtpIpResponseReceiver implements IPtpIpCommandCallback
{
    private final String TAG = toString();

    public PtpIpResponseReceiver()
    {
        //
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        if (rx_body != null)
        {
            try
            {
                if (rx_body.length > 10)
                {
                    int responseCode = (rx_body[9] & 0xff) + ((rx_body[10] & 0xff) * 256);
                    Log.v(TAG, String.format(" ID : %d, RESPONSE CODE : 0x%x ", id, responseCode));
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
