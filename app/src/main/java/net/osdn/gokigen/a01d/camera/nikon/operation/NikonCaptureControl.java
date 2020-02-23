package net.osdn.gokigen.a01d.camera.nikon.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.REQUEST_SHUTTER_ON;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_DEVICE_READY;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.SEQ_GET_EVENT;

public class NikonCaptureControl implements ICaptureControl, IPtpIpCommandCallback
{
    private final String TAG = this.toString();
    private final PtpIpCommandPublisher commandPublisher;
    //private final IAutoFocusFrameDisplay frameDisplayer;
    private boolean isDumpLog = true;

    public NikonCaptureControl(@NonNull PtpIpCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer)
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
            commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, REQUEST_SHUTTER_ON, isDumpLog, 0, 0x9207, 8, 0xffffffff, 0x00));
            commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_DEVICE_READY, isDumpLog, 0, 0x90c8));
            commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, SEQ_GET_EVENT, isDumpLog, 0, 0x90c7));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedMessage(int id, byte[] rx_body)
    {
        Log.v(TAG, " CanonCaptureControl::receivedMessage() : ");
        try
        {
            if ((rx_body.length > 10)&&((rx_body[8] != (byte) 0x01)||(rx_body[9] != (byte) 0x20)))
            {
                Log.v(TAG, " --- RECEIVED NG REPLY. : " + id);
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
        Log.v(TAG, " CanonCaptureControl::onReceiveProgress() : " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

}
