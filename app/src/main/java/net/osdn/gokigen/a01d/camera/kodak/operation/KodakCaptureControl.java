package net.osdn.gokigen.a01d.camera.kodak.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandCallback;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.IKodakCommandPublisher;
import net.osdn.gokigen.a01d.camera.kodak.wrapper.command.messages.specific.KodakExecuteShutter;

import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

public class KodakCaptureControl implements ICaptureControl, IKodakCommandCallback
{
    private final String TAG = this.toString();
    private final IKodakCommandPublisher commandPublisher;
    //private final IAutoFocusFrameDisplay frameDisplayer;

    public KodakCaptureControl(@NonNull IKodakCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer)
    {
        this.commandPublisher = commandPublisher;
        //this.frameDisplayer = frameDisplayer;
    }

    @Override
    public void doCapture(int kind)
    {
        try
        {
            // シャッター
            Log.v(TAG, " doCapture() ");

            // シャッターを切る
            commandPublisher.enqueueCommand(new KodakExecuteShutter(this));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, " KodakCaptureControl::receivedMessage() : ");
/*
        try
        {
            int responseCode = (rx_body[8] & 0xff) + ((rx_body[9] & 0xff) * 256);
            if ((rx_body.length > 10) && (responseCode != 0x2001))
            {
                Log.v(TAG, String.format(" RECEIVED NG REPLY ID : %d, RESPONSE CODE : 0x%04x ", id, responseCode));
            }
            else
            {
                Log.v(TAG, String.format(" OK REPLY (ID : %d) ", id));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
*/
    }
}
