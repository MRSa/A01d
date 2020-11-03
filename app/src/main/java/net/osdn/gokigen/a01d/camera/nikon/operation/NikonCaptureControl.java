package net.osdn.gokigen.a01d.camera.nikon.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGenericWithRetry;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.REQUEST_SHUTTER_ON;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_DEVICE_READY;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_EVENT;

public class NikonCaptureControl implements ICaptureControl, IPtpIpCommandCallback
{
    private final String TAG = this.toString();
    private final PtpIpCommandPublisher commandPublisher;
    //private final IAutoFocusFrameDisplay frameDisplayer;

    public NikonCaptureControl(@NonNull PtpIpCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer)
    {
        this.commandPublisher = commandPublisher;
        //this.frameDisplayer = frameDisplayer;
    }

    @Override
    public void doCapture(int kind)
    {
        boolean isDumpLog = false;
        try
        {
            // シャッター
            Log.v(TAG, " doCapture() ");

            // シャッターを切る
            commandPublisher.enqueueCommand(new PtpIpCommandGenericWithRetry(this, REQUEST_SHUTTER_ON, 30, 200, false, isDumpLog, 0, 0x9207, 8, 0xffffffff, 0x00, 0, 0));
            commandPublisher.enqueueCommand(new PtpIpCommandGenericWithRetry(this, SEQ_DEVICE_READY, 30, 200, false, isDumpLog, 0, 0x90c8, 0, 0, 0, 0, 0));
            commandPublisher.enqueueCommand(new PtpIpCommandGenericWithRetry(this, SEQ_GET_EVENT, 30, 200, false, isDumpLog, 0, 0x90c7, 0, 0, 0, 0, 0));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, " NikonCaptureControl::receivedMessage() : ");
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
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] rx_body)
    {
        Log.v(TAG, " NikonCaptureControl::onReceiveProgress() : " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

}
