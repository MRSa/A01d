package net.osdn.gokigen.a01d.camera.canon.operation;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.a01d.camera.ICaptureControl;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.PtpIpCommandPublisher;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.messages.PtpIpCommandGeneric;
import net.osdn.gokigen.a01d.liveview.IAutoFocusFrameDisplay;

import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.REQUEST_SHUTTER_OFF;
import static net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpMessages.REQUEST_SHUTTER_ON;

public class CanonCaptureControl implements ICaptureControl, IPtpIpCommandCallback
{
    private final String TAG = this.toString();
    private final PtpIpCommandPublisher commandPublisher;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private boolean isDumpLog = true;

    public CanonCaptureControl(@NonNull PtpIpCommandPublisher commandPublisher, IAutoFocusFrameDisplay frameDisplayer)
    {
        this.commandPublisher = commandPublisher;
        this.frameDisplayer = frameDisplayer;
    }


    @Override
    public void doCapture(int kind)
    {
        try
        {
            // シャッター
            Log.v(TAG, " doCapture() ");

            // シャッターONとOFF
            commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, REQUEST_SHUTTER_ON, isDumpLog, 0, 0x9128, 8, 0x03, 0x00));
            commandPublisher.enqueueCommand(new PtpIpCommandGeneric(this, REQUEST_SHUTTER_OFF, isDumpLog, 0, 0x9129, 4, 0x03));
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
